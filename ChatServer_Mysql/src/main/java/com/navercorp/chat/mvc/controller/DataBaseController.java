package com.navercorp.chat.mvc.controller;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.navercorp.chat.mvc.model.UserInfo;

@Component
@ComponentScan
public class DataBaseController {
	private static final Logger LOG = Logger.getLogger(DataBaseController.class.getName());

//	@Autowired
//	private MySqlConfig mysql;

	@Autowired
	private JdbcTemplate jdb;

	public DataBaseController() {
		LOG.info("DBC Created");
	}

	// 유저 생성(회원가입)
	// params : userId, password, name
	// return : SUCCESS or FAIL
	public Boolean signup(UserInfo user) throws Exception {
		LOG.info("[signup()] START");

		if (checkUserExist(user.getUserId())) {
			LOG.severe("[signup()] END with FAIL, Because User Already Exist");
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
	public UserInfo login(UserInfo user) throws Exception {
		LOG.info("[login()] START");

		// check userId & password
		if (!checkUserPassword(user.getUserId(), user.getPassword())) {
			LOG.info("[login()] FAIL");
			return null;
		}

		// create token
		String token = createAuthToken();

		// INSERT TOKEN to CHAT_AUTH_TB. or UPDATE TOKEN to CHAT_AUTH_TB.
		if (getAuthTokenFromDB(user.getUserId()) == null) {// Else if (token is not exist) INSERT TOKEN.
			if (!insertAuthTokenToDB(user.getUserId(), token)) {
				LOG.severe("[login()] END with FAIL");
				return null;
			}
		} else {// If (token is already exist) UPDATE TOKEN
			if (!updateAuthTokenToDB(user.getUserId(), token)) {
				LOG.severe("[login()] END with FAIL");
				return null;
			}
		}

		// set token.
		user.setToken(token);

		// return token;
		LOG.info("[login()] END with SUCCESS");
		return user;
	}

	// 유저 로그아웃.
	// params : authToken
	// return : userId
	// if(FAIL) : return null;
	public UserInfo logout(UserInfo user) throws Exception {
		LOG.info("[logout()] START");
		// DELETE FROM `pgtDB`.`CHAT_AUTH_TB` WHERE (`userId` = 'testUser1');

		// CheckingUserLogin && getUserIdUsingToken.
		String userId = null;
		if ((userId = getUserIdUsingToken(user.getToken())) == null) {
			LOG.severe("[logout()] END with FAIL");
			return null;
		}

		// setUserId.
		user.setUserId(userId);

		// DELETE Field.
		try {
			String sql = String.format("DELETE FROM pgtDB.CHAT_AUTH_TB WHERE userId='%s' AND token = '%s'", userId,
					user.getToken());
			System.out.println("Update Field = " + jdb.update(sql));
		} catch (EmptyResultDataAccessException e) {// DELETE 실패.
			LOG.info("There's no same userId & token. It can't deleted");
			LOG.severe("[logout()] END with FAIL");
			return null;
		}

		LOG.info("[logout()] END with SUCCESS");
		return user;
	}
	
	public UserInfo signout(UserInfo user) throws Exception {
		//DELETE FROM `pgtDB`.`CHAT_USER_TB` WHERE (`userId` = 'testUser1');
		
		return user;
	}

// Func() =======================================================
	public Boolean test() throws Exception {
		System.out.println("DBC.signup");
		List<Map<String, Object>> queryForList = jdb.queryForList("SELECT * FROM CHAT_USERS_TB");
		System.out.println(queryForList);

		return true;
	}

	// 유저 존재 확인.
	private boolean checkUserExist(String userId) {
		Map<String, Object> map = null;
		try {
			String sql = String.format("SELECT userId FROM pgtDB.CHAT_USER_TB WHERE userId='%s'", userId);
			map = jdb.queryForMap(sql);
		} catch (EmptyResultDataAccessException e) {// User가 없는 경우.
			LOG.info("There's no same userId. It can create new user");
		}

		if (map == null)
			return false;
		else
			return true;
	}

	private boolean checkUserLogin(String token) {
		Map<String, Object> map = null;
		try {
			String sql = String.format("SELECT userId FROM pgtDB.CHAT_USER_TB WHERE token='%s'", token);
			map = jdb.queryForMap(sql);
		} catch (EmptyResultDataAccessException e) {
			LOG.severe("There's no user. having same token");
			return false;
		}

		return true;
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

	private String createAuthToken() {
		return "TmpAuthToken";
	}

	private boolean insertAuthTokenToDB(String userId, String token) {
		// INSERT INTO `pgtDB`.`CHAT_AUTH_TB` (`userId`, `token`) VALUES ('testUserId1',
		// 'A');
		try {
			String sql = String.format("INSERT INTO pgtDB.CHAT_AUTH_TB (userId, token) VALUES ('%s', '%s')", userId,
					token);
			System.out.println("Update Field = " + jdb.update(sql));
		} catch (EmptyResultDataAccessException e) {
			LOG.info("INSERT AuthToken  is FAIL");
			return false;
		}

		return true;
	}

	private boolean updateAuthTokenToDB(String userId, String token) {
		// UPDATE `pgtDB`.`CHAT_AUTH_TB` SET `token` = 'B' WHERE (`userId` =
		// 'testUserId1');
		try {
			String sql = String.format("UPDATE pgtDB.CHAT_AUTH_TB SET token = '%s' WHERE userId = '%s'", token, userId);
			System.out.println("Update Field = " + jdb.update(sql));
		} catch (EmptyResultDataAccessException e) {
			LOG.info("UPDATE AuthToken  is FAIL");
			return false;
		}

		return true;
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

	private String getUserIdUsingToken(String token) {
		Map<String, Object> map = null;
		try {
			String sql = String.format("SELECT userId FROM pgtDB.CHAT_AUTH_TB WHERE token='%s'", token);
			map = jdb.queryForMap(sql);
		} catch (EmptyResultDataAccessException e) {
			LOG.severe("There's no user. having same token");
			return null;
		}

		return (String) map.get("userId");
	}
}
