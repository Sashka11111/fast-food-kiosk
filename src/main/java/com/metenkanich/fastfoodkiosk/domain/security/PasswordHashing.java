package com.metenkanich.fastfoodkiosk.domain.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHashing {

  private static PasswordHashing instance;

  private PasswordHashing() {
  }

  public static PasswordHashing getInstance() {
    if (instance == null) {
      instance = new PasswordHashing();
    }
    return instance;
  }

  public String hashedPassword(String password) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      byte[] encodedHash = messageDigest.digest(password.getBytes());

      StringBuilder hexString = new StringBuilder();
      for (byte b : encodedHash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }

      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }
}