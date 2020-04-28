package com.sc.property.service.repair;

import com.sc.base.dto.repair.RepairDto;
import com.sc.base.dto.repair.RepairOrderDto;
import com.sc.base.entity.repair.RepairEntity;
import com.sc.base.entity.repair.RepairOrderEntity;
import com.sc.base.entity.work.WorkEntity;
import com.sc.base.enums.*;
import com.sc.base.repository.repair.RepairOrderRepository;
import com.sc.base.repository.repair.RepairRepository;
import com.sc.base.repository.work.WorkRepository;
import myString.MyStringUtils;
import mydate.MyDateUtil;
import myspringbean.MyBeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vo.Result;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepairService {

    @Autowired
    private RepairRepository repairRepository;
    @Autowired
    private RepairOrderRepository repairOrderRepository;
    @Autowired
    private WorkRepository workRepository;

    /**
     * 查询报修列表
     * @return
     */
    public Result<List<RepairDto>> findRepairEntityList(RepairDto repairDto){
        try {
            //多条件排序
            Sort sort = Sort.by(Sort.Direction.DESC,"createDate");
            //页数与每页大小
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE,sort);
            //条件
            Page<RepairEntity> page = repairRepository.findAll(new Specification<RepairEntity>() {
                @Override
                public Predicate toPredicate(Root<RepairEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    ArrayList<Predicate> predicateList = new ArrayList<>();
                    predicateList.add(criteriaBuilder.equal(root.get("maintenanceStatus"), RepairStatusEnum.DISPATCH.getType()));
                    predicateList.add(criteriaBuilder.equal(root.get("whetherValid"), WhetherValidEnum.VALID.getType()));
                    return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
                }
            }, pageable);
            List<RepairDto> repairDtoList = page.getContent().stream().map(e -> {
                RepairDto dto = MyBeanUtils.copyPropertiesAndResTarget(e, RepairDto::new);
                dto.setCreateDateStr(MyDateUtil.getDateAndTime(e.getCreateDate()));
                dto.setUpdateDateStr(MyDateUtil.getDateAndTime(e.getUpdateDate()));
                dto.setWhetherValidStr(WhetherValidEnum.getTypesName(e.getWhetherValid()));
                dto.setMaintenanceStatusStr(RepairStatusEnum.getTypesName(e.getMaintenanceStatus()));
                return dto;
            }).collect(Collectors.toList());
            return new Result<List<RepairDto>>().setSuccess(repairDtoList).setCount(page.getTotalElements());
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    /**
     * 查询详情
     * @param repairDto
     * @return
     */
    public Result<RepairDto> findRepairEntityById(RepairDto repairDto){
        try {
            RepairEntity repairEntity = repairRepository.findRepairEntityById(repairDto.getId());
            RepairDto repairDto1 = MyBeanUtils.copyPropertiesAndResTarget(repairEntity, RepairDto::new, d -> {
                d.setCreateDateStr(MyDateUtil.getDateAndTime(repairEntity.getCreateDate()));
                d.setUpdateDateStr(MyDateUtil.getDateAndTime(repairEntity.getUpdateDate()));
                d.setWhetherValidStr(WhetherValidEnum.getTypesName(repairEntity.getWhetherValid()));
                d.setMaintenanceStatusStr(RepairStatusEnum.getTypesName(repairEntity.getMaintenanceStatus()));
            });
            return new Result<RepairDto>().setSuccess(repairDto1);
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    /**
     * 修改报修信息，添加维修订单表信息
     * @param repairDto
     * @return
     */
    public Result addRepairOrderEntity(RepairDto repairDto){
        try {
            Date date = new Date();
            //修改维修表
            RepairEntity repairEntity = repairRepository.findRepairEntityById(repairDto.getId());
            repairEntity.setMaintenanceStatus(RepairStatusEnum.SUCCESSFUL_DISPATCH.getType());
            repairEntity.setStaffUserId(repairDto.getStaffUserId());
            repairEntity.setStaffUserActualName(repairDto.getStaffUserActualName());
            repairEntity.setStaffUserPhoneNumber(repairDto.getStaffUserPhoneNumber());
            repairEntity.setUpdateDate(date);
            repairRepository.save(repairEntity);
            //添加维修订单表
            RepairOrderEntity repairOrderEntity = new RepairOrderEntity();
            repairOrderEntity.setId(MyStringUtils.getIdDateStr("repair_order"));
            repairOrderEntity.setCreateDate(date);
            repairOrderEntity.setUpdateDate(date);
            repairOrderEntity.setWhetherValid(WhetherValidEnum.VALID.getType());
            repairOrderEntity.setScore(3);
            repairOrderEntity.setRepairId(repairDto.getId());
            repairOrderEntity.setStaffUserId(repairDto.getStaffUserId());
            repairOrderEntity.setRepairmanStatus(RepairOrderStatusEnum.RECEIVE_DISPATCH.getType());
            repairOrderRepository.save(repairOrderEntity);
            //修改工作表
            Page<WorkEntity> page = findWorkEntity(repairDto.getStaffUserId());
            page.getContent().stream().forEach(e->{
                e.setWorkStatus(WorkStatusEnum.BE_BUSY.getType());
                e.setUpdateDate(date);
                workRepository.save(e);
            });
            return Result.createSimpleSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    /**
     * 我的维修任务
     * @return
     */
    public Result<List<RepairDto>> findMyRepairEntityList(RepairDto repairDto){
        try {
            //多条件排序
            Sort sort = Sort.by(Sort.Direction.DESC,"createDate");
            //页数与每页大小
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE,sort);
            //条件
            Page<RepairEntity> page = repairRepository.findAll(new Specification<RepairEntity>() {
                @Override
                public Predicate toPredicate(Root<RepairEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    ArrayList<Predicate> predicateList = new ArrayList<>();
                    if (StringUtils.isNotBlank(repairDto.getRepairmanStatus())){
                        predicateList.add(criteriaBuilder.equal(root.get("repairmanStatus"), repairDto.getRepairmanStatus()));
                    }
                    predicateList.add(criteriaBuilder.equal(root.get("staffUserId"), repairDto.getStaffUserId()));
                    predicateList.add(criteriaBuilder.equal(root.get("whetherValid"), WhetherValidEnum.VALID.getType()));
                    return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
                }
            }, pageable);
            List<RepairDto> repairDtoList = page.getContent().stream().map(e -> {
                RepairDto dto = MyBeanUtils.copyPropertiesAndResTarget(e, RepairDto::new);
                dto.setCreateDateStr(MyDateUtil.getDateAndTime(e.getCreateDate()));
                dto.setUpdateDateStr(MyDateUtil.getDateAndTime(e.getUpdateDate()));
                dto.setWhetherValidStr(WhetherValidEnum.getTypesName(e.getWhetherValid()));
                dto.setMaintenanceStatusStr(RepairStatusEnum.getTypesName(e.getMaintenanceStatus()));
                return dto;
            }).collect(Collectors.toList());
            return new Result<List<RepairDto>>().setSuccess(repairDtoList).setCount(page.getTotalElements());
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    /**
     * 查询RepairOrderEntity
     * @param repairDto
     * @return
     */
    public Result findRepairOrderEntity(RepairDto repairDto){
        try {
            Sort sort = Sort.by(Sort.Direction.DESC,"createDate");
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE,sort);
            Page<RepairOrderEntity> page = repairOrderRepository.findAll(new Specification<RepairOrderEntity>() {
                @Override
                public Predicate toPredicate(Root<RepairOrderEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    ArrayList<Predicate> predicateList = new ArrayList<>();
                    predicateList.add(criteriaBuilder.equal(root.get("staffUserId"), repairDto.getStaffUserId()));
                    predicateList.add(criteriaBuilder.equal(root.get("repairId"), repairDto.getId()));
                    predicateList.add(criteriaBuilder.equal(root.get("whetherValid"), WhetherValidEnum.VALID.getType()));
                    return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
                }
            }, pageable);
            List<RepairOrderDto> repairOrderDtoList = page.getContent().stream().map(e -> {
                return MyBeanUtils.copyPropertiesAndResTarget(e, RepairOrderDto::new, dto -> {
                    dto.setCreateDateStr(MyDateUtil.getDateAndTime(e.getCreateDate()));
                    dto.setUpdateDateStr(MyDateUtil.getDateAndTime(e.getUpdateDate()));
                    dto.setWhetherValidStr(WhetherValidEnum.getTypesName(e.getWhetherValid()));
                    dto.setRepairmanStatusStr(RepairOrderStatusEnum.getTypesName(e.getRepairmanStatus()));
                });
            }).collect(Collectors.toList());
            return new Result().setSuccess(repairOrderDtoList.get(0));
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    /**
     * 取消维修
     * @param repairOrderDto
     * @return
     */
    public Result cacelRepair(RepairOrderDto repairOrderDto){
        try {
            Date date = new Date();
            //修改维修订单表
            RepairOrderEntity repairOrderEntity = repairOrderRepository.findRepairOrderEntityById(repairOrderDto.getId());
            repairOrderEntity.setRepairmanStatus(RepairOrderStatusEnum.CANCEL.getType());
            repairOrderEntity.setScore(-1);
            repairOrderEntity.setUpdateDate(date);
            repairOrderRepository.save(repairOrderEntity);
            //修改工作表
            Page<WorkEntity> page = findWorkEntity(repairOrderDto.getStaffUserId());
            page.getContent().stream().forEach(e->{
                e.setWeight(e.getWeight()-1);
                e.setWorkStatus(WorkStatusEnum.BE_BUSY.getType());
                e.setUpdateDate(date);
                workRepository.save(e);
            });
            RepairEntity repairEntity = repairRepository.findRepairEntityById(repairOrderDto.getRepairId());
            //修改维修表1.查询工作表是否有空闲维修员
            List<WorkEntity> workEntityList = findStaffUserList(repairOrderDto.getStaffUserId()).getData();
            if (workEntityList!=null&&workEntityList.size()>0){
                repairEntity.setStaffUserId(workEntityList.get(0).getStaffUserId());
                repairEntity.setStaffUserActualName(workEntityList.get(0).getStaffUserActualName());
                repairEntity.setStaffUserPhoneNumber(workEntityList.get(0).getStaffUserPhoneNumber());
                repairEntity.setMaintenanceStatus(RepairStatusEnum.SUCCESSFUL_DISPATCH.getType());
                repairRepository.save(repairEntity);
                //添加维修订单表
                RepairOrderEntity repairOrderEntity1 = new RepairOrderEntity();
                repairOrderEntity1.setId(MyStringUtils.getIdDateStr("repair_order"));
                repairOrderEntity1.setCreateDate(date);
                repairOrderEntity1.setUpdateDate(date);
                repairOrderEntity1.setWhetherValid(WhetherValidEnum.VALID.getType());
                repairOrderEntity1.setScore(3);
                repairOrderEntity1.setRepairId(repairEntity.getId());
                repairOrderEntity1.setStaffUserId(repairEntity.getStaffUserId());
                repairOrderEntity1.setRepairmanStatus(RepairOrderStatusEnum.RECEIVE_DISPATCH.getType());
                repairOrderRepository.save(repairOrderEntity);
                //修改工作表
                Page<WorkEntity> page1 = findWorkEntity(repairEntity.getStaffUserId());
                page1.getContent().stream().forEach(e->{
                    e.setWorkStatus(WorkStatusEnum.BE_BUSY.getType());
                    e.setUpdateDate(date);
                    workRepository.save(e);
                });
            }else {
                repairEntity.setMaintenanceStatus(RepairStatusEnum.DISPATCH.getType());
                repairRepository.save(repairEntity);
            }
            return Result.createSimpleSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    /**
     * 开始维修
     * @param repairOrderDto
     * @return
     */
    public Result startRepair(RepairOrderDto repairOrderDto){
        try {
            Date date = new Date();
            //修改维修订单表
            RepairOrderEntity repairOrderEntity = repairOrderRepository.findRepairOrderEntityById(repairOrderDto.getId());
            repairOrderEntity.setRepairmanStatus(RepairOrderStatusEnum.UNDER_MAINTENANCE.getType());
            repairOrderEntity.setUpdateDate(date);
            repairOrderRepository.save(repairOrderEntity);

            RepairEntity repairEntity = repairRepository.findRepairEntityById(repairOrderDto.getRepairId());
            repairEntity.setMaintenanceStatus(RepairStatusEnum.UNDER_MAINTENANCE.getType());
            repairEntity.setUpdateDate(date);
            repairRepository.save(repairEntity);
            return Result.createSimpleSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    /**
     * 完成维修
     * @param repairOrderDto
     * @return
     */
    public Result endRepair(RepairOrderDto repairOrderDto){
        try {
            Date date = new Date();
            //修改维修订单表
            RepairOrderEntity repairOrderEntity = repairOrderRepository.findRepairOrderEntityById(repairOrderDto.getId());
            repairOrderEntity.setRepairmanStatus(RepairOrderStatusEnum.REPAIR_SUCCESSFUL.getType());
            repairOrderEntity.setUpdateDate(date);
            repairOrderRepository.save(repairOrderEntity);

            RepairEntity repairEntity = repairRepository.findRepairEntityById(repairOrderDto.getRepairId());
            repairEntity.setMaintenanceStatus(RepairStatusEnum.REPAIR_SUCCESSFUL.getType());
            repairEntity.setUpdateDate(date);
            repairRepository.save(repairEntity);
            //修改工作表
            Page<WorkEntity> page1 = findWorkEntity(repairEntity.getStaffUserId());
            page1.getContent().stream().forEach(e->{
                e.setWorkStatus(WorkStatusEnum.ON_DUTY_STATUS.getType());
                e.setWeight(e.getWeight()+3);
                e.setUpdateDate(date);
                workRepository.save(e);
            });
            return Result.createSimpleSuccessResult();
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }



    /**
     * 查询上班状态维修员工
     * 根据维修任务，优先级排序
     * @return
     */
    public Result<List<WorkEntity>> findStaffUserList(String staffUserId){
        try {
            //多条件排序
            List<Sort.Order> orderList = new ArrayList<>();
            orderList.add(new Sort.Order(Sort.Direction.ASC,"repairNumber"));
            orderList.add(new Sort.Order(Sort.Direction.ASC,"weight"));
            Sort sort =Sort.by(orderList);
            //页数与每页大小
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE,sort);
            //条件
            Page<WorkEntity> page = workRepository.findAll(new Specification<WorkEntity>() {
                @Override
                public Predicate toPredicate(Root<WorkEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    ArrayList<Predicate> predicateList = new ArrayList<>();
                    predicateList.add(criteriaBuilder.like(root.get("staffUserPosition"), "%"+ PositionEnum.REPAIRMAN.getType()+"%"));
                    predicateList.add(criteriaBuilder.equal(root.get("workStatus"), WorkStatusEnum.ON_DUTY_STATUS.getType()));
                    predicateList.add(criteriaBuilder.equal(root.get("whetherValid"), WhetherValidEnum.VALID.getType()));
                    return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
                }
            }, pageable);
            List<WorkEntity> workEntityList = page.getContent().stream().filter(e -> {
                return !e.getStaffUserId().equals(staffUserId);
            }).collect(Collectors.toList());
            return new Result<List<WorkEntity>>().setSuccess(workEntityList);
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }


    private Page<WorkEntity> findWorkEntity(String staffUserId){
        Sort sort = Sort.by(Sort.Direction.DESC,"createDate");
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE,sort);
        Page<WorkEntity> page = workRepository.findAll(new Specification<WorkEntity>() {
            @Override
            public Predicate toPredicate(Root<WorkEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                ArrayList<Predicate> predicateList = new ArrayList<>();
                predicateList.add(criteriaBuilder.equal(root.get("staffUserId"), staffUserId));
                predicateList.add(criteriaBuilder.equal(root.get("whetherValid"), WhetherValidEnum.VALID.getType()));
                return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
            }
        }, pageable);
        return page;
    }

}