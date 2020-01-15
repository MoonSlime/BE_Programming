package com.navercorp.chat.mvc.model;

import lombok.Data;

@Data
public class Room {
	String roomId;
	String name; // Room Name.
	String password; //Room Password
	int lastMsgId; //Room's last msg
}
