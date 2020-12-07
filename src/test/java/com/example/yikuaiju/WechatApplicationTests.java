package com.example.yikuaiju;

import com.example.yikuaiju.bean.Ykj_user;
import com.example.yikuaiju.service.ICommonService;
import com.example.yikuaiju.service.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
class WechatApplicationTests {

	@Autowired
	IUserService userService;


	@Autowired
	ICommonService commonService;

	@Test
	void contextLoads() {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("nickName","李四");
		params.put("avatarUrl","6995");
		params.put("openid","123456789");
		params.put("sessionkey","123456789");
		try {
			Ykj_user user = commonService.addOneRecord(Ykj_user.class, params);
//			Ykj_user user = commonService.update(Ykj_user.class,params);
			System.out.println("该用户ID为：");
			System.out.println(user.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
