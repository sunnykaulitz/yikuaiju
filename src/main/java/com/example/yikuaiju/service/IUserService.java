package com.example.yikuaiju.service;

import com.example.yikuaiju.bean.Ykj_user;
import com.example.yikuaiju.bean.common.WechatUserInfo;

import java.io.IOException;

public interface IUserService {

    public Ykj_user loginSimple(String code, WechatUserInfo userInfo, Integer source) throws Exception;

    public Ykj_user getUserByUnionkey(String unionkey);

     /*
      * @author lifei
      * @Params
      * @return
      * @description: 获取随机头像
      * @date 2020/11/21 21:09
      */
    public String getImgUrl() throws IOException;
}
