package com.navercorp.chat.mvc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.navercorp.chat.mvc.model.UserInfo;

@Component
@ComponentScan
public class DataBaseController {
	
//	@Autowired
//	private MySqlConfig mysql;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public DataBaseController()  {
		System.out.println("DBC Created");
	}
	
	public Boolean signup(UserInfo user) throws Exception {
		System.out.println("DBC.signup");
		List<Map<String, Object>> queryForList = jdbcTemplate.queryForList("SELECT * FROM CHAT_USERS_TB");
		System.out.println(queryForList);
		
//		mysql.query("SELET * FROM CHAT_USERS_TB");
		
		return true;
	}
}
