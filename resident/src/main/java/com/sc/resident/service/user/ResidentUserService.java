package com.sc.resident.service.user;

import com.alibaba.fastjson.JSONObject;
import com.sc.base.dto.user.RegisterDto;
import com.sc.base.dto.user.ResidentUserDto;
import com.sc.base.dto.user.UpdateUserDto;
import com.sc.base.entity.user.ResidentRegistrationEntity;
import com.sc.base.entity.user.ResidentUserEntity;
import com.sc.base.enums.RoleEnum;
import com.sc.base.enums.WhetherValidEnum;
import com.sc.base.repository.user.ResidentRegistrationRepository;
import com.sc.base.repository.user.ResidentUserRepository;
import myJson.MyJsonUtil;
import myString.MyStringUtils;
import mydate.MyDateUtil;
import myspringbean.MyBeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import vo.Result;
import weChat.entity.WeChatEntity;
import weChat.util.GetWeChatInfoUtil;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResidentUserService {


    @Autowired
    private ResidentUserRepository residentUserRepository;
    @Autowired
    private ResidentRegistrationRepository residentRegistrationRepository;
    @Value("${wechat.appid}")
    private String appId;
    @Value("${wechat.appsecret}")
    private String appSecret;

    public ResidentUserEntity findResidentUserEntityById(String id){
        return residentUserRepository.findResidentUserEntityById(id);
    }

    //用户登录方式
    // 1.点击登录，获取用户信息注册
    // 2.已注册，自动登录
    /**
     * 方式1
     * 1.获取openId{
     *     需要参数 appId,appSecret,code
     * }
     * 2.判断数据库是否存在openId，存在，登录{
     *   返回userEntity
     * }，
     * 不存在,返回失败
     */
    public Result<ResidentUserEntity> login(WeChatEntity weChatEntity){
        //查询用户openId
        weChatEntity.setAppid(appId);
        weChatEntity.setAppSecret(appSecret);
        Result<String> result1 = GetWeChatInfoUtil.getOpenIdAndSessionKey(weChatEntity);
        if (!result1.isSuccess()) return Result.createNewResult(result1);
        JSONObject jsonObject = JSONObject.parseObject(result1.getData());
        weChatEntity.setOpenId((String) jsonObject.get("openid"));
        //根据openId查询是否存在改用户
        ResidentUserEntity residentUserEntity = residentUserRepository.findResidentUserEntityByOpenId(weChatEntity.getOpenId());
        if (residentUserEntity !=null && StringUtils.isNotBlank(residentUserEntity.getId())) return new Result().setSuccess(residentUserEntity);
        return Result.createSimpleFailResult();
    }

    /**
     * //查询是否拥有该居民，是，注册,返回成功，不是，返回失败;
     * 跳转到注册页面{
     *  1.注册时从居民记录表中审核信息，如果不存在，返回失败
     *  2.如果存在则注册信息{
     *      获取用户信息需要参数 encryptedData,iv
     *  }
     * }
     * @param registerDto
     * @return
     */
    public Result register(RegisterDto registerDto){
        //判断是否存在该用户
        ResidentRegistrationEntity residentRegistrationEntity = residentRegistrationRepository.findResidentRegistrationEntityByPhoneNumber(registerDto.getPhoneNumber());
        if (residentRegistrationEntity==null&&StringUtils.isBlank(residentRegistrationEntity.getId()))
            return Result.createSimpleFailResult();

        ResidentUserEntity residentUserEntity = new ResidentUserEntity();
        residentUserEntity.setId(MyStringUtils.getIdDateStr("residentUser"));
        residentUserEntity.setIdNumber(residentRegistrationEntity.getIdNumber());
        residentUserEntity.setActualName(residentRegistrationEntity.getActualName());
        residentUserEntity.setUnit(registerDto.getUnit());
        residentUserEntity.setFloor(registerDto.getFloor());
        residentUserEntity.setDoor(registerDto.getDoor());
        residentUserEntity.setAddress(registerDto.getUnit()+registerDto.getFloor()+registerDto.getDoor());
        //创建roleEnum(居民，委员会成员)
        residentUserEntity.setRole(RoleEnum.RESIDENT.getType());
        residentUserEntity.setUserAuditId(residentRegistrationEntity.getId());
        residentUserEntity.setPhoneNumber(residentRegistrationEntity.getPhoneNumber());
        //获取微信用户信息
        WeChatEntity weChatEntity = new WeChatEntity();
        weChatEntity.setAppid(appId);
        weChatEntity.setAppSecret(appSecret);
        weChatEntity.setCode(registerDto.getCode());
        weChatEntity.setEncryptedData(registerDto.getEncryptedData());
        weChatEntity.setIv(registerDto.getIv());
        Result<String> result = GetWeChatInfoUtil.getUserInfo(weChatEntity);
        /*
        {"openId":"oojUN5JWGwgsLgdwoPXI5LmTKY6U",
        "nickName":"源","gender":0,
        "language":"zh_CN",
        "city":"","province":"","country":"",
        "avatarUrl":"https://wx.qlogo.cn/mmopen/vi_32/wjSpv6ibG6ribZU7eFAzYUQYgcZAOqTmkWTpXcQicBUc1ry1rYXOSVlaEoI3Km2j0LyvB3HRZeXxgYnCyIzFSFtibw/132",
        "watermark":{"timestamp":1585462984,"appid":"wx358e0da133141282"}}
         */
        if (StringUtils.isNotBlank(result.getData())){
            JSONObject jsonObject = JSONObject.parseObject(result.getData());
            residentUserEntity.setOpenId((String) jsonObject.get("openId"));
            residentUserEntity.setUsername((String) jsonObject.get("nickName"));
            residentUserEntity.setHeadPictureUrl((String) jsonObject.get("avatarUrl"));
            addUserEntity(residentUserEntity);
            residentUserEntity=residentUserRepository.findResidentUserEntityByOpenId((String) jsonObject.get("openId"));
        }else{
            return Result.createNewResult(result);
        }
        return new Result().setSuccess(residentUserEntity);
    }

    /**
     * 方式2
     * 1.获取openId
     * 2.判断数据库是否存在openId，存在，自动登录
     */
    public Result<ResidentUserEntity> automaticLogin(WeChatEntity weChatEntity){
        weChatEntity.setAppid(appId);
        weChatEntity.setAppSecret(appSecret);
        WeChatEntity weChatEntity1 = MyJsonUtil.jsonToPojo(GetWeChatInfoUtil.getOpenIdAndSessionKey(weChatEntity).getData(), WeChatEntity.class);
        ResidentUserEntity residentUserEntity = residentUserRepository.findResidentUserEntityByOpenId(weChatEntity1.getOpenId());
        if (StringUtils.isNotBlank(residentUserEntity.getId())) return new Result().setSuccess(residentUserEntity);
        return Result.createSimpleFailResult();
    }

    public void addUserEntity(ResidentUserEntity residentUserEntity){
        residentUserEntity.setCreateDate(new Date());
        residentUserEntity.setUpdateDate(new Date());
        residentUserEntity.setWhetherValid(WhetherValidEnum.VALID.getType());
        residentUserRepository.saveAndFlush(residentUserEntity);
    }

    /**
     * 查询所有居民用户
     * @return
     */
    public Result<List<ResidentUserDto>> findAll(@RequestBody ResidentUserDto dto){
        try {
            List<ResidentUserEntity> residentUserEntityList = residentUserRepository.findResidentUserEntitiesByWhetherValid(WhetherValidEnum.VALID.getType());
            List<ResidentUserDto> residentUserDtoList = residentUserEntityList.stream().filter(e->{
                return !e.getId().equals(dto.getId());
            }).map(e -> {
                ResidentUserDto residentUserDto = MyBeanUtils.copyPropertiesAndResTarget(e, ResidentUserDto::new);
                residentUserDto.setCreateDateStr(MyDateUtil.getDateAndTime(e.getCreateDate()));
                residentUserDto.setUpdateDateStr(MyDateUtil.getDateAndTime(e.getUpdateDate()));
                residentUserDto.setRoleStr(RoleEnum.getTypesName(e.getRole()));
                residentUserDto.setWhetherValidStr(WhetherValidEnum.getTypesName(e.getWhetherValid()));
                return residentUserDto;
            }).collect(Collectors.toList());
            return new Result<List<ResidentUserDto>>().setSuccess(residentUserDtoList);
        }catch (Exception e){
            e.printStackTrace();
            return Result.createSystemErrorResult();
        }
    }

    public Result update(UpdateUserDto updateUserDto){
        ResidentUserEntity residentUserEntity=residentUserRepository.findResidentUserEntityByOpenId(updateUserDto.getOpenId());
        if(updateUserDto.getChange().equals("unit")){
            residentUserEntity.setUnit(updateUserDto.getValue());
            residentUserEntity.setAddress(residentUserEntity.getUnit()+residentUserEntity.getFloor()+residentUserEntity.getDoor());
        }else if(updateUserDto.getChange().equals("floor")){
            residentUserEntity.setFloor(updateUserDto.getValue());
            residentUserEntity.setAddress(residentUserEntity.getUnit()+residentUserEntity.getFloor()+residentUserEntity.getDoor());
        }else if(updateUserDto.getChange().equals("door")){
            residentUserEntity.setDoor(updateUserDto.getValue());
            residentUserEntity.setAddress(residentUserEntity.getUnit()+residentUserEntity.getFloor()+residentUserEntity.getDoor());
        }else if(updateUserDto.getChange().equals("actualName")){
            residentUserEntity.setActualName(updateUserDto.getValue());
            residentUserEntity.setAddress(residentUserEntity.getUnit()+residentUserEntity.getFloor()+residentUserEntity.getDoor());
        }else if(updateUserDto.getChange().equals("phoneNumber")){
            residentUserEntity.setPhoneNumber(updateUserDto.getValue());
            residentUserEntity.setAddress(residentUserEntity.getUnit()+residentUserEntity.getFloor()+residentUserEntity.getDoor());
        }
        residentUserRepository.saveAndFlush(residentUserEntity);
        return new Result().setSuccess(residentUserEntity);
    }
}
