package com.navercorp.chat.mvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.navercorp.chat.mvc.model.User;
import com.navercorp.chat.service.JwtTokenUtil;

@Component
@ComponentScan
public class DataBaseController {
	private static final Logger LOG = Logger.getLogger(DataBaseController.class.getName());

//	@Autowired
//	private MySqlConfig mysql;

	@Autowired
	private JdbcTemplate jdb;

	@Autowired
	private JwtTokenUtil jwt;

	public DataBaseController() {
		LOG.info("DBC Created");
	}

	// 유저 생성(회원가입)
	// params : userId, password, name
	// return : SUCCESS or FAIL
	public Boolean signup(User user) throws Exception {
		LOG.info("[signup()] START");

		if (checkUserExist(user.getUserId(), user.getName())) {
			LOG.severe("[signup()] END with FAIL, Because UserId or UserName is Already Exist");
			return false;
		}

		try {
			String sql = String.format("INSERT INTO pgtDB.CHAT_USER_TB (userId, password) VALUES ('%s', '%s')",
					user.getUserId(), user.getPassword());
			System.out.println("Update Field = " + jdb.update(sql));
		} catch (DataAccessException e) {
			LOG.severe("INSERT CHAT_USER_TB FAIL");
			LOG.info("[signup()] END with FAIL");
			return false;
		}

		try {
			String sql = String.format("INSERT INTO pgtDB.CHAT_NAME_TB (userId, name) VALUES ('%s', '%s')",
					user.getUserId(), user.getName());
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
	public User login(User user) throws Exception {
		LOG.info("[login()] START");

		// check userId & password
		if (!checkUserPassword(user.getUserId(), user.getPassword())) {
			LOG.info("[login()] END with FAIL");
			return null;
		}

		// create & set token
		user.setToken(createAuthToken(user));

		// INSERT TOKEN to CHAT_AUTH_TB. or UPDATE TOKEN to CHAT_AUTH_TB.
		try {
			String sql = String.format(
					"INSERT INTO pgtDB.CHAT_AUTH_TB (userId, token) VALUES ('%s','%s') ON DUPLICATE KEY UPDATE token='%s'",
					user.getUserId(), user.getToken(), user.getToken());
			jdb.update(sql);
		} catch (EmptyResultDataAccessException e) {
			LOG.severe("[login()] END with FAIL");
			return null;
		}
		
		LOG.info("[login()] END with SUCCESS");
		return user;
	}

	// 유저 로그아웃.
	// params : authToken
	// return : userId
	// if(FAIL) : return null;
	public User logout(User user) throws Exception {
		LOG.info("[logout()] START");

		if (!authorization(user.getToken())) {
			LOG.info("[logout()] END with FAIL");
			return null;
		}
		// token -> userId
		user.setUserId(jwt.getUserIdFromToken(user.getToken()));

		// DELETE Field.
		try {
			String sql = String.format("DELETE FROM pgtDB.CHAT_AUTH_TB WHERE token = '%s'", user.getToken());
			System.out.println("Update Field = " + jdb.update(sql));
		} catch (EmptyResultDataAccessException e) {// DELETE 실패.
			LOG.info("There's no same userId & token. It can't deleted");
			LOG.severe("[logout()] END with FAIL");
			return null;
		}

		LOG.info("[logout()] END with SUCCESS");
		return user;
	}

	// 유저 회원탈퇴.
	// params : authToken
	// return : userId
	// if(FAIL) : return null;
	public User signout(User user) throws Exception {
		LOG.info("[signout()] START");

		if (!authorization(user.getToken())) {
			LOG.info("[signout()] END with FAIL");
			return null;
		}
		// token -> userId
		user.setUserId(jwt.getUserIdFromToken(user.getToken()));

		try {// CHAT_USER_TB's userId is (AUTH_TB & NAME_TB)'s Foriegn key. on delete
				// cascade.
			String sql = String.format("DELETE FROM pgtDB.CHAT_USER_TB WHERE userId='%s'", user.getUserId());
			System.out.println("Update Field = " + jdb.update(sql));
		} catch (EmptyResultDataAccessException e) {// DELETE 실패.
			LOG.info("There's no same userId & token. It can't deleted");
			LOG.severe("[signout()] END with FAIL");
			return null;
		}

		LOG.info("[signout()] END with SUCCESS");
		return user;
	}

	// 유저정보 변경.
	// params : token, name
	// return : userId, name
	// if(FAIL) : return null;
	public User updateUserInfo(User user) throws Exception {
		LOG.info("[updateUserInfo()] START");

		if (!authorization(user.getToken())) {
			LOG.info("[updateUserInfo()] END with FAIL");
			return null;
		}

		// token -> userId
		user.setUserId(jwt.getUserIdFromToken(user.getToken()));

		// userId-> update name.
		// UPDATE DB.NAME_TB SET name = '%s' WHERE userId = '%s'
		try {
			String sql = String.format("UPDATE pgtDB.CHAT_NAME_TB SET name = '%s' WHERE userId='%s'", user.getName(),
					user.getUserId());
			jdb.update(sql);
		} catch (EmptyResultDataAccessException e) {
			LOG.severe("FAIL with Update User Name ");
			LOG.info("[updateUserInfo()] END with FAIL");
			return null;
		}

		LOG.info("[updateUserInfo()] END with SUCCESS");
		return user;
	}

	// 유저 목록 조회
	// params : token
	// return : map<userId, name>
	// if(FAIL) : return null;
	public List<Map<String, Object>> getUserList(User user) throws Exception {
		LOG.info("[getUserList()] START");

		if (!authorization(user.getToken())) {
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

	// 로그인한 유저 목록 조회
	// params : token
	// return : map<userId, name>
	// if(FAIL) : return null;
	public List<Map<String, Object>> getLoginedUserList(User user) throws Exception {
		LOG.info("[getLloginedUserList()] START");

		if (!authorization(user.getToken())) {
			LOG.info("[getLoginedUserList()] END with FAIL");
			return null;
		}

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

	private String createAuthToken(User user) {
		return jwt.generateToken(user);
	}

	private boolean authorization(String token) {
		User user = new User();
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			String sql = String.format("SELECT userId FROM pgtDB.CHAT_AUTH_TB WHERE token='%s'", token);
			map = jdb.queryForMap(sql);
			user = new User();
			user.setUserId((String) map.get("userId"));
		} catch (EmptyResultDataAccessException e) {
			LOG.severe("Authorization FAIL");
			return false;
		}

		return jwt.validateToken(token, user);
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
}
