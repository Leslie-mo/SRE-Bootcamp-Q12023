package com.wizeline;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.*;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

public class Methods {

  private static final String SECRET_KEY = "my2w7wjd7yXF64FIADfJxNs1oupTGAuW";
  private static final SecretKey KEY = Keys.hmacShaKeyFor(Base64.decodeBase64(SECRET_KEY));

  public static String generateToken(String username, String password) throws Exception {
    // validate user credentials and retrieve user role
    String role = validateUserCredentials(username, password);
    if (role == null) {
      throw new Exception("Invalid username or password");
    }

    // build JWT token
    Key key = new SecretKeySpec(KEY.getEncoded(), "HS256");
    Date now = new Date();
    Date expiration = new Date(now.getTime() + 3600000); // token expires in 1 hour
    return Jwts.builder()
            .setSubject(username)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .setAudience(key.getAlgorithm())
            .compact();

  }

  public static String accessData(String authorizationHeader) throws Exception {
    // extract token from authorization header
    String token = extractToken(authorizationHeader);
    if (token == null) {
      throw new Exception("Missing or invalid authorization token");
    }

    // validate token and retrieve user data
    Jws<Claims> claims = validateToken(token);
    String username = claims.getBody().getSubject();
    String role = claims.getBody().get("role", String.class);

    // return protected data
    return String.format("Protected data for user %s with role %s", username, role);
  }

  private static String validateUserCredentials(String username, String password) {
    // TODO: retrieve hashed password and salt for username from database and validate against provided password
    // return user role if validation succeeds, null otherwise
    if ("admin".equals(username) && "secret".equals(password)) {
      return "admin";
    } else if ("noadmin".equals(username) && "noPow3r".equals(password)) {
      return "editor";
    } else if ("bob".equals(username) && "thisIsNotAPasswordBob".equals(password)) {
      return "viewer";
    } else {
      return null;
    }
  }

  private static String extractToken(String authorizationHeader) {
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      return authorizationHeader.substring(7);
    } else {
      return null;
    }
  }

  private static Jws<Claims> validateToken(String token) throws Exception {
    try {
      return Jwts.parser().parseClaimsJws(token);

    } catch (Exception e) {
      throw new Exception("Invalid or expired authorization token");
    }
  }
}
