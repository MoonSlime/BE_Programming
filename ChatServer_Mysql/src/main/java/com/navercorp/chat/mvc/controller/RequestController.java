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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Configuration
@ComponentScan("com.navercorp.chat.mvc.controller")
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
	public Map<String, String> signup(@RequestParam("userId") String userId, @RequestParam("password") String password,
			@RequestParam("name") String name) throws Exception {

		// password Encryption.

		LOG.info("[signup()] POST : /signIn START");
		ResponseCode rc = ResponseCode.FAIL;
		if (dbc.signup(userId, password, name)) {
			rc = ResponseCode.SUCCESS;
		}
		LOG.info("[signup()] POST : /signIn END");
		return ResponseMapping(RequestType.POST, rc);
	}

	// 로그인
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Map<String, String> login(@RequestParam("userId") String userId, @RequestParam("password") String password)
			throws Exception {

		// password Encryption.

		String token = null;

		LOG.info("[login()] POST : /login START");
		ResponseCode rc = ResponseCode.SUCCESS;
		if ((token = dbc.login(userId, password)) == null) {
			rc = ResponseCode.FAIL;
		}
		LOG.info("[login()] POST : /login END");
		return ResponseMapping(RequestType.POST, rc);
	}

//	string	token	user auth token
//	string	name	room name
//	string	password	room password. not essential
	// 채팅방을 생성한다. 생성한 채팅방에 자동입장됨. name 은 중복이 불가능. password 는 선택사항
	@RequestMapping(value = "/room", method = RequestMethod.POST)
	public Map<String, Object> createChatRoom(@RequestParam("token") String token, @RequestParam("name") String rname,
			@RequestParam(value = "password", required = false) String rpassword) {

		// rpassword encryption.

		Map<String, Object> response = dbc.createChatRoom(token, rname, rpassword);

		if (response == null) {
			response = new HashMap<String, Object>();
			response.put("responseCode", 1);
		} else {// Success.
			response.put("responseCode", 0);
		}

		return response;
	}

//DELETE======================================================================
	// 회원탈퇴
	@RequestMapping(value = "/user", method = RequestMethod.DELETE)
	public Map<String, String> signout(@RequestParam("token") String token) throws Exception {

		String userId = null;

		LOG.info("[signout()] DELETE : /user START");
		ResponseCode rc = ResponseCode.SUCCESS;
		if ((userId = dbc.signout(token)) == null) {
			rc = ResponseCode.FAIL;
		}
		LOG.info("[signout()] DELETE : /user END");
		return ResponseMapping(RequestType.DELETE, rc);
	}

	// 로그아웃
	@RequestMapping(value = "/logout", method = RequestMethod.DELETE)
	public Map<String, String> logout(@RequestParam("token") String token) throws Exception {
		String userId = null;

		LOG.info("[logout()] DELETE : /logout START");
		ResponseCode rc = ResponseCode.SUCCESS;
		if ((userId = dbc.logout(token)) == null) {
			rc = ResponseCode.FAIL;
		}
		LOG.info("[logout()] DELETE: /logout END");
		return ResponseMapping(RequestType.DELETE, rc);
	}

//PUT========================================================================
	// 유저 정보 변경
	// return : userId, name
	@RequestMapping(value = "/user", method = RequestMethod.PUT)
	public Map<String, String> updateUserInfo(@RequestParam("token") String token, @RequestParam("name") String name)
			throws Exception {
		Map<String, Object> response = null;

		LOG.info("[updateUserInfo()] PUT : /user START");
		ResponseCode rc = ResponseCode.SUCCESS;
		if ((response = dbc.updateUserInfo(token, name)) == null) {
			rc = ResponseCode.FAIL;
		}

		LOG.info("[updateUserInfo()] PUT : /user END");
		return ResponseMapping(RequestType.PUT, rc);
	}

//GET========================================================================
	// 유저 목록 조회
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public Map<String, Object> getUserList(@RequestParam("token") String token) throws Exception {
		LOG.info("[getUserList()] GET : /user START");
		ResponseCode rc = ResponseCode.SUCCESS;
		List<Map<String, Object>> users = null;
		if ((users = dbc.getUserList(token)) == null) {
			rc = ResponseCode.FAIL;
		}

		LOG.info("[getUserList()] GET : /user END");
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("responseCode", (Object) rc);
		responseMap.put("users", (Object) users);
		return responseMap;
	}

	// 로그인한 유저 목록 조회
//	@RequestMapping(value = "/user/login", method = RequestMethod.GET)
//	public Map<String, Object> getLoginedUserList(@RequestParam("token") String token) throws Exception {
//		LOG.info("[getLoginedUserList()] GET : /user/login START");
//		ResponseCode rc = ResponseCode.SUCCESS;
//		List<Map<String, Object>> users = null;
//		if ((users = dbc.getLoginedUserList(token)) == null) {
//			rc = ResponseCode.FAIL;
//		}
//		LOG.info("[getLoginedUserList()] GET : /user/login END");
//		Map<String, Object> responseMap = new HashMap<String, Object>();
//		responseMap.put("responseCode", (Object) rc);
//		responseMap.put("users", (Object) users);
//		return responseMap;
//	}
	
	//채팅방 조회 
	@RequestMapping(value = "/room", method = RequestMethod.GET)
	public Map<String, Object> getRoomList(@RequestParam("token") String token) throws Exception {
		Map<String, Object> response = new HashMap<String, Object>();

		List<Map<String, String>> rooms = dbc.getRoomList(token);
		if (rooms == null) {
			response.put("responseCode", 1);
		} else {// Success.
			response.put("rooms", rooms);
			response.put("responseCode", 0);
		}

		return response;
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
