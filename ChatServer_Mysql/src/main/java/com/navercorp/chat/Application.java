package com.navercorp.chat;

import java.util.Date;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.context.WebApplicationContextServletContextAwareProcessor;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.navercorp.chat.mvc.model.UserInfo;
import com.navercorp.chat.service.JwtTokenUtil;

@SpringBootApplication(scanBasePackages = { "com.navercorp.chat.mvc.controller" })
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class Application extends WebApplicationContextServletContextAwareProcessor {

	String hello = "Hello";

	public final static String jwtSecret = "KR19815";

	public Application(ConfigurableWebApplicationContext webApplicationContext) {
		super(webApplicationContext);
		// TODO Auto-generated constructor stub
	}

	private List<UserInfo> users;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

		//Before is Token Example.
//		UserInfo user = new UserInfo();
//		user.setUserId("chankyu");
//		user.setPassword("password");
//		
//		JwtTokenUtil jwt = new JwtTokenUtil();
//		String token = jwt.generateToken(user);
//		System.out.println(token);
//		
//		Date expirationDate = jwt.getExpirationDateFromToken(token);
//		System.out.println(expirationDate);
//		
//		String userId = jwt.getUserIdFromToken(token);
//		System.out.println(userId);
//		
//		Boolean validate = jwt.validateToken(token, user);
//		System.out.println(validate);
	}
}
