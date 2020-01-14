package com.navercorp.chat;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.context.WebApplicationContextServletContextAwareProcessor;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.navercorp.chat.mvc.model.UserInfo;

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

//		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	}
}
