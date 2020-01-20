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

import com.navercorp.chat.mvc.model.Room;
import com.navercorp.chat.service.ChatService;
import com.navercorp.chat.service.UserService;

@RestController
@Configuration
@ComponentScan("com.navercorp.chat.mvc.controller")
public class RequestController {
	private static final Logger LOG = Logger.getLogger(RequestController.class.getName());

	@Autowired
	private DataBaseController dbc;

	@Autowired
	private ChatService cs;
	
	@Autowired
	private UserService us;
	
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
	// 유저생성.
	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public Map<String, Object> createUser(@RequestParam("userId") String userId, @RequestParam("password") String password,
			@RequestParam("name") String name) throws Exception {

		// password Encryption.

		Map<String, Object> response = new HashMap<String, Object>();
		if(us.createUser(userId, password, name)) {
			response.put("responseCode", 0);
		}
		else {
			response.put("responseCode", 1);
		}
		return response;
	}

	// 로그인
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Map<String, Object> login(@RequestParam("userId") String userId, @RequestParam("password") String password)
			throws Exception {
		LOG.info("[login()] POST : /login START");
		// password Encryption.

		Map<String, Object> response = new HashMap<String, Object>();
		
		String token = null;
		if ((token = us.login(userId, password)) == null) {
			LOG.info("[login()] POST : /login => FAIL");
			response.put("responseCode", 1);
		} else {
			LOG.info("[login()] POST : /login => SUCCESS");
			response.put("responseCode", 0);
			response.put("token", token);
		}

		LOG.info("[login()] POST : /login END");
		return response;
	}

//	string	token	user auth token
//	string	name	room name
//	string	password	room password. not essential
	@RequestMapping(value = "/room", method = RequestMethod.POST)
	public Map<String, Object> createChatRoom(@RequestParam("token") String token, @RequestParam("name") String rname,
			@RequestParam(value = "password", required = false) String rpassword) {
		LOG.info("[createChatRoom()] POST : /room START");

		// rpassword encryption.

		Map<String, Object> response = cs.createChatRoom(token, rname, rpassword);

		if (response == null) {
			LOG.info("[createChatRoom()] POST : /room => FAIL");
			response = new HashMap<String, Object>();
			response.put("responseCode", 1);
		} else {// Success.
			LOG.info("[createChatRoom()] POST : /room => SUCCESS");
			response.put("responseCode", 0);
		}

		LOG.info("[createChatRoom()] POST : /room END");
		return response;
	}

//	//채팅방 입장.
	@RequestMapping(value = "/room/user", method = RequestMethod.POST)
	public Map<String, Object> joinChatRoom(@RequestParam("token") String token, @RequestParam("roomId") String roomId,
			@RequestParam(value = "password", required = false) String password) throws Exception {

		// password Encryption.

		Map<String, Object> response = cs.joinChatRoom(token, roomId, password);

		if (response == null) {
			response = new HashMap<String, Object>();
			response.put("responseCode", 1);
		} else {// Success.
			response.put("responseCode", 0);
		}

		return response;
	}

	// 대화(메시지 전송)
	@RequestMapping(value = "/room/talk", method = RequestMethod.POST)
	public Map<String, Object> sendMsg(@RequestParam("token") String token, @RequestParam("roomId") String roomId,
			@RequestParam(value = "text", required = true) String text) throws Exception {
		LOG.info("[sendMsg()] POST : /room/talk START");

		Map<String, Object> response = new HashMap<String, Object>();
		Map<String, Object> msg = cs.sendMsg(token, "talk", roomId, null, text);

		if (msg == null) {
			LOG.info("[sendMsg()] POST : /room/talk => FAIL");
			response.put("responseCode", 1);
		} else {// Success.
			LOG.info("[sendMsg()] POST : /room/talk => SUCCESS");
			response.put("responseCode", 0);
			response.put("roomId", roomId);
			response.put("msg", msg);
		}

		LOG.info("[sendMsg()] POST : /room/talk END");
		return response;
	}

	// 귓속말(메시지 전송)
	@RequestMapping(value = "/room/whisper", method = RequestMethod.POST)
	public Map<String, Object> sendWhisperMsg(@RequestParam("token") String token,
			@RequestParam("roomId") String roomId, @RequestParam("to") String to, @RequestParam("text") String text)
			throws Exception {
		LOG.info("[sendMsg()] POST : /room/whisper START");

		Map<String, Object> response = new HashMap<String, Object>();
		Map<String, Object> msg = cs.sendMsg(token, "whisper", roomId, to, text);

		if (msg == null) {
			LOG.info("[sendMsg()] POST : /room/talk => FAIL");
			response.put("responseCode", 1);
		} else {// Success.
			LOG.info("[sendMsg()] POST : /room/talk => SUCCESS");
			response.put("responseCode", 0);
			response.put("roomId", roomId);
			response.put("msg", msg);
		}

		LOG.info("[sendMsg()] POST : /room/whisper END");

		return response;
	}

//DELETE======================================================================
	// 유저 탈퇴.
	@RequestMapping(value = "/user", method = RequestMethod.DELETE)
	public Map<String, Object> deleteUser(@RequestParam("token") String token) throws Exception {
		LOG.info("[signout()] DELETE : /user START");
		
		Map<String, Object> response = new HashMap<String, Object>();
		String userId = us.deleteUser(token);

		if (userId == null) {
			LOG.info("[signout()] DELETE : /user => FAIL");
			response.put("responseCode", 1);
		}
		else {
			LOG.info("[signout()] DELETE : /user => SUCCESS");
			response.put("responseCode", 0);
			response.put("userId", userId);
		}
		
		LOG.info("[signout()] DELETE : /user END");
		return response;
	}

	// 로그아웃
	@RequestMapping(value = "/logout", method = RequestMethod.DELETE)
	public Map<String, Object> logout(@RequestParam("token") String token) throws Exception {
		LOG.info("[logout()] DELETE : /logout START");

		Map<String, Object> response = new HashMap<String, Object>();
		String userId = us.logout(token);
		
		if (userId == null) {
			LOG.info("[logout()] DELETE : /logout => FAIL");
			response.put("responseCode", 1);
		}
		else {
			LOG.info("[logout()] DELETE : /logout => SUCCESS");
			response.put("responseCode", 0);
			response.put("userId", userId);
		}
		
		LOG.info("[logout()] DELETE : /logout END");		
		return response;
	}

	// 채팅방 나가기.
	@RequestMapping(value = "/room/exit", method = RequestMethod.DELETE)
	public Map<String, Object> exitRoom(@RequestParam("token") String token, @RequestParam("roomId") String roomId)
			throws Exception {
		Map<String, Object> response = new HashMap<String, Object>();

		if (cs.exitRoom(token, roomId)) {// SUCCESS
			response.put("responseCode", 0);
			response.put("roomId", roomId);
		} else {// FAIL
			response.put("respoonseCode", 1);
		}

		return response;
	}

//PUT========================================================================
	// 유저 정보 변경
	// return : userId, name
	@RequestMapping(value = "/user", method = RequestMethod.PUT)
	public Map<String, Object> updateUserInfo(@RequestParam("token") String token, @RequestParam("name") String name)
			throws Exception {
		LOG.info("[updateUserInfo()] PUT : /user START");
		
		Map<String, Object> response = us.updateUserInfo(token, name);
		if (response == null) {
			response = new HashMap<String, Object>();
			response.put("responseCode", 1);
		}
		else {
			response.put("responseCode", 0);
		}
		
		LOG.info("[updateUserInfo()] PUT : /user END");
		return response;
	}

	// 채팅방 정보 변경.
//	string	token	user auth token
//	string	roomId	room id
//	string	name	if empty or null, name is not changed
//	string	password	if empty or null, password is deleted
	@RequestMapping(value = "/room", method = RequestMethod.PUT)
	public Map<String, Object> udpateRoomInfo(@RequestParam("token") String token,
			@RequestParam("roomId") String roomId, @RequestParam(value = "name", required = false) String rname,
			@RequestParam(value = "password", required = false) String password) throws Exception {
		LOG.info("[updateRoomInfo()] PUT : /room START");
		Map<String, Object> response = new HashMap<String, Object>();
		Room room = cs.updateRoomInfo(token, roomId, rname, password);
		
		if (room != null) {
			LOG.info("[updateRoomInfo()] PUT : /room => SUCCESS");
			response.put("responseCode", 0);
			response.put("roomId", room.getRoomId());
			response.put("name", room.getName());
		} else {
			LOG.info("[updateRoomInfo()] PUT : /room => FAIL");
			response.put("responseCode", 1);
		}

		LOG.info("[updateRoomInfo()] PUT : /room END");
		return response;
	}

//GET========================================================================
	// 유저 목록 조회
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public Map<String, Object> getUserList(@RequestParam("token") String token) throws Exception {
		LOG.info("[getUserList()] GET : /user START");
		ResponseCode rc = ResponseCode.SUCCESS;
		List<Map<String, Object>> users = null;
		if ((users = dbc.getUsers(token)) == null) {
			rc = ResponseCode.FAIL;
		}

		LOG.info("[getUserList()] GET : /user END");
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("responseCode", (Object) rc);
		responseMap.put("users", (Object) users);
		return responseMap;
	}

	// 채팅방 조회
	@RequestMapping(value = "/room", method = RequestMethod.GET)
	public Map<String, Object> getRooms(@RequestParam("token") String token) throws Exception {
		LOG.info("[getRooms()] GET : /room START");

		Map<String, Object> response = new HashMap<String, Object>();
		List<Map<String,Object>> rooms = cs.getRooms(token);
		
		if (rooms == null) {
			LOG.info("[getRooms()] GET : /room => FAIL");
			response.put("responseCode", 1);
		} else {// Success.
			LOG.info("[getRooms()] GET : /room => SUCCESS");
			response.put("responseCode", 0);
			response.put("rooms", rooms);
		}

		LOG.info("[getRooms()] GET : /room END");
		return response;
	}

	// 채팅방 멤버 조회.
	@RequestMapping(value = "/room/member", method = RequestMethod.GET)
	public Map<String, Object> getMembers(@RequestParam("token") String token, @RequestParam("roomId") String roomId)
			throws Exception {
		LOG.info("[getMembers()] GET : /room/member START");

		Map<String, Object> response = new HashMap<String, Object>();
		List<Map<String, Object>> users = cs.getMembers(token, roomId);
		
		if (users == null) {
			LOG.info("[getMembers()] GET : /room/member => FAIL");
			response.put("responseCode", 1);
		} else {// Success.
			LOG.info("[getMembers()] GET : /room/member => SUCCESS");
			response.put("responseCode", 0);
			response.put("roomId", roomId);
			response.put("users", users);
		}

		LOG.info("[getMembers()] GET : /room/member END");
		return response;
	}

	// 대화 내역 조회.
	@RequestMapping(value = "/room/talk", method = RequestMethod.GET)
	public Map<String, Object> getMsgListFromRoom(@RequestParam("token") String token,
			@RequestParam("roomId") String roomId, @RequestParam("msgId") int msgId,
			@RequestParam("orderBy") String orderBy, @RequestParam("msgCnt") String msgCnt) throws Exception {
		LOG.info("[getMsgList()] GET : /room/talk START");
		Map<String, Object> response = new HashMap<String, Object>();
		List<Map<String, Object>> msgs = cs.getMsgsFromRoom(token, roomId, msgId, orderBy, msgCnt);
		
		if (msgs == null) {
			LOG.info("[getMsgList()] GET : /room/talk => FAIL");
			response.put("responseCode", 1);
		} else {// Success.
			LOG.info("[getMsgList()] GET : /room/talk => SUCCESS");
			response.put("responseCode", 0);
			response.put("msgs", msgs);
			response.put("roomId", roomId);
		}

		LOG.info("[getMsgList()] GET : /room/talk END");
		return response;
	}
	
	
	// 타임라인
	@RequestMapping(value = "/timeline", method = RequestMethod.GET)
	public Map<String, Object> getTimeline(@RequestParam("token") String token) throws Exception {
		LOG.info("[getTimeline()] GET : /timeline START");
		Map<String, Object> response = new HashMap<String, Object>();
		
		List<Map<String, Object>> rooms = cs.getTimeline(token);
		if (rooms == null) {
			LOG.info("[getTimeline()] GET : /timeline => FAIL");
			response.put("responseCode", 1);
		} else {// Success.
			LOG.info("[getTimeline()] GET : /timeline => SUCCESS");
			response.put("responseCode", 0);
			response.put("rooms", rooms);
		}

		LOG.info("[getTimeline()] GET : /timeline END");
		return response;
	}
	
	// 최신글.
	@RequestMapping(value = "/room/summary", method = RequestMethod.GET)
	public Map<String, Object> getSummary(@RequestParam("token") String token) throws Exception {
		LOG.info("[getSummary()] GET : /timeline START");
		Map<String, Object> response = new HashMap<String, Object>();
		
		List<Map<String, Object>> rooms = cs.getSummary(token);
		if (rooms == null) {
			LOG.info("[getSummary()] GET : /timeline => FAIL");
			response.put("responseCode", 1);
		} else {// Success.
			LOG.info("[getSummary()] GET : /timeline => SUCCESS");
			response.put("responseCode", 0);
			response.put("rooms", rooms);
		}

		LOG.info("[getSummary()] GET : /timeline END");
		return response;
	}
	

//TEST===================================================================

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
