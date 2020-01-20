package com.navercorp.chat.dao;

import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class MySqlConfig{
	
	public MySqlConfig() {
		System.out.println("MySqlConfig Created");
	}
	
	@Bean
	public JdbcTemplate initJdbcTemplate() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
		properties.setProperty("username", "pgt");
		properties.setProperty("password", "PASS1234");
		properties.setProperty("url",
				"jdbc:mysql://10.105.194.81:13306/pgtDB?autoReconnect=true&useTimezone=true&serverTimezone=UTC");

		BasicDataSource createDataSource = BasicDataSourceFactory.createDataSource(properties);
		return new JdbcTemplate(createDataSource);
	}
	
	public void query(String str){
		
	}
}
