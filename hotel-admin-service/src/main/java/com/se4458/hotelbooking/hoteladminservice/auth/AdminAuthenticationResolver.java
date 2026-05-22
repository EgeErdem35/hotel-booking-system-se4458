package com.se4458.hotelbooking.hoteladminservice.auth;

import com.se4458.hotelbooking.hoteladminservice.common.UnauthorizedException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminAuthenticationResolver {

    private final SupabaseJwtVerifier jwtVerifier;

    public AdminAuthenticationResolver(SupabaseJwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    public AdminAuthentication resolve(String authorizationHeader, UUID forwardedUserId) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization Bearer token is required.");
        }
        String token = authorizationHeader.substring("Bearer ".length());
        JwtClaims claims = jwtVerifier.verifyRequired(token);
        if (forwardedUserId != null && !forwardedUserId.equals(claims.subject())) {
            throw new UnauthorizedException("X-User-Id must match the Supabase JWT subject.");
        }
        return new AdminAuthentication(claims.subject(), token);
    }
}
