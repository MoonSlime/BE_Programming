package com.navercorp.chat.mvc.controller;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.navercorp.chat.ApplicationDatabase;
import com.navercorp.chat.mvc.model.Room;
import com.navercorp.chat.service.JwtTokenUtil;

@Component
@ComponentScan("com.navercorp.chat.dao")
public class DataBaseController {
	private static final Logger LOG = Logger.getLogger(DataBaseController.class.getName());

	@Autowired
	private JdbcTemplate jdb;

	@Autowired
	private JwtTokenUtil jwt;

	@Autowired
	private ApplicationDatabase appDB;

	public DataBaseController() {
		LOG.info("DataBaseController Created");
	}

	// For Debug. Save Token to DB. IF(Deploy){It must be false.}
	private boolean token_to_db = false;

// USER_DB_CONTROLL ==================================================	

	// 유저 생성(회원가입)
	// params : userId, password, name
	// return : SUCCESS or FAIL
	public Boolean signup(String userId, String password, String name) throws Exception {
		LOG.info("[signup()] START");

		if (checkUserExist(userId, name)) {
			LOG.severe("[signup()] END with FAIL, Because UserId or UserName is Already Exist");
			return false;
		}

		try {
			String sql = String.format("INSERT INTO pgtDB.CHAT_USER_TB (userId, password) VALUES ('%s', '%s')", userId,
					password);
			System.out.println("Update Field = " + jdb.update(sql));
		} catch (DataAccessException e) {
			LOG.severe("INSERT CHAT_USER_TB FAIL");
			LOG.info("[signup()] END with FAIL");
			return false;
		}

		try {
			String sql = String.format("INSERT INTO pgtDB.CHAT_NAME_TB (userId, name) VALUES ('%s', '%s')", userId,
					name);
			System.out.println("Update Field = " + jdb.update(sql));
		} catch (DataAccessException e) {
			LOG.severe("INSERT CHAT_USER_TB FAIL");
			LOG.info("[signup()] END with FAIL");
			return false;
		}

		// 성공시 return true;
		LOG.info("[signup()] END with SUCCESS");
		return true;
	}

	// 유저 로그인.
	// params : userId, password
	// return : user's auth token
	// if(FAIL) : return null;
	public String login(String userId, String password) throws Exception {
		LOG.info("[login()] START");

		// check userId & password
		if (!checkUserPassword(userId, password)) {
			LOG.info("[login()] END with FAIL");
			return null;
		}

		String token = createAuthToken(userId);

		appDB.loginedUser.put(userId, token);

		if (!token_to_db) {
			LOG.info("[login()] END with SUCCESS");
			return token;
		}

		// INSERT TOKEN to CHAT_AUTH_TB. or UPDATE TOKEN to CHAT_AUTH_TB.
		try {
			String sql = String.format(
					"INSERT INTO pgtDB.CHAT_AUTH_TB (userId, token) VALUES ('%s','%s') ON DUPLICATE KEY UPDATE token='%s'",
					userId, token, token);
			jdb.update(sql);
		} catch (EmptyResultDataAccessException e) {
			LOG.severe("[login()] END with FAIL");
			return null;
		}

		LOG.info("[login()] END with SUCCESS");
		return token;
	}

	// 유저 로그아웃.
	// params : authToken
	// return : userId
	// if(FAIL) : return null;
	public String logout(String token) throws Exception {
		LOG.info("[logout()] START");

		if (!authorization(token)) {
			LOG.info("[logout()] END with FAIL");
			return null;
		}
		// token -> userId
		String userId = jwt.getUserIdFromToken(token);

		appDB.loginedUser.remove(userId);

		if (!token_to_db) {
			LOG.info("[logout()] END with SUCCESS");
			return token;
		}

		// DELETE Field.
		try {
			String sql = String.format("DELETE FROM pgtDB.CHAT_AUTH_TB WHERE token = '%s'", token);
			System.out.println("Update Field = " + jdb.update(sql));
		} catch (EmptyResultDataAccessException e) {// DELETE 실패.
			LOG.info("There's no same userId & token. It can't deleted");
			LOG.severe("[logout()] END with FAIL");
			return null;
		}

		LOG.info("[logout()] END with SUCCESS");
		return userId;
	}

	// 유저 회원탈퇴.
	// params : authToken
	// return : userId
	// if(FAIL) : return null;
	public String signout(String token) throws Exception {
		LOG.info("[signout()] START");

		if (!authorization(token)) {
			LOG.info("[signout()] END with FAIL");
			return null;
		}
		// token -> userId
		String userId = jwt.getUserIdFromToken(token);

		appDB.loginedUser.remove(userId);

		try {// CHAT_USER_TB's userId is (AUTH_TB & NAME_TB)'s Foriegn key. on delete
				// cascade.
			String sql = String.format("DELETE FROM pgtDB.CHAT_USER_TB WHERE userId='%s'", userId);
			System.out.println("Update Field = " + jdb.update(sql));
		} catch (EmptyResultDataAccessException e) {// DELETE 실패.
			LOG.info("There's no same userId & token. It can't deleted");
			LOG.severe("[signout()] END with FAIL");
			return null;
		}

		LOG.info("[signout()] END with SUCCESS");
		return userId;
	}

	// 유저정보 변경.
	// params : token, name
	// return : userId, name
	// if(FAIL) : return null;
	public Map<String, Object> updateUserInfo(String token, String newName) throws Exception {
		LOG.info("[updateUserInfo()] START");

		if (!authorization(token)) {
			LOG.info("[updateUserInfo()] END with FAIL");
			return null;
		}

		// token -> userId
		String userId = jwt.getUserIdFromToken(token);

		// userId-> update name.
		// UPDATE DB.NAME_TB SET name = '%s' WHERE userId = '%s'
		try {
			String sql = String.format("UPDATE pgtDB.CHAT_NAME_TB SET name = '%s' WHERE userId='%s'", newName, userId);
			jdb.update(sql);
		} catch (EmptyResultDataAccessException e) {
			LOG.severe("FAIL with Update User Name ");
			LOG.info("[updateUserInfo()] END with FAIL");
			return null;
		}

		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("userId", userId);
		ret.put("name", newName);

		LOG.info("[updateUserInfo()] END with SUCCESS");
		return ret;
	}

	// 유저 목록 조회
	// params : token
	// return : map<userId, name>
	// if(FAIL) : return null;
	public List<Map<String, Object>> getUserList(String token) throws Exception {
		LOG.info("[getUserList()] START");

		if (!authorization(token)) {
			LOG.info("[getUserList()] END with FAIL");
			return null;
		}

		List<Map<String, Object>> users = null;
		try {
			String sql = String.format("SELECT userId, name FROM pgtDB.CHAT_NAME_TB");
			users = jdb.queryForList(sql);
		} catch (EmptyResultDataAccessException e) {
			LOG.info("[getUserList()] END with FAIL");
			return null;
		}

		LOG.info("[getUserList()] END with SUCCESS");
		return users;
	}

	// 로그인한 유저 목록 조회 (잠시 사용 중지)
	// params : token
	// return : map<userId, name>
	// if(FAIL) : return null;
	public List<Map<String, Object>> getLoginedUserList(String token) throws Exception {
		LOG.info("[getLloginedUserList()] START");

		if (!authorization(token)) {
			LOG.info("[getLoginedUserList()] END with FAIL");
			return null;
		}

		// appDB.update();!!

		List<Map<String, Object>> users = null;
		try {
			String sql = new String(
					"SELECT a.userID, n.name FROM pgtDB.CHAT_AUTH_TB AS a JOIN pgtDB.CHAT_NAME_TB AS n ON a.userID = n.userId");
			users = jdb.queryForList(sql);
		} catch (EmptyResultDataAccessException e) {
			LOG.info("[getLoginedUserList()] END with FAIL");
			return null;
		}

		LOG.info("[getLoginedUserList()] END with SUCCESS");
		return users;
	}

// ROOM_DB_CONTROLL ===================================================

	// 채팅방 생성
	//
	public Map<String, Object> createChatRoom(String token, String rname, String rpassword) {
		LOG.info("[createChatRoom()] START");

		if (!authorization(token)) {
			LOG.info("[createChatRoom()] END with FAIL");
			return null;
		}

		// Name 중복 확인.
		for (String key : appDB.createdRoom.keySet()) {
			if (appDB.createdRoom.get(key).getName().equals(rname)) {
				LOG.info("[createChatRoom()] END with FAIL");
				return null;
			}
		}

		String roomId = null, userId = jwt.getUserIdFromToken(token), username = null;
		try {
			String sql = null;
			if (rpassword == null) {
				sql = String.format("INSERT INTO pgtDB.CHAT_ROOM_TB (name) VALUES ('%s')", rname);
			} else {
				sql = String.format("INSERT INTO pgtDB.CHAT_ROOM_TB (name,password) VALUES ('%s','%s')", rname,
						rpassword);
			}
			jdb.update(sql);

			sql = String.format("SELECT roomID FROM pgtDB.CHAT_ROOM_TB WHERE name='%s'", rname);
			roomId = Integer.toString((int) jdb.queryForMap(sql).get("roomId"));

			sql = String.format("SELECT name FROM pgtDB.CHAT_NAME_TB WHERE userId='%s'", userId);
			username = (String) jdb.queryForMap(sql).get("name");

		} catch (EmptyResultDataAccessException e) {
			LOG.info("[createChatRoom()] END with FAIL");
			return null;
		}

		Room room = new Room();
		room.setRoomId(roomId);
		room.setName(rname);
		room.setPassword(rpassword);
		room.setLastMsgId(0);
		appDB.createdRoom.put(roomId, room);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("roomId", roomId);
		result.put("name", rname);
		Map<String, Object> user = new HashMap<String, Object>();
		user.put("userId", userId);
		user.put("name", username);
		result.put("users", user);

		LOG.info("[createChatRoom()] END with SUCCESS");
		return result;
	}

	//채팅방 조회 
	//return : RoomList.
	public List<Map<String, String>> getRoomList(String token) {
		LOG.info("[getRoomList()] START");

		if (!authorization(token)) {
			LOG.info("[getRoomList()] END with FAIL");
			return null;
		}
		
		List<Map<String, String>> rooms = new Vector<Map<String, String>>();
		for (String key : appDB.createdRoom.keySet()) {
			Map<String, String> room = new HashMap<String, String>();
			room.put("roomId", key);
			room.put("name", appDB.createdRoom.get(key).getName());
			rooms.add(room);
		}
		
		return rooms;
	}
	
// Func() =======================================================
	// 유저 존재 확인.
	private boolean checkUserExist(String userId, String name) {
		Map<String, Object> map = null;
		try {
			String sql = String.format("SELECT userId FROM pgtDB.CHAT_USER_TB WHERE userId='%s'", userId);
			map = jdb.queryForMap(sql);
		} catch (EmptyResultDataAccessException e) {// User가 없는 경우.
			LOG.info("There's no same userId. It can create new user");
		}

		if (map != null)
			return true;

		try {
			String sql = String.format("SELECT userId FROM pgtDB.CHAT_NAME_TB WHERE name='%s'", name);
			map = jdb.queryForMap(sql);
		} catch (EmptyResultDataAccessException e) {// User가 없는 경우.
			LOG.info("There's no same name. It can create new user");
		}

		if (map != null)
			return true;

		return false;
	}

	// 유저 존재 확인 & 유저 비밀번호의 적합성 검사.
	private boolean checkUserPassword(String userId, String password) {
		try {
			String sql = String.format("SELECT userId FROM pgtDB.CHAT_USER_TB WHERE userID='%s' AND password='%s'",
					userId, password);
			Map<String, Object> map = jdb.queryForMap(sql);
		} catch (EmptyResultDataAccessException e) {
			LOG.severe("There's no appropriate User");
			return false;
		}
		return true;
	}

	private String createAuthToken(String userId) {
		String generatedToken = jwt.generateToken(userId);
		System.out.println(userId + " Token => " + generatedToken);
		return generatedToken;
	}

	private boolean authorization(String token) {
		String validToken = appDB.loginedUser.get(jwt.getUserIdFromToken(token));
		if (validToken == null)
			return false;
		else if (validToken.equals(token))
			return !jwt.isTokenExpired(validToken);
		else
			return false;

//		String userId = null;
//		try {
//			Map<String, Object> map = new HashMap<String, Object>();
//			String sql = String.format("SELECT userId FROM pgtDB.CHAT_AUTH_TB WHERE token='%s'", token);
//			map = jdb.queryForMap(sql);
//			userId = (String) map.get("userId");
//		} catch (EmptyResultDataAccessException e) {
//			LOG.severe("Authorization FAIL");
//			return false;
//		}
//
//		return jwt.validateToken(token, userId);
	}

	// get & check. if user have autoToken.
	// params : userId
	// return : user's auth token
	// if(FAIL) : return null;
	private String getAuthTokenFromDB(String userId) {
		// SELECT token FROM pgtDB.CHAT_AUTH_TB WHERE userID='testUserId1';
		Map<String, Object> map = null;
		try {
			String sql = String.format("SELECT token FROM pgtDB.CHAT_AUTH_TB WHERE userID='%s'", userId);
			map = jdb.queryForMap(sql);
		} catch (EmptyResultDataAccessException e) {
			LOG.info("There's no token for user.");
			return null;
		}

		String token = (String) map.get("token");
		return token;
	}

//	==============================================================
	public Hashtable<String, Room> getCurrentRoomList(String auth) {
		LOG.info("[getCurrentRoomList()] START");

		if (!auth.equals("KR19815")) {
			return null;
		}

		List<Map<String, Object>> rooms = null;
		try {
			String sql = new String("SELECT * FROM pgtDB.CHAT_ROOM_TB");
			rooms = jdb.queryForList(sql);
		} catch (EmptyResultDataAccessException e) {
			LOG.info("[getCurrentRoomList()] END with NULL");
			return null;
		}

		Hashtable<String, Room> ht = new Hashtable<String, Room>();

		ListIterator<Map<String, Object>> iter = rooms.listIterator();
		while (iter.hasNext()) {
			Map<String, Object> map = iter.next();
			Room room = new Room();
			room.setRoomId(Integer.toString((int) map.get("roomId")));
			room.setName((String) map.get("name"));
			room.setPassword((String) map.get("password"));
			room.setLastMsgId((int) map.get("lastMsgId"));
			ht.put((String) Integer.toString((int) map.get("roomId")), room);
		}

		LOG.info("[getCurrentRoomList()] END with SUCCESS");
		return ht;
	}
}
