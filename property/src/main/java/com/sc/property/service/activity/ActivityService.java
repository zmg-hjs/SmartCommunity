package com.sc.property.service.activity;

import com.sc.base.dto.activity.ActivityDto;
import com.sc.base.dto.activity.ManageActivityIndexIntoDto;
import com.sc.base.dto.activity.ManageActivityIndexOutDto;
import com.sc.base.dto.common.BaseIntoDto;
import com.sc.base.dto.enroll.EnrollDto;
import com.sc.base.dto.enroll.ManageEnrollIndexIntoDto;
import com.sc.base.dto.enroll.ManageEnrollIndexOutDto;
import com.sc.base.dto.vote.VoteDto;
import com.sc.base.entity.activity.ActivityEntity;
import com.sc.base.entity.enroll.EnrollEntity;
import com.sc.base.entity.vote.VoteEntity;
import com.sc.base.enums.ActivityStatusEnum;
import com.sc.base.enums.AuditStatusEnum;
import com.sc.base.enums.WhetherValidEnum;
import com.sc.base.repository.activity.ActivityRepository;
import com.sc.base.repository.enroll.EnrollRepository;
import com.sc.base.repository.vote.VoteRepository;
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
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private EnrollRepository enrollRepository;
    @Autowired
    private VoteRepository voteRepository;

    public Result addActivityEntity(ActivityDto dto){
        try {
            Date date = new Date();
            ActivityEntity entity = new ActivityEntity();
            entity.setId(MyStringUtils.getIdDateStr("activity"));
            entity.setTitle(dto.getTitle());
            entity.setContent(dto.getContent());
            entity.setHostParty(dto.getHostParty());
            entity.setCommitteesNumber(dto.getCommitteesNumber());
            entity.setActivityStartTime(MyDateUtil.dateString3Date(dto.getActivityStartTimeStr()));
            entity.setActivityEndTime(MyDateUtil.dateString3Date(dto.getActivityEndTimeStr()));
            entity.setVotingStartTime(MyDateUtil.dateString3Date(dto.getVotingStartTimeStr()));
            entity.setVotingEndTime(MyDateUtil.dateString3Date(dto.getVotingEndTimeStr()));
            entity.setActivityStatus(ActivityStatusEnum.UNPUBLISHED.getType());
            entity.setCreateDate(date);
            entity.setUpdateDate(date);
            entity.setWhetherValid(WhetherValidEnum.VALID.getType());
            activityRepository.save(entity);
            return Result.createSimpleSuccessResult();

        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    public Result findActivityEntityById(ActivityDto dto){
        try {
            ActivityEntity entity = activityRepository.findActivityEntityById(dto.getId());
            if (entity!=null){
                ActivityDto newsDto = MyBeanUtils.copyPropertiesAndResTarget(entity, ActivityDto::new, d -> {
                    d.setCreateDateStr(MyDateUtil.getDateAndTime(entity.getCreateDate()));
                    d.setUpdateDateStr(MyDateUtil.getDateAndTime(entity.getUpdateDate()));
                    d.setWhetherValidStr(WhetherValidEnum.getTypesName(entity.getWhetherValid()));
                    d.setActivityStartTimeStr(MyDateUtil.getDateAndTime(entity.getActivityStartTime()));
                    d.setActivityEndTimeStr(MyDateUtil.getDateAndTime(entity.getActivityEndTime()));
                    d.setVotingStartTimeStr(MyDateUtil.getDateAndTime(entity.getVotingStartTime()));
                    d.setVotingEndTimeStr(MyDateUtil.getDateAndTime(entity.getVotingEndTime()));
                    d.setActivityStatusStr(ActivityStatusEnum.getTypesName(entity.getActivityStatus()));
                });
                return new Result().setSuccess(newsDto);
            }else {
                return Result.createSimpleFailResult();
            }
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    public Result<List<ManageActivityIndexOutDto>> activityIndex(ManageActivityIndexIntoDto indexIntoDto){
        try {
            //根据时间倒序
            Sort sort = Sort.by(Sort.Direction.DESC,"createDate");
            //页数与每页大小
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE,sort);
            //条件
            Page<ActivityEntity> page = activityRepository.findAll(new Specification<ActivityEntity>() {
                @Override
                public Predicate toPredicate(Root<ActivityEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                    ArrayList<Predicate> predicateList = new ArrayList<>();
                    if (StringUtils.isNotBlank(indexIntoDto.getTitle())){
                        predicateList.add(criteriaBuilder.like(root.get("title"),"%"+indexIntoDto.getTitle()+"%"));
                    }
                    if (StringUtils.isNotBlank(indexIntoDto.getHostParty())){
                        predicateList.add(criteriaBuilder.equal(root.get("hostParty"),indexIntoDto.getHostParty()));
                    }
                    if (StringUtils.isNotBlank(indexIntoDto.getActivityStatus())){
                        predicateList.add(criteriaBuilder.equal(root.get("activityStatus"),indexIntoDto.getActivityStatus()));
                    }
                    getBaseIntoDtoPredicate(predicateList,(BaseIntoDto) indexIntoDto,root,criteriaQuery,criteriaBuilder);
                    return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
                }
            }, pageable);
            List<ManageActivityIndexOutDto> manageActivityIndexOutDtoList = page.getContent().stream().map(e -> {
                ManageActivityIndexOutDto outDto = MyBeanUtils.copyPropertiesAndResTarget(e, ManageActivityIndexOutDto::new);
                outDto.setCreateDateStr(MyDateUtil.getDateAndTime(e.getCreateDate()));
                outDto.setUpdateDateStr(MyDateUtil.getDateAndTime(e.getUpdateDate()));
                outDto.setActivityStatusStr(ActivityStatusEnum.getTypesName(e.getActivityStatus()));
                outDto.setActivityStartTimeStr(MyDateUtil.getDateAndTime(e.getActivityStartTime()));
                outDto.setActivityEndTimeStr(MyDateUtil.getDateAndTime(e.getActivityEndTime()));
                outDto.setVotingEndTimeStr(MyDateUtil.getDateAndTime(e.getVotingEndTime()));
                outDto.setVotingStartTimeStr(MyDateUtil.getDateAndTime(e.getVotingStartTime()));
                outDto.setWhetherValidStr(WhetherValidEnum.getTypesName(e.getWhetherValid()));
                return outDto;
            }).collect(Collectors.toList());
            return new Result<List<ManageActivityIndexOutDto>>().setSuccess(manageActivityIndexOutDtoList).setCount(page.getTotalElements());
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    public Result<List<EnrollDto>> findEnrollEnitiesByActivityIdAndAuditStatus(EnrollDto enrollDto){
        try {
            List<EnrollEntity> enrollEntityList = enrollRepository.findEnrollEntitiesByActivityIdAndAuditStatusOrderByVoteNumberDesc(enrollDto.getActivityId(), AuditStatusEnum.SUCCESS.getType());
            if (enrollEntityList!=null&&enrollEntityList.size()>0){
                List<EnrollDto> enrollDtoList = enrollEntityList.stream().map(e -> {
                    return MyBeanUtils.copyPropertiesAndResTarget(e, EnrollDto::new, d -> {
                        d.setCreateDateStr(MyDateUtil.getDateAndTime(e.getCreateDate()));
                        d.setUpdateDateStr(MyDateUtil.getDateAndTime(e.getUpdateDate()));
                        d.setAuditStatusStr(AuditStatusEnum.getTypesName(e.getAuditStatus()));
                        d.setWhetherValidStr(WhetherValidEnum.getTypesName(e.getWhetherValid()));
                    });
                }).collect(Collectors.toList());
                return new Result<List<EnrollDto>>().setSuccess(enrollDtoList);
            }else return new Result<>().setCustomMessage("数据为空");
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    public Result<List<EnrollDto>> findEnrollEntitiesByActivityIdOrderByCreateDateDesc(EnrollDto enrollDto){
        try {
            List<EnrollEntity> enrollEntityList = enrollRepository.findEnrollEntitiesByActivityIdOrderByCreateDateDesc(enrollDto.getActivityId());
            if (enrollEntityList!=null&&enrollEntityList.size()>0){
                List<EnrollDto> enrollDtoList = enrollEntityList.stream().map(e -> {
                    return MyBeanUtils.copyPropertiesAndResTarget(e, EnrollDto::new, d -> {
                        d.setCreateDateStr(MyDateUtil.getDateAndTime(e.getCreateDate()));
                        d.setUpdateDateStr(MyDateUtil.getDateAndTime(e.getUpdateDate()));
                        d.setAuditStatusStr(AuditStatusEnum.getTypesName(e.getAuditStatus()));
                        d.setWhetherValidStr(WhetherValidEnum.getTypesName(e.getWhetherValid()));
                    });
                }).collect(Collectors.toList());
                return new Result<List<EnrollDto>>().setSuccess(enrollDtoList);
            }else return new Result<>().setCustomMessage("数据为空");
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    /**
     * activityId（活动id）
     * @param indexIntoDto
     * @return
     */
    public Result<List<ManageEnrollIndexOutDto>> enrollEntityResult(ManageEnrollIndexIntoDto indexIntoDto){
        try {
            ActivityEntity entity = activityRepository.findActivityEntityById(indexIntoDto.getActivityId());
            if (entity!=null){
                //根据时间倒序
                Sort sort = Sort.by(Sort.Direction.DESC,"voteNumber");
                //页数与每页大小
                Pageable pageable = PageRequest.of(0,entity.getCommitteesNumber(),sort);
                //条件
                Page<EnrollEntity> page = enrollRepository.findAll(new Specification<EnrollEntity>() {
                    @Override
                    public Predicate toPredicate(Root<EnrollEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                        ArrayList<Predicate> predicateList = new ArrayList<>();
                        predicateList.add(criteriaBuilder.equal(root.get("activityId"),indexIntoDto.getActivityId()));
                        predicateList.add(criteriaBuilder.equal(root.get("auditStatus"),AuditStatusEnum.SUCCESS.getType()));
                        getBaseIntoDtoPredicate1(predicateList,(BaseIntoDto) indexIntoDto,root,criteriaQuery,criteriaBuilder);
                        return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
                    }
                }, pageable);
                List<ManageEnrollIndexOutDto> manageEnrollIndexOutDtoList = page.getContent().stream().map(e -> {
                    ManageEnrollIndexOutDto outDto = MyBeanUtils.copyPropertiesAndResTarget(e, ManageEnrollIndexOutDto::new);
                    outDto.setCreateDateStr(MyDateUtil.getDateAndTime(e.getCreateDate()));
                    outDto.setUpdateDateStr(MyDateUtil.getDateAndTime(e.getUpdateDate()));
                    outDto.setAuditStatusStr(AuditStatusEnum.getTypesName(e.getAuditStatus()));
                    outDto.setWhetherValidStr(WhetherValidEnum.getTypesName(e.getWhetherValid()));
                    return outDto;
                }).collect(Collectors.toList());
                return new Result<List<ManageEnrollIndexOutDto>>().setSuccess(manageEnrollIndexOutDtoList).setCount(page.getTotalElements());
            }else return Result.createSimpleFailResult();
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }


    public Result<EnrollDto> findEnrollEnityById(EnrollDto enrollDto){
        try {
            EnrollEntity entity = enrollRepository.findEnrollEntityById(enrollDto.getId());
            if (entity!=null){
                EnrollDto dto = MyBeanUtils.copyPropertiesAndResTarget(entity, EnrollDto::new, d -> {
                    d.setCreateDateStr(MyDateUtil.getDateAndTime(entity.getCreateDate()));
                    d.setUpdateDateStr(MyDateUtil.getDateAndTime(entity.getUpdateDate()));
                    d.setAuditStatusStr(AuditStatusEnum.getTypesName(entity.getAuditStatus()));
                    d.setWhetherValidStr(WhetherValidEnum.getTypesName(entity.getWhetherValid()));
                });
                return new Result<EnrollDto>().setSuccess(dto);
            }else return Result.createSimpleFailResult();
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    public Result<List<VoteDto>> findAll(VoteDto voteDto){
        try {
            List<VoteEntity> voteEntityList = voteRepository.findVoteEntitiesByEnrollId(voteDto.getEnrollId());
            if (voteEntityList!=null){
                List<VoteDto> voteDtoList = voteEntityList.stream().map(e -> {
                    VoteDto voteDto1 = MyBeanUtils.copyPropertiesAndResTarget(e, VoteDto::new, d -> {
                        d.setCreateDateStr(MyDateUtil.getDateAndTime(e.getCreateDate()));
                        d.setUpdateDateStr(MyDateUtil.getDateAndTime(e.getUpdateDate()));
                        d.setWhetherValidStr(WhetherValidEnum.getTypesName(e.getWhetherValid()));
                    });
                    return voteDto1;
                }).collect(Collectors.toList());
                return new Result<List<VoteDto>>().setSuccess(voteDtoList);
            }else  return Result.createSimpleFailResult();
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }

    }


    private void getBaseIntoDtoPredicate(List<Predicate> predicateList, BaseIntoDto intoDto, Root<ActivityEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder){
        if (StringUtils.isNotBlank(intoDto.getWhetherValid())){
            predicateList.add(criteriaBuilder.equal(root.get("whetherValid"),intoDto.getWhetherValid()));
        }else {
            predicateList.add(criteriaBuilder.equal(root.get("whetherValid"), WhetherValidEnum.VALID.getType()));
        }
        if (StringUtils.isNotBlank(intoDto.getStartCreateDateStr())){
            predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createDate"),intoDto.getStartCreateDateStr()));
        }
        if (StringUtils.isNotBlank(intoDto.getStartCreateDateStr())){
            predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("createDate"),intoDto.getStartCreateDateStr()));
        }
        if (StringUtils.isNotBlank(intoDto.getStartUpdateDateStr())){
            predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("updateDate"),intoDto.getStartUpdateDateStr()));
        }
        if (StringUtils.isNotBlank(intoDto.getStartUpdateDateStr())){
            predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("updateDate"),intoDto.getStartUpdateDateStr()));
        }
    }

    private void getBaseIntoDtoPredicate1(List<Predicate> predicateList, BaseIntoDto intoDto, Root<EnrollEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder){
        if (StringUtils.isNotBlank(intoDto.getWhetherValid())){
            predicateList.add(criteriaBuilder.equal(root.get("whetherValid"),intoDto.getWhetherValid()));
        }else {
            predicateList.add(criteriaBuilder.equal(root.get("whetherValid"), WhetherValidEnum.VALID.getType()));
        }
        if (StringUtils.isNotBlank(intoDto.getStartCreateDateStr())){
            predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createDate"),intoDto.getStartCreateDateStr()));
        }
        if (StringUtils.isNotBlank(intoDto.getStartCreateDateStr())){
            predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("createDate"),intoDto.getStartCreateDateStr()));
        }
        if (StringUtils.isNotBlank(intoDto.getStartUpdateDateStr())){
            predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("updateDate"),intoDto.getStartUpdateDateStr()));
        }
        if (StringUtils.isNotBlank(intoDto.getStartUpdateDateStr())){
            predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("updateDate"),intoDto.getStartUpdateDateStr()));
        }
    }


}
