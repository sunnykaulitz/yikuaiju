package com.example.yikuaiju.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.yikuaiju.bean.Ykj_user;
import com.example.yikuaiju.bean.common.CommonBean;
import com.example.yikuaiju.bean.common.WeChatAppLoginReq;
import com.example.yikuaiju.bean.common.WechatUserInfo;
import com.example.yikuaiju.service.ICommonService;
import com.example.yikuaiju.service.IUserService;
import com.example.yikuaiju.util.Algorithm;
import com.example.yikuaiju.util.WeChatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.util.*;


@Controller
@RequestMapping("user")
@Transactional
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private ICommonService commonService;
    @Autowired
    private IUserService userService;


    //获取openid
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean loginSimple(String code, WechatUserInfo userInfo, Integer source) {
        CommonBean commonBean = new CommonBean();
        if(code != null) {
            try {
                Ykj_user user = userService.loginSimple(code, userInfo, source);
                /*获取session_key和openid，并且创建用户，Base64加密session_key和openid返回给前端作为session*/
                commonBean.setSuccess(true);
                commonBean.setMessage("获取session成功");
                commonBean.setData(user);
                return commonBean;
            } catch (Exception e) {
                logger.error(e.getMessage());
                // 手动回滚事物
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                commonBean.setSuccess(false);
                commonBean.setMessage(e.getMessage());
                return commonBean;
            }
        }else {
            logger.error("code参数不能为空");
            commonBean.setSuccess(false);
            commonBean.setMessage("code参数不能为空");
            return commonBean;
        }
    }


    /**
     * 登录并验证：验证数据完整性
     * 1.前端调用wx.login()获取code值
     *
     * 2.前端通过调用wx.getUserInfo获取iv、rawData、signature、encryptedData等加密数据，传递给后端
     *
     * 3.服务器通过code请求api换回session_key和openid
     *
     * 4.服务器通过前端给的rawData 加获取的session_key使用sha1加密，计算出signature1
     *
     * 5.比对前端传的signature和自己算出来的signature1是否一致（防止数据不一致）
     *
     * 6.用AES算法解密encryptedData里的敏感数据
     *
     * 7.拿着敏感数据后做自己的逻辑
     *
     * 8.通知前端登陆成功
     * @param req
     * @return
     */
    @RequestMapping(value = "/loginAndSign",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean loginAndSign(WeChatAppLoginReq req){
        CommonBean commonBean = new CommonBean();
        try {
            //获取 session_key 和 openId
            JSONObject res = WeChatUtil.getWxUserInfo(req.getCode());
            String session_key = res.getString("session_key");
            String openid = res.getString("openid");
            String str = req.getRawData() + session_key;
            String signature = Algorithm.useSHA1(str);//用SHA-1算法计算签名

            if (!signature.equals(req.getSignature())) {
                logger.info(" req signature=" + req.getSignature() + "\n\t\n" + " java signature=" + signature);
                throw new Exception("签名无法解析，或被篡改，无法登录");
            }

            byte[] resultByte = null;
            try {//解密敏感数据
                resultByte = WeChatUtil.decrypt(Base64.getDecoder().decode(req.getEncryptedData()),
                        Base64.getDecoder().decode(session_key),
                        Base64.getDecoder().decode(req.getIv()));
            } catch (Exception e) {
                throw new Exception("数据无法解析！");
            }

            if (null != resultByte && resultByte.length > 0) {
                try {
                    String userInfoStr = new String(resultByte, "UTF-8");
                    Map<String,Object> user = res.parseObject(userInfoStr, Map.class);
                    commonBean.setSuccess(true);
                    commonBean.setData(user);
                    commonBean.setMessage("授权成功");
                    return commonBean;
                } catch (UnsupportedEncodingException e) {
                    logger.error("对象转换错误", e);
                    // 手动回滚事物
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    commonBean.setSuccess(false);
                    commonBean.setMessage("对象转换错误,"+ e.getMessage());
                    return commonBean;
                }
            }
            return null;
        }catch (Exception e){
            logger.error("对象转换错误", e);
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage("对象转换错误,"+ e.getMessage());
            return commonBean;
        }
    }


    //获取openid
    @RequestMapping(value = "/markUser",method = RequestMethod.POST)
    @ResponseBody   //代表返回json
    public CommonBean markUser(String userid, String mark) {
        CommonBean commonBean = new CommonBean();
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("id",userid);
            params.put("mark",mark);
            commonService.update(Ykj_user.class,params);
            commonBean.setSuccess(true);
            commonBean.setMessage("标记成功");
            return commonBean;
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            commonBean.setSuccess(false);
            commonBean.setMessage(e.getMessage());
            return commonBean;
        }
    }

}
