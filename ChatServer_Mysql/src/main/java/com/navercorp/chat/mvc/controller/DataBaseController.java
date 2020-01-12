package com.navercorp.chat.mvc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.navercorp.chat.mvc.model.UserInfo;

//@Component

public class DataBaseController {
	
	@Autowired
	private JdbcTemplate jdbcTemplate; 
	
	public Boolean signIn(UserInfo user) throws Exception {
//		List<Map<String, Object>> queryForList = jdbcTemplate.queryForList("SELECT * FROM CHAT_USERS_TB");
//		System.out.println(queryForList);
		
		return true;
	}
}
