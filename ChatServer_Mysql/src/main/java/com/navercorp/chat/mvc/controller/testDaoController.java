package com.navercorp.chat.mvc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.navercorp.chat.dao.testDao;

@Component
@ComponentScan
public class testDaoController {
	@Autowired
	private testDao testdao;
	
	public testDaoController() {
		System.out.println("testDaoController 생성자");
	}
	
	public void run() {
		System.out.println("testDao 이용하여 달립니다.");
		testdao.exec();
	}
}
