package com.stoonproduction.jobapplicatio.utils;

import com.stoonproduction.jobapplicatio.models.User;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.IOException;

public class AuthMiddleware {
    public static User authenticate(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid token");
        }

        String token = authHeader.substring(7);
        Claims claims = JwtUtil.validateToken(token);

        // Optionally: Fetch user from DB to ensure they still exist
        return new User(
                claims.getSubject(), // email
                null, // password not needed
                User.UserRole.valueOf(claims.get("role", String.class))
        );
    }
}
