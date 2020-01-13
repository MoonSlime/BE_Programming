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
public class UserInfo {
	private String userId; // user id
	private String password; // password
	private String name; // chat name

//	@Singular("token") // can push(data) oncely
//	List<String> token; // user's auth token(create at login)
	private String token;
}
