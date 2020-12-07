package com.example.yikuaiju.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.example.yikuaiju.bean.Ykj_user;
import com.example.yikuaiju.bean.Ykj_usersource;
import com.example.yikuaiju.bean.common.CommonBean;
import com.example.yikuaiju.bean.common.WechatUserInfo;
import com.example.yikuaiju.service.ICommonService;
import com.example.yikuaiju.service.IUserService;
import com.example.yikuaiju.util.BeanMapConvertUtil;
import com.example.yikuaiju.util.WeChatUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements IUserService {

    @Autowired
    private ICommonService commonService;

     /*
      * @author lifei
      * @Params source 小程序搜索1\活动扫码2
      * @return
      * @description: 微信授权用户，注册到数据库，返回unionkey
      * @date 2020/11/16 22:26
      */
    @Override
    public Ykj_user loginSimple(String code, WechatUserInfo userInfo, Integer source) throws Exception {
        if(code == null){
            throw new Exception("用户code参数不能为空");
        }
        if(userInfo== null)
            throw new Exception("用户userInfo参数不能为空");
        if(userInfo.getNickName() == null){
            throw new Exception("用户nickName参数不能为空");
        }
        if(userInfo.getAvatarUrl() == null){
            throw new Exception("用户avatarUrl参数不能为空");
        }
        if(source == null){
            throw new Exception("用户来源source参数不能为空");
        }
        HashMap<String, Object> paramMap = new HashMap<>();
        if(userInfo!= null)
            paramMap = BeanMapConvertUtil.beanToMap(userInfo);
        Ykj_user user = new Ykj_user();
        /*1、从微信服务端获取用户的openid、sessionkey*/
        //获取 sessionkey 和 openId
        JSONObject res = WeChatUtil.getWxUserInfo(code);
        /*2、查询自己的数据库用户表，是否存在这个openid,如果存在则修改sessionkey，如果不存在则创建一条用户数据*/
        String sessionkey = res.getString("session_key");
        String openid = res.getString("openid");
        //Base64加密sessionkey和openid返回给前端作为session
        String unionkey = Base64.getEncoder().encodeToString((sessionkey+openid).getBytes());
        paramMap.put("openid", openid);
        paramMap.put("sessionkey", sessionkey);
        paramMap.put("unionkey", unionkey);
        paramMap.put("modifytime", new Date());
        List<Map<String, Object>> users = commonService.select("select *from ykj_user where openid=:openid ", paramMap);
        if (users != null && users.size() > 0) {
            //用户已经存在，更新sessionkey、modifytime
            paramMap.put("id", (Integer) users.get(0).get("id"));
            user = commonService.update(Ykj_user.class, paramMap);
        } else {
            //用户不存在，创建数据
            if(source != null)
                paramMap.put("source", source);
            else
                paramMap.put("source", Ykj_usersource.appsearch);   //小程序搜索
            paramMap.put("creationtime", new Date());
            user = commonService.addOneRecord(Ykj_user.class, paramMap);
        }
        /*3、Base64加密sessionkey和openid返回给前端作为session*/
        return user;
    }

    @Override
    public Ykj_user getUserByUnionkey(String unionkey) {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("unionkey",unionkey);
        Ykj_user user = commonService.selectOne(Ykj_user.class, "select *from ykj_user where unionkey=:unionkey ", paramMap);
        return user;
    }

    @Override
    public String getImgUrl() throws IOException {
        String url = "http://api.btstu.cn/sjtx/api.php?method=mobile&lx=c1&format=json";
        HttpClient client = HttpClientBuilder.create().build();//构建一个Client
        HttpGet get = new HttpGet(url.toString());    //构建一个GET请求
        HttpResponse response = client.execute(get);//提交GET请求
        org.apache.http.HttpEntity result = response.getEntity();//拿到返回的HttpResponse的"实体"
        String content = EntityUtils.toString(result);
        System.out.println(content);//打印返回的信息
        JSONObject res = JSONObject.parseObject(content);//把信息封装为json
        return res.getString("imgurl");
    }
}
