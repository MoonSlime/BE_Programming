package com.navercorp.chat.mvc.model;

import lombok.Data;

@Data
public class Room {
	int roomId;
	String name; // Room Name.
	String password; //Room Password
	String lastMsgId; //Room's last msg
}
