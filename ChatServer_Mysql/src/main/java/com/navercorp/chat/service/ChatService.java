package com.navercorp.chat.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import com.navercorp.chat.ApplicationDatabase;
import com.navercorp.chat.mvc.controller.DataBaseController;
import com.navercorp.chat.mvc.controller.RequestController;
import com.navercorp.chat.mvc.model.Room;
import com.navercorp.chat.util.JwtTokenUtil;

//getMsgListFromRoom(String token, String roomId, int msgId, String orderBy,
//		String msgCnt) {
//	
//}
@Component
public class ChatService {
	private static final Logger LOG = Logger.getLogger(RequestController.class.getName());

	@Autowired
	private ApplicationDatabase appDB;
	@Autowired
	private JwtTokenUtil jwt;
	@Autowired
	private DataBaseController dbc;

	private final static long currentTimeNanosOffset = (System.currentTimeMillis() * 1000000) - System.nanoTime();

	public static long currentTimeNanos() {
		return System.nanoTime() + currentTimeNanosOffset;
	}

	public Map<String, Object> createChatRoom(String token, String rname, String rpassword) {
		if (!authorization(token)) {
			LOG.severe("authorization fail");
			return null;
		}

		// Name 중복 확인.
		if (checkRoomsNameIsDup(rname)) {
			LOG.severe("room name is duplicated");
			return null;
		}

		String roomId = null, userId = jwt.getUserIdFromToken(token), username = null;

		try {
			if (!dbc.createChatRoom(rname, rpassword)) {
				throw new Exception("createChatRoom Fail");
			}

			if ((roomId = dbc.getRoomId(rname)) == null) {
				throw new Exception("getRoomId Fail");
			}

			if ((username = dbc.getUserName(userId)) == null) {
				throw new Exception("getUserName Fail");
			}

			// 채팅방생성시 자동입장.
			if (!dbc.joinChatRoom(roomId, userId, 0)) {
				throw new Exception("joinChatRoom Fail");
			}
		} catch (Exception e) {
			LOG.severe(e.getMessage());
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

		return result;
	}

	public List<Map<String, Object>> getRooms(String token) {
		if (!authorization(token)) {
			LOG.severe("authorization fail");
			return null;
		}

		List<Map<String, Object>> rooms = new Vector<Map<String, Object>>();
		for (String key : appDB.createdRoom.keySet()) {
			Map<String, Object> room = new HashMap<String, Object>();
			room.put("roomId", key);
			room.put("name", appDB.createdRoom.get(key).getName());
			rooms.add(room);
		}
		return rooms;
	}

//	Map<String, Object> response = dbc.joinChatRoom(token, roomId, password);
	public Map<String, Object> joinChatRoom(String token, String roomId, String password) {
		if (!authorization(token)) {
			LOG.severe("authorization fail");
			return null;
		}

		// roomId 존재 확인.
		Room room = null;
		if ((room = roomIsExist(roomId)) == null) {
			LOG.severe("room is not exist");
			return null;
		}

		String userId = jwt.getUserIdFromToken(token);
		String name = null;
		int lastMsgId = 0;

		try {
			// room pwd 확인.
			if (room.getPassword() == null && password == null) {
			} else if (room.getPassword() == null && password != null) {
				throw new Exception("Password is not valid");
			} else if (room.getPassword() != null && password == null) {
				throw new Exception("Password is not valid");
			} else if (!room.getPassword().equals(password)) {
				throw new Exception("Password is not valid");
			}

			if (dbc.checkUserIsRoomMember(roomId, userId)) {
				throw new Exception("User is already room's member");
			}

			if (room.getLastMsgId() != 0) {
				lastMsgId = room.getLastMsgId() + 1;
			}

			if (!dbc.joinChatRoom(roomId, userId, lastMsgId)) {
				throw new Exception("joinChatRoom Fail");
			}

			if ((name = dbc.getUserName(userId)) == null) {
				throw new Exception("getUserName Fail");
			}
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			return null;
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("roomId", roomId);
		result.put("name", room.getName());
		result.put("msgId", lastMsgId);
		Map<String, Object> user = new HashMap<String, Object>();
		user.put("userId", userId);
		user.put("name", name);
		result.put("users", user);

		return result;
	}

//	cs.exitRoom(token, roomId)
	public boolean exitRoom(String token, String roomId) {
		if (!authorization(token)) {
			LOG.severe("authorization fail");
			return false;
		}

		// roomId 존재 확인.
		Room room = null;
		if ((room = roomIsExist(roomId)) == null) {
			LOG.severe("room is not exist");
			return false;
		}

		if (!dbc.exitRoom(roomId, jwt.getUserIdFromToken(token))) {
			LOG.severe("exitRoom Fail");
			return false;
		}

		return true;
	}
	
	public List<Map<String, Object>> getMembers(String token, String roomId) {
		if (!authorization(token)) {
			LOG.severe("authorization fail");
			return null;
		}

		// roomId 존재 확인.
		Room room = null;
		if ((room = roomIsExist(roomId)) == null) {
			LOG.severe("room is not exist");
			return null;
		}
		
		List<Map<String, Object>> users;
		try {
			if (!dbc.checkUserIsRoomMember(roomId, jwt.getUserIdFromToken(token)))
				throw new Exception("User is not room member");

			if ((users = dbc.getMembers(roomId))==null)
				throw new Exception("getMembers Fail");
			
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			return null;
		}

		return users;
	}
	
	public Room updateRoomInfo(String token, String roomId, String rname, String password) {
		if (!authorization(token)) {
			LOG.severe("authorization fail");
			return null;
		}

		// roomId 존재 확인.
		Room room = null;
		if ((room = roomIsExist(roomId)) == null) {
			LOG.severe("room is not exist");
			return null;
		}

		try {
			if (!dbc.checkUserIsRoomMember(roomId, jwt.getUserIdFromToken(token))) {
				throw new Exception("User is Not RoomMember");
			}
			
			if (rname != null) {
				if (checkRoomsNameIsDup(rname)) {
					throw new Exception("Room name is duplicated");
				}
			}
			
			if ((room = dbc.updateRoomInfo(room, roomId, rname, password))==null) {
				throw new Exception("updateRoomInfo Fail");
			}
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			return null;
		}

		appDB.createdRoom.put(roomId, room);
		return room;
	}
	
	public Map<String, Object> sendMsg(String token, String type, String roomId, String to, String text) {
		if (!authorization(token)) {
			LOG.severe("authorization fail");
			return null;
		}

		// roomId 존재 확인.
		Room room = null;
		if ((room = roomIsExist(roomId)) == null) {
			LOG.severe("room is not exist");
			return null;
		}

		int msgId = room.getLastMsgId() + 1;
		long timestamp = currentTimeNanos();

		try {
			String sql = null;

			// Check Sender is Room Member.
			if (!dbc.checkUserIsRoomMember(roomId, jwt.getUserIdFromToken(token)))
				throw new Exception("User is not room member");

			// Check Reciever is Room Member.
			if (to != null) {
				if (!dbc.checkUserIsRoomMember(roomId, to))
					throw new Exception("User is not room member");
			}

			// send Msg
			if (!dbc.sendMsg2(type, roomId, msgId, jwt.getUserIdFromToken(token), to, text, timestamp)) {
				throw new Exception("sendMsg Fail");
			}

			// setRoomsLastMsgId
			if (!dbc.setRoomsLastMsgId(roomId, msgId)) {
				throw new Exception("setRoomsLastMsgId Fail");
			}

			room.setLastMsgId(msgId);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.severe(e.getMessage());
			return null;
		}

		Map<String, Object> msg = new HashMap<String, Object>();
		msg.put("timestamp", timestamp);
		msg.put("from", jwt.getUserIdFromToken(token));
		msg.put("msgId", msgId);
		msg.put("text", text);
		msg.put("type", type);
		if (type.equals("whisper") && to != null) {
			msg.put("to", to);
		}

		return msg;
	}

	public List<Map<String, Object>> getMsgsFromRoom(String token, String roomId, int msgId, String orderBy,
			String msgCnt) {
		if (!authorization(token)) {
			LOG.severe("authorization fail");
			return null;
		}

		// roomId 존재 확인.
		Room room = null;
		if ((room = roomIsExist(roomId)) == null) {
			LOG.severe("room is not exist");
			return null;
		}

		List<Map<String, Object>> msgs = null;
		try {
			// Check Room Member.
			if (!dbc.checkUserIsRoomMember(roomId, jwt.getUserIdFromToken(token)))
				throw new Exception("User is not room member");

			// GetUserName
			String userName = null;
			if ((userName = dbc.getUserName(jwt.getUserIdFromToken(token))) == null)
				throw new Exception("Can not get a userName");

			// Order Parsing
			if (orderBy.equals("increase"))
				orderBy = "ASC";
			else if (orderBy.equals("decrese") || orderBy.equals("decrease"))
				orderBy = "DESC";
			else
				throw new Exception("syntax Error.");

			// getMsgs
			if ((msgs = dbc.getMsgs(roomId, msgId, userName, orderBy, msgCnt)) == null)
				throw new Exception("Can not get a msgs");

			if (msgs.size() == 0)
				throw new Exception("There are no Msg");

			// getLastMsgId
			int lastMsgId = 0;
			for (Map<String, Object> msg : msgs) {
				lastMsgId = Math.max(lastMsgId, (int) msg.get("id"));
			}

			// setLastMsgId
			dbc.setLastMsgId(roomId, jwt.getUserIdFromToken(token), lastMsgId);
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			return null;
		}

		return msgs;
	}

	public List<Map<String, Object>> getTimeline(String token) {
		if (!authorization(token)) {
			LOG.severe("authorization fail");
			return null;
		}

		List<Map<String, Object>> roomIds = null;
		if ((roomIds = dbc.getRoomInfos(jwt.getUserIdFromToken(token))) == null) {
			LOG.severe("user do not have joined room.");
			return null;
		}

		// GetUserName
		String userName = null;
		try {
			if ((userName = dbc.getUserName(jwt.getUserIdFromToken(token))) == null)
				throw new Exception("Can not get a userName");
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			return null;
		}

		List<Map<String, Object>> rooms = new Vector<Map<String, Object>>();
		for (Map<String, Object> map : roomIds) {
			String roomId = Integer.toString((int) map.get("roomId"));
			int msgId = ((int) map.get("lastMsgId")) + 1;
			String orderBy = "ASC";
			String msgCnt = "all";

			try {// getMsgs
				List<Map<String, Object>> msgs = null;
				if ((msgs = dbc.getMsgs(roomId, msgId, userName, orderBy, msgCnt)) == null)
					throw new Exception();

				if (msgs.size() == 0)
					throw new Exception("There are no Msg");

				Map<String, Object> tmp = new HashMap<String, Object>();
				tmp.put("roomId", roomId);
				tmp.put("msgs", msgs);
				rooms.add(tmp);

				// getLastMsgId
				int lastMsgId = 0;
				for (Map<String, Object> msg : msgs) {
					lastMsgId = Math.max(lastMsgId, (int) msg.get("id"));
				}

				// setLastMsgId
				dbc.setLastMsgId(roomId, jwt.getUserIdFromToken(token), lastMsgId);

			} catch (EmptyResultDataAccessException e1) {
				LOG.info("There are no Recent Msg");
			} catch (Exception e2) {
				LOG.info("There are no Recent Msg");
			}
		}

		return rooms;
	}

	public List<Map<String, Object>> getSummary(String token) {
		if (!authorization(token)) {
			LOG.severe("authorization fail");
			return null;
		}

		List<Map<String, Object>> roomIds = null;
		if ((roomIds = dbc.getRoomInfos(jwt.getUserIdFromToken(token))) == null) {
			LOG.severe("user do not have joined room.");
			return null;
		}

		// GetUserName
		String userName = null;
		try {
			if ((userName = dbc.getUserName(jwt.getUserIdFromToken(token))) == null)
				throw new Exception("Can not get a userName");
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			return null;
		}

		List<Map<String, Object>> rooms = new Vector<Map<String, Object>>();
		for (Map<String, Object> map : roomIds) {
			String roomId = Integer.toString((int) map.get("roomId"));
			int msgId = ((int) map.get("lastMsgId")) + 1;
			String orderBy = "DESC";
			String msgCnt = "1";

			try {// getMsgs
				List<Map<String, Object>> msgs = null;
				if ((msgs = dbc.getMsgs(roomId, msgId, userName, orderBy, msgCnt)) == null)
					throw new Exception();

				if (msgs.size() == 0)
					throw new Exception("There are no Msg");

				Map<String, Object> tmp = new HashMap<String, Object>();
				tmp.put("roomId", roomId);
				tmp.put("msg", msgs.get(0));
				rooms.add(tmp);
			} catch (EmptyResultDataAccessException e1) {
				LOG.info("There are no Recent Msg");
			} catch (Exception e2) {
				LOG.info("There are no Recent Msg");
			}
		}

		return rooms;
	}

//========================

	private boolean authorization(String token) {
		String validToken = appDB.loginedUser.get(jwt.getUserIdFromToken(token));
		if (validToken == null)
			return false;
		else if (validToken.equals(token))
			return !jwt.isTokenExpired(validToken);
		else
			return false;
	}

	public boolean checkRoomsNameIsDup(String rname) {
		for (String key : appDB.createdRoom.keySet()) {
			if (appDB.createdRoom.get(key).getName().equals(rname)) {
				LOG.info("[createChatRoom()] END with FAIL");
				return true;
			}
		}
		return false;
	}

	private Room roomIsExist(String roomId) {
		Room room = appDB.createdRoom.get(roomId);
		if (room == null) {
			LOG.severe("room is not exist");
			return null;
		}
		return room;
	}
}
