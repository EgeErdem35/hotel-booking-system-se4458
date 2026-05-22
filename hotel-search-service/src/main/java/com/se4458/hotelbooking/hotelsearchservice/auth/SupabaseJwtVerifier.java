package com.se4458.hotelbooking.hotelsearchservice.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SupabaseJwtVerifier {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final String jwtSecret;
    private final String issuer;
    private final String jwksUrl;

    public SupabaseJwtVerifier(
            @Value("${app.auth.jwt-secret}") String jwtSecret,
            @Value("${app.auth.issuer}") String issuer,
            @Value("${app.auth.jwks-url}") String jwksUrl
    ) {
        this.jwtSecret = jwtSecret;
        this.issuer = issuer;
        this.jwksUrl = jwksUrl;
    }

    public Optional<JwtClaims> verify(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!verifySignature(signedJWT)) {
                return Optional.empty();
            }

            String tokenIssuer = signedJWT.getJWTClaimsSet().getIssuer();
            if (StringUtils.hasText(issuer) && !issuer.equals(tokenIssuer)) {
                return Optional.empty();
            }

            Object audience = signedJWT.getJWTClaimsSet().getAudience();
            if (audience == null || !signedJWT.getJWTClaimsSet().getAudience().contains("authenticated")) {
                return Optional.empty();
            }

            UUID subject = UUID.fromString(signedJWT.getJWTClaimsSet().getSubject());
            Instant expiresAt = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();
            if (expiresAt.isBefore(Instant.now())) {
                return Optional.empty();
            }
            return Optional.of(new JwtClaims(subject, expiresAt));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private boolean verifySignature(SignedJWT signedJWT) throws Exception {
        JWSAlgorithm algorithm = signedJWT.getHeader().getAlgorithm();
        if (JWSAlgorithm.HS256.equals(algorithm)) {
            if (!StringUtils.hasText(jwtSecret)) {
                return false;
            }
            return signedJWT.verify(new MACVerifier(jwtSecret));
        }

        if (!StringUtils.hasText(jwksUrl)) {
            return false;
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(jwksUrl)).GET().build();
        String jwksJson = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        JWK key = JWKSet.parse(jwksJson).getKeyByKeyId(signedJWT.getHeader().getKeyID());
        if (key == null) {
            return false;
        }
        JWSVerifier verifier = new DefaultJWSVerifierFactory().createJWSVerifier(
                signedJWT.getHeader(),
                toPublicKey(key)
        );
        return signedJWT.verify(verifier);
    }

    private PublicKey toPublicKey(JWK key) throws Exception {
        if (key instanceof ECKey ecKey) {
            return ecKey.toECPublicKey();
        }
        if (key instanceof RSAKey rsaKey) {
            return rsaKey.toRSAPublicKey();
        }
        throw new IllegalArgumentException("Unsupported JWT signing key type.");
    }
}
