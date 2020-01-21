package com.navercorp.chat.mvc.controller;

import java.sql.Timestamp;
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
import com.navercorp.chat.util.EncryptionUtil;
import com.navercorp.chat.util.JwtTokenUtil;

@Component
@ComponentScan("com.navercorp.chat.dao")
public class DataBaseController {
	private static final Logger LOG = Logger.getLogger(DataBaseController.class.getName());

	@Autowired
	private JdbcTemplate jdb;

	public DataBaseController() {
		LOG.info("DataBaseController Created");
	}

// USER_DB_CONTROLL ==================================================	
	// 유저 생성.
	public Boolean createUser(String userId, String password, String name) {
		try {
			String sql = String.format("INSERT INTO pgtDB.CHAT_USER_TB (userId, password) VALUES ('%s', '%s')", userId,
					password);
			if (jdb.update(sql) == 0)
				throw new Exception("INSERT CHAT_USER_TB FAIL");

			sql = String.format("INSERT INTO pgtDB.CHAT_NAME_TB (userId, name) VALUES ('%s', '%s')", userId, name);
			if (jdb.update(sql) == 0)
				throw new Exception("INSERT CHAT_NAME_TB FAIL");
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			return false;
		}
		return true;
	}

	// 유저 회원탈퇴.
	public boolean deleteUser(String userId) {
		try {// CHAT_USER_TB's userId is (AUTH_TB & NAME_TB)'s Foriegn key. on delete cascade
			String sql = String.format("DELETE FROM pgtDB.CHAT_USER_TB WHERE userId='%s'", userId);
			if (jdb.update(sql) == 0)
				throw new Exception();
		} catch (Exception e) {// DELETE 실패.
			return false;
		}
		return true;
	}

	// 유저정보 변경.
	public synchronized boolean updateUserInfo(String userId, String name) {
		try {
			String sql = String.format("UPDATE pgtDB.CHAT_NAME_TB SET name = '%s' WHERE userId='%s'", name, userId);
			if (jdb.update(sql) == 0)
				throw new Exception("Name is duplicated");// => Can't input duplicate name.
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			return false;
		}
		return true;
	}

	// 유저 목록 조회
	public List<Map<String, Object>> getUsers(String token) {
		List<Map<String, Object>> users = null;
		try {
			String sql = String.format("SELECT userId, name FROM pgtDB.CHAT_NAME_TB");
			users = jdb.queryForList(sql);
		} catch (Exception e) {
			LOG.severe("getUsers Fail");
			return null;
		}
		return users;
	}

// ROOM_DB_CONTROLL ===================================================

	public boolean exitRoom(String roomId, String userId) {
		try {
			String sql = String.format("DELETE FROM pgtDB.CHAT_JOIN_TB WHERE roomId='%s' AND userId='%s'", roomId,
					userId);
			if (jdb.update(sql) == 0) {
				throw new Exception("user is not exist in the room");
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public List<Map<String, Object>> getMembers(String roomId) {
		List<Map<String, Object>> users = null;
		try {
			String sql_inner = String.format("SELECT userId FROM pgtDB.CHAT_JOIN_TB WHERE roomId='%s'", roomId);
			String sql_outer = String.format("SELECT * FROM pgtDB.CHAT_NAME_TB WHERE userId IN (%s)", sql_inner);
			users = jdb.queryForList(sql_outer);
		} catch (Exception e) {
			return null;
		}
		return users;
	}

	public synchronized Room updateRoomInfo(Room room, String roomId, String rname, String password) {
		try {
			String sql = null;

			if (rname != null) {
				sql = String.format("UPDATE pgtDB.CHAT_ROOM_TB SET name='%s' WHERE roomId='%s'", rname, roomId);
				if (jdb.update(sql) == 0)
					throw new Exception();
				room.setName(rname);
			}

			if (room.getPassword() == null && password != null) {
				room.setPassword(password);
				sql = String.format("UPDATE pgtDB.CHAT_ROOM_TB SET password='%s' WHERE roomId='%s'", password, roomId);
			} else if (room.getPassword() != null && password == null) {
				room.setPassword(null);
				sql = String.format("UPDATE pgtDB.CHAT_ROOM_TB SET password=NULL WHERE roomId='%s'", roomId);
			}
			if (jdb.update(sql) == 0)
				throw new Exception();
		} catch (Exception e) {
			return null;
		}

		return room;
	}

// Func() =======================================================
	// 유저 존재 확인.
	public boolean checkUserExist(String userId, String name) {
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

//	// 유저 존재 확인 & 유저 비밀번호의 적합성 검사.
//	public boolean checkUserPassword(String userId, String password) {
//		try {
//			String sql = String.format("SELECT userId FROM pgtDB.CHAT_USER_TB WHERE userID='%s' AND password='%s'",
//					userId, password);
//			Map<String, Object> map = jdb.queryForMap(sql);
//		} catch (EmptyResultDataAccessException e) {
//			LOG.severe("There's no appropriate User");
//			return false;
//		}
//		return true;
//	}
//	

	// 유저 존재 확인 & 유저 비밀번호의 적합성 검사.
	public boolean checkUserPassword(String userId, String password) {
		try {
			String sql = String.format("SELECT password FROM pgtDB.CHAT_USER_TB WHERE userID='%s'", userId);
			Map<String, Object> map = jdb.queryForMap(sql);
			return EncryptionUtil.check(password, (String)map.get("password"));
		} catch (Exception e) {
			LOG.severe("There's no appropriate User");
			return false;
		}
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

	public String getUserName(String userId) {
		String userName = null;

		try {
			String sql = String.format("SELECT name FROM pgtDB.CHAT_NAME_TB  WHERE userId='%s'", userId);
			userName = (String) jdb.queryForMap(sql).get("name");
		} catch (Exception e) {
			return null;
		}

		return userName;
	}

	public boolean checkUserIsRoomMember(String roomId, String userId) {
		try {
			// Check Room Member.
			String sql = String.format("SELECT userId FROM pgtDB.CHAT_JOIN_TB WHERE roomId='%s' AND userId='%s'",
					roomId, userId);
			if (jdb.queryForMap(sql).size() == 0)
				throw new Exception("user is not memeber of this room");
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public List<Map<String, Object>> getMsgs(String roomId, int msgId, String userName, String orderBy, String msgCnt) {
		List<Map<String, Object>> msgs = null;
		try {
			String sql = null;
			if (msgCnt.equals("all")) {
				sql = String.format(
						"SELECT timestamp, CHAT_TALK_TB.from, type, msgId AS id, text FROM pgtDB.CHAT_TALK_TB WHERE roomId='%s' AND msgId>='%d' AND (CHAT_TALK_TB.to IS NULL OR CHAT_TALK_TB.to = '%s')",
						roomId, msgId, userName);
			} else {
				sql = String.format(
						"SELECT timestamp, CHAT_TALK_TB.from, type, msgId AS id, text FROM pgtDB.CHAT_TALK_TB WHERE roomId='%s' AND msgId>='%d' AND (CHAT_TALK_TB.to IS NULL OR CHAT_TALK_TB.to = '%s') ORDER BY msgId %s LIMIT %s",
						roomId, msgId, userName, orderBy, msgCnt);
			}
			msgs = jdb.queryForList(sql);

			if (msgs.size() == 0)
				throw new Exception("There are no Msg");
		} catch (Exception e) {
			LOG.info("getMsgs() Fail");
			return null;
		}

		return msgs;
	}

	public List<Map<String, Object>> getRoomInfos(String userId) {
		List<Map<String, Object>> roomIds = null;

		try {
			String sql = String.format("SELECT roomId, lastMsgId FROM pgtDB.CHAT_JOIN_TB WHERE userId='%s'", userId);
			roomIds = jdb.queryForList(sql);
		} catch (Exception e) {
			LOG.info(e.getMessage());
			return null;
		}

		return roomIds;
	}

	public synchronized boolean setUsersLastMsgId(String roomId, String userId, int lastMsgId) {
		try {
			String sql = String.format("UPDATE pgtDB.CHAT_JOIN_TB SET lastMsgId='%d' WHERE roomId='%s' AND userId='%s'",
					lastMsgId, roomId, userId);
			if (jdb.update(sql) == 0)
				throw new Exception("update fail");
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			return false;
		}

		return true;
	}

	public synchronized boolean setRoomsLastMsgId(String roomId, int lastMsgId) {
		try {
			String sql = String.format("UPDATE pgtDB.CHAT_ROOM_TB SET lastMsgId='%d' WHERE roomId='%s'", lastMsgId,
					roomId);
			if (jdb.update(sql) == 0)
				throw new Exception();
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public boolean sendMsg(String type, String roomId, int msgId, String from, String to, String text, long timestamp) {
		String sql = null;
		if (type.equals("talk") && to == null) {
			sql = String.format(
					"INSERT INTO `pgtDB`.`CHAT_TALK_TB` (`roomId`, `msgId`, `type`, `from`, `text`, `timestamp`) VALUES ('%s','%d','%s','%s','%s','%s')",
					roomId, msgId, type, from, text, Long.toString(timestamp));
		} else if (type.equals("whisper") && to != null) {
			sql = String.format(
					"INSERT INTO `pgtDB`.`CHAT_TALK_TB` (`roomId`, `msgId`, `type`, `from`, `to`, `text`, `timestamp`) VALUES ('%s','%d','%s','%s', '%s','%s','%s')",
					roomId, msgId, type, from, to, text, Long.toString(timestamp));
		} else
			return false;

		try {
			if (jdb.update(sql) == 0)
				throw new Exception("sendMsg is Fail");
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			return false;
		}

		return true;
	}

	public boolean createChatRoom(String rname, String rpassword) {
		try {
			String sql = null;
			if (rpassword == null) {
				sql = String.format("INSERT INTO pgtDB.CHAT_ROOM_TB (name, lastMsgId) VALUES ('%s', '0')", rname);
			} else {
				sql = String.format("INSERT INTO pgtDB.CHAT_ROOM_TB (name,password, lastMsgId) VALUES ('%s','%s','0')",
						rname, rpassword);
			}
			if (jdb.update(sql) == 0)
				throw new Exception();
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public String getRoomId(String rname) {
		String roomId = null;
		try {
			String sql = String.format("SELECT roomID FROM pgtDB.CHAT_ROOM_TB WHERE name='%s'", rname);
			roomId = Integer.toString((int) jdb.queryForMap(sql).get("roomId"));
		} catch (Exception e) {
			return null;
		}
		return roomId;
	}

	public boolean joinChatRoom(String roomId, String userId, int joinMsgId) {
		try {
			String sql = String.format(
					"INSERT INTO pgtDB.CHAT_JOIN_TB (roomId, userId, joinMsgId, lastMsgId) VALUES ('%s','%s',%d,%d)",
					roomId, userId, joinMsgId, joinMsgId);
			if (jdb.update(sql) == 0) {
				throw new Exception("joinChatRoomFail");
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}
