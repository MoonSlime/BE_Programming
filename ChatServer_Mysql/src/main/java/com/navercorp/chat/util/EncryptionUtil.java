package com.navercorp.chat.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class EncryptionUtil {
	public static String encryption(String str) {
		if (str == null)return null;
		String hashedPassword = BCrypt.hashpw(str, BCrypt.gensalt());
		return hashedPassword;
	}
	public static boolean check(String str1, String str2) {
		return BCrypt.checkpw(str1, str2);
	}
}
