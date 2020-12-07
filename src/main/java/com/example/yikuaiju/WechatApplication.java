package com.example.yikuaiju;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling
@EnableTransactionManagement
@SpringBootApplication
@MapperScan("com.example.yikuaiju.mapper")
public class WechatApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {

		SpringApplication.run(WechatApplication.class, args);
	}

	/*部署war包时开启 */
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		// 注意这里要指向原先用main方法执行的Application启动类
		return builder.sources(WechatApplication.class);
	}

}
