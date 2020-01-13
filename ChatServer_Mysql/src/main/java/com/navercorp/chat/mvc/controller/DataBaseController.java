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
		}
		try {
			String sql = String.format("INSERT INTO pgtDB.CHAT_NAME_TB (userId, name) VALUES ('%s', '%s')",
					user.getUserId(), user.getName());
			System.out.println("Update Field = " + jdb.update(sql));
		} catch (DataAccessException e) {
			LOG.severe("INSERT CHAT_USER_TB FAIL");
		}
		
		// 성공시 return true;
		LOG.info("[signup()] END with SUCCESS");
		return true;
	}

	public Boolean test() throws Exception {
		System.out.println("DBC.signup");
		List<Map<String, Object>> queryForList = jdb.queryForList("SELECT * FROM CHAT_USERS_TB");
		System.out.println(queryForList);

		return true;
	}

// Func() =======================================================

	// 유저 존재 확인.
	private boolean checkUserExist(String userId) {
		Map<String, Object> map = null;
		try {
			map = jdb.queryForMap(String.format("SELECT userId FROM pgtDB.CHAT_USER_TB WHERE userId='%s'", userId));
		} catch (EmptyResultDataAccessException e) {// User가 없는 경우.
			LOG.info("There's no same userId. It can create new user");
//			map = new HashMap<String, Object>();
//			map.put("userId", "NULL");
		}

		if (map == null)
			return false;
		else
			return true;

//		// 있을경우 return true;
//		if (map.size() > 0) {
//			return true;
//		}
//		else return false;
	}
}
