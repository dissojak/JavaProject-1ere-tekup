package com.stoonproduction.jobapplicatio.utils;

import com.stoonproduction.jobapplicatio.models.User;
import io.jsonwebtoken.*;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "9BzJ7YmCjz7N8StoonWasHereWJkQf5LwU6R2M4A0VnPz+X6LhY=";
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    public static String generateToken(String email, User.UserRole role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static Claims validateToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}
