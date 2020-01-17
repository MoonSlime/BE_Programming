package com.navercorp.chat;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.MappingIterator;
import com.navercorp.chat.mvc.controller.DataBaseController;
import com.navercorp.chat.mvc.model.Room;
import com.navercorp.chat.mvc.model.User;

@Component
@ComponentScan("com.navercorp.chat.mvc.controller")
public class ApplicationDatabase{
	public Hashtable<String, String> loginedUser;	//<userId, token>
	public Hashtable<String, Room> createdRoom;	//<roomId, Room>
	
	@Autowired
	private DataBaseController dbc;
	
	public ApplicationDatabase() {
		System.out.println("--Application DB Created--");
	}
	
	public void init() {
		//get data from DB_Server.
		loginedUser = new Hashtable<String, String>();
		if ((createdRoom = dbc.getCurrentRoomList("KR19815"))==null) {
			createdRoom = new Hashtable<String, Room>();
		}
		
		//TEST
//		for( String key : createdRoom.keySet() ){
//			System.out.println( String.format("키 : %s, 값 : %s", key, createdRoom.get(key)) );
//		}
	}
	
	private boolean updateUserToDbServer() {
		return true;
	}
	
	private boolean updateRoomToDbServer() {
		return true;
	}
	
	public boolean update() {
		updateUserToDbServer();
		updateRoomToDbServer();
		
		return true;
	}
}
