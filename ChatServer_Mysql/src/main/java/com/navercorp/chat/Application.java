package com.navercorp.chat;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = { "com.navercorp.chat.mvc.controller" })
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan
public class Application implements ApplicationListener<ApplicationStartedEvent> {

	@Autowired
	ApplicationDatabase appDB;
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		// TODO Auto-generated method stub
		appDB.init();

//		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//		System.out.println(timestamp);
//		System.out.println(currentTimeNanos());
	}
	
	
}
