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

	private Room roomIsExist(String roomId) {
		Room room = appDB.createdRoom.get(roomId);
		if (room == null) {
			LOG.severe("room is not exist");
			return null;
		}
		return room;
	}
}
