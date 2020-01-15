package com.navercorp.chat.mvc.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;

@Data
//@Builder
public class User {
	private String userId; // user id
	private String password; // password must be hidden.!!
	private String name; // chat name

//	@Singular("token") // can push(data) oncely
//	List<String> token; // user's auth token(create at login)
	private String token;
}
