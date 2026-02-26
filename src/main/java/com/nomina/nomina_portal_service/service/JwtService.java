package com.nomina.nomina_portal_service.service;

import com.nomina.nomina_portal_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
	private final String secret;
	private final long expirationSeconds;
	private SecretKey signingKey;

	public JwtService(
		@Value("${security.jwt.secret}") String secret,
		@Value("${security.jwt.expiration-seconds}") long expirationSeconds
	) {
		this.secret = secret;
		this.expirationSeconds = expirationSeconds;
		this.signingKey = Keys.hmacShaKeyFor(resolveKeyBytes(secret));
	}

	public long getExpirationSeconds() {
		return expirationSeconds;
	}

	public String generateToken(User user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(expirationSeconds);
		Map<String, Object> claims = new HashMap<>();
		claims.put("username", user.getUsername());
		claims.put("is_super_user", user.isSuperUser());
		claims.put("is_admin_user", user.isAdminUser());
		claims.put("is_active_user", user.isActiveUser());

		return Jwts.builder()
			.subject(Long.toString(user.getId()))
			.claims(claims)
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.signWith(signingKey, SignatureAlgorithm.HS256)
			.compact();
	}

	public Claims parseToken(String token) {
		return Jwts.parser()
			.verifyWith(signingKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private byte[] resolveKeyBytes(String secret) {
		byte[] decoded = null;
		try {
			decoded = Decoders.BASE64URL.decode(secret);
		} catch (RuntimeException ignored) {
			decoded = null;
		}
		if (decoded != null && decoded.length >= 32) {
			return decoded;
		}

		try {
			decoded = Decoders.BASE64.decode(secret);
		} catch (RuntimeException ignored) {
			decoded = null;
		}
		if (decoded != null && decoded.length >= 32) {
			return decoded;
		}

		byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
		if (raw.length >= 32) {
			return raw;
		}

		try {
			return MessageDigest.getInstance("SHA-256").digest(raw);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Unable to initialize JWT signing key.", ex);
		}
	}
}
