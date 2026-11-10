package com.library.service;

import com.library.exception.LibraryException;
import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Passwords {
  private static final int ITERATIONS = 210_000;

  private Passwords() {}

  public static String hash(String password) {
    if (password == null || password.length() < 8 || password.length() > 128)
      throw new LibraryException("Password must contain 8 to 128 characters.");
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return ITERATIONS
        + ":"
        + Base64.getEncoder().encodeToString(salt)
        + ":"
        + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
  }

  public static boolean verify(String password, String encoded) {
    if (password == null || password.length() > 128) return false;
    try {
      String[] parts = encoded.split(":");
      int rounds = Integer.parseInt(parts[0]);
      if (parts.length != 3 || rounds < 100_000 || rounds > 1_000_000) return false;
      return MessageDigest.isEqual(
          Base64.getDecoder().decode(parts[2]),
          derive(password, Base64.getDecoder().decode(parts[1]), rounds));
    } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  private static byte[] derive(String password, byte[] salt, int rounds) {
    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, rounds, 256);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new LibraryException("Password protection is unavailable.", e);
    } finally {
      spec.clearPassword();
    }
  }
}
