package com.navercorp.chat.service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import com.navercorp.chat.ApplicationDatabase;
import com.navercorp.chat.mvc.controller.DataBaseController;
import com.navercorp.chat.mvc.controller.RequestController;
import com.navercorp.chat.util.JwtTokenUtil;

@Component
public class UserService {
	private static final Logger LOG = Logger.getLogger(RequestController.class.getName());

	@Autowired
	private ApplicationDatabase appDB;
	@Autowired
	private JwtTokenUtil jwt;
	@Autowired
	private DataBaseController dbc;

	public boolean createUser(String userId, String password, String name) {
		if (dbc.checkUserExist(userId, name)) {
			LOG.severe("User is already exist");
			return false;
		}

		if (!dbc.createUser(userId, password, name)) {
			LOG.severe("createUser Fail");
		}

		return true;
	}

//	us.login(userId, password)
	public String login(String userId, String password) {
		// check userId & password
		if (!dbc.checkUserPassword(userId, password)) {
			LOG.severe("checkUserPassword Fail");
			return null;
		}

		String token = createAuthToken(userId);
		appDB.loginedUser.put(userId, token);

		return token;

	}

	public String logout(String token) {
		// 유저 인증.
		if (!authorization(token)) {
			LOG.severe("Authorization Fail");
			return null;
		}

		// token -> userId
		String userId = jwt.getUserIdFromToken(token);
		appDB.loginedUser.remove(userId);

		return userId;
	}

	public String deleteUser(String token) {
		// 유저 인증.
		if (!authorization(token)) {
			LOG.severe("Authorization Fail");
			return null;
		}

		// token -> userId
		String userId = jwt.getUserIdFromToken(token);
		appDB.loginedUser.remove(userId);


		if (!dbc.deleteUser(userId)) {
			LOG.severe("deleteUser Fail");
			return null;
		}

		return userId;
	}
	
	public Map<String, Object> updateUserInfo(String token, String newName) {
		// 유저 인증.
		if (!authorization(token)) {
			LOG.severe("Authorization Fail");
			return null;
		}

		// token -> userId
		String userId = jwt.getUserIdFromToken(token);

		//update userInfo
		if (!dbc.updateUserInfo(userId, newName)) {
			LOG.severe("Update UserInfo Fail");
			return null;
		}

		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("userId", userId);
		ret.put("name", newName);

		return ret;
	}

//==========
	private boolean authorization(String token) {
		String validToken = appDB.loginedUser.get(jwt.getUserIdFromToken(token));
		if (validToken == null)
			return false;
		else if (validToken.equals(token))
			return !jwt.isTokenExpired(validToken);
		else
			return false;
	}

	private String createAuthToken(String userId) {
		String generatedToken = jwt.generateToken(userId);
		System.out.println(userId + " Token => " + generatedToken);
		return generatedToken;
	}
}
