package com.navercorp.chat.mvc.model;

import lombok.Data;

@Data
public class Msg {
	int msgId;
	String text;	//Msg text
	String type;	//talk or whisper
	String from;	//sender userId
	String to;		//reciever userId
}
