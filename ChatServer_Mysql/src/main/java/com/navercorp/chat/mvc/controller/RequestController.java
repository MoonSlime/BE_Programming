package com.navercorp.chat.mvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.navercorp.chat.mvc.model.User;
import com.navercorp.chat.service.JwtResponse;

@RestController
@Configuration
@ComponentScan("com.navercorp.chat.dao")
@ComponentScan("com.navercorp.chat.mvc.controller")
@ComponentScan("com.navercorp.chat.service")
//@ComponentScan("com.navercorp.chat.service")
public class RequestController {
	private static final Logger LOG = Logger.getLogger(RequestController.class.getName());
	
	@Autowired
	private DataBaseController dbc;
	
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
	public Map<String, String> signup(User user, Model model) throws Exception {
		LOG.info("[signup()] POST : /signIn START");
		ResponseCode rc = ResponseCode.FAIL;
		if (dbc.signup(user)) {
			rc = ResponseCode.SUCCESS;
		}
		LOG.info("[signup()] POST : /signIn END");
		return ResponseMapping(RequestType.POST, rc);
	}
	
	// 로그인
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Map<String, String> login(User user, Model model) throws Exception {
		LOG.info("[login()] POST : /login START");
		ResponseCode rc = ResponseCode.SUCCESS;
		if ((user = dbc.login(user))==null) {
			rc = ResponseCode.FAIL;
		}
		LOG.info("[login()] POST : /login END");
		return ResponseMapping(RequestType.POST, rc);
	}

	//채팅방을 생성한다. 생성한 채팅방에 자동입장됨. name 은 중복이 불가능. password 는 선택사항
//	@RequestMapping(value = "/room", method = RequestMethod.POST)
//	public Map<String, String> createChatRoom()
	
//DELETE======================================================================
	// 회원탈퇴
	@RequestMapping(value = "/user", method = RequestMethod.DELETE)
	public Map<String, String> signout(User user, Model model) throws Exception {
		LOG.info("[signout()] DELETE : /user START");
		ResponseCode rc = ResponseCode.SUCCESS;
		if ((user = dbc.signout(user))==null) {
			rc = ResponseCode.FAIL;
		}
		LOG.info("[signout()] DELETE : /user END");
		return ResponseMapping(RequestType.DELETE, rc);
	}

	// 로그아웃
	@RequestMapping(value = "/logout", method = RequestMethod.DELETE)
	public Map<String, String> logout(User user, Model model) throws Exception {
		LOG.info("[logout()] DELETE : /logout START");
		ResponseCode rc = ResponseCode.SUCCESS;
		if ((user = dbc.logout(user))==null) {
			rc = ResponseCode.FAIL;
		}
		LOG.info("[logout()] DELETE: /logout END");
		return ResponseMapping(RequestType.DELETE, rc);
	}

//PUT========================================================================
	// 유저 정보 변경
	@RequestMapping(value = "/user", method = RequestMethod.PUT)
	public Map<String, String> updateUserInfo(User user, Model model) throws Exception {
		LOG.info("[updateUserInfo()] PUT : /user START");
		ResponseCode rc = ResponseCode.SUCCESS;
		if ((user = dbc.updateUserInfo(user))==null) {
			rc = ResponseCode.FAIL;
		}
		LOG.info("[updateUserInfo()] PUT : /user END");
		return ResponseMapping(RequestType.PUT, rc);
	}


//GET========================================================================
	// 유저 목록 조회 
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public Map<String, Object> getUserList(User user, Model model) throws Exception {
		LOG.info("[getUserList()] GET : /user START");
		ResponseCode rc = ResponseCode.SUCCESS;
		List<Map<String, Object>> users = null;
		if ((users = dbc.getUserList(user))==null) {
			rc = ResponseCode.FAIL;
		}
		LOG.info("[getUserList()] GET : /user END");
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("responseCode", (Object)rc);
		responseMap.put("users", (Object)users);
		return responseMap;
//		return ResponseMapping(RequestType.GET, rc);
	}

	// 로그인한 유저 목록 조회 
	@RequestMapping(value = "/user/login", method = RequestMethod.GET)
	public Map<String, Object> getLoginedUserList(User user, Model model) throws Exception {
		LOG.info("[getLoginedUserList()] GET : /user/login START");
		ResponseCode rc = ResponseCode.SUCCESS;
		List<Map<String, Object>> users = null;
		if ((users = dbc.getLoginedUserList(user))==null) {
			rc = ResponseCode.FAIL;
		}
		LOG.info("[getLoginedUserList()] GET : /user/login END");
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("responseCode", (Object)rc);
		responseMap.put("users", (Object)users);
		return responseMap;
//		return ResponseMapping(RequestType.GET, rc);
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
