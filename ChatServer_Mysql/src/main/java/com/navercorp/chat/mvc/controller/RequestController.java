package com.navercorp.chat.mvc.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.navercorp.chat.mvc.model.UserInfo;

@RestController
@Configuration
@ComponentScan("com.navercorp.chat.dao")
@ComponentScan("com.navercorp.chat.mvc.controller")
public class RequestController {
	private static final Logger LOG = Logger.getLogger(RequestController.class.getName());

	enum RequestType {
		POST, PUT, GET, DELETE
	}

	enum ResponseCode {
		SUCCESS, FAIL
	}

	public Map<String, String> ResponseMapping(RequestType rt, ResponseCode rc) {
		Map<String, String> response = new HashMap<String, String>();

		switch (rt) {
		case POST:
			response.put("RequestType", "Post");
			break;
		case PUT:
			response.put("RequestType", "PUT");
			break;
		case GET:
			response.put("RequestType", "GET");
			break;
		case DELETE:
			response.put("RequestType", "DELETE");
			break;
		default:
			response.put("RequestType", "None");
		}

		switch (rc) {
		case SUCCESS:
			response.put("ResponseCode", "SUCCESS");
			break;
		case FAIL:
			response.put("ResponseCode", "FAIL");
			break;
		default:
			response.put("ResponseCode", "None");
		}

		return response;
	}

//POST======================================================================
	// 회원가입
	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public Map<String, String> signup(UserInfo user, Model model) {
		LOG.info("[RequestMapping()] POST : /signIn START");
		// signup();
		LOG.info("[RequestMapping()] POST : /signIn END");
		return ResponseMapping(RequestType.POST, ResponseCode.SUCCESS);
	}

	// 로그인
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Map<String, String> login(UserInfo user, Model model) {
		LOG.info("[RequestMapping()] POST : /login START");
		// login();
		LOG.info("[RequestMapping()] POST : /login END");
		return ResponseMapping(RequestType.POST, ResponseCode.SUCCESS);
	}

//DELETE======================================================================
	// 회원탈퇴
	@RequestMapping(value = "/user", method = RequestMethod.DELETE)
	public Map<String, String> signout(UserInfo user, Model model) {
		LOG.info("[RequestMapping()] DELETE : /signOut START");
		// signout();
		LOG.info("[RequestMapping()] DELETE : /signOut END");
		return ResponseMapping(RequestType.DELETE, ResponseCode.SUCCESS);
	}

	// 로그아웃
	@RequestMapping(value = "/logout", method = RequestMethod.DELETE)
	public Map<String, String> logout(UserInfo user, Model model) {
		LOG.info("[RequestMapping()] POST : /logout START");
		// logout();
		LOG.info("[RequestMapping()] POST : /logout END");
		return ResponseMapping(RequestType.DELETE, ResponseCode.SUCCESS);
	}

//PUT========================================================================
	// 유저 정보 변경
	@RequestMapping(value = "/user", method = RequestMethod.PUT)
	public Map<String, String> updateUserInfo(UserInfo user, Model model) {
		LOG.info("[RequestMapping()] POST : /signIn START");
		// signUp();
		LOG.info("[RequestMapping()] POST : /signIn END");
		return ResponseMapping(RequestType.PUT, ResponseCode.SUCCESS);
	}


//GET========================================================================
	//
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public Map<String, String> getUser(UserInfo user, Model model) {
		LOG.info("[RequestMapping()] GET : /signIn START");
		// signUp();
		LOG.info("[RequestMapping()] GET : /signIn END");
		return ResponseMapping(RequestType.GET, ResponseCode.SUCCESS);
	}
	
//TEST===================================================================
	
	@Autowired
	testDaoController tdc;
	
	@RequestMapping(value = "/test1", method = RequestMethod.GET)
	public Map<String, String> test1() {
		
		tdc.run();
		
		return ResponseMapping(RequestType.POST, ResponseCode.SUCCESS);
	}
	
	@RequestMapping(value = "/h", method = RequestMethod.GET)
	public Map<String, String> home(Locale locale) {
		System.out.println("[LOG] == start home ==");

		try {
			Object hello = SpringBootApplication.class.getDeclaredField("hello");
			System.out.println(hello);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, String> map = new HashMap<String, String>();
		map.put("Request", "Success");
		return map;
	}
}
