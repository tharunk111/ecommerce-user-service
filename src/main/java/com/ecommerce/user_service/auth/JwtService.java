package com.ecommerce.user_service.auth;

import com.ecommerce.user_service.user.User;
import com.ecommerce.user_service.user.UserRole;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.Utilities;

@Service
public class JwtService {

	private static final String TOKEN_USE_USER = "user";
	private static final String TOKEN_USE_CLIENT_CREDENTIALS = "client_credentials";

	private final JwtProperties properties;
	private final JwtEncoder jwtEncoder;
	private final JwtDecoder jwtDecoder;

	public JwtService(JwtProperties properties) throws IOException, GeneralSecurityException {
		this.properties = properties;
		RSAPrivateKey privateKey = loadPrivateKey(properties);
		RSAPublicKey publicKey = loadPublicKey(properties);
		this.jwtEncoder = createJwtEncoder(publicKey, privateKey);
		this.jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
	}

	public TokenDetails generateAccessToken(User user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(properties.getAccessTokenTtl());
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.getIssuer())
				.subject(user.getEmail())
				.issuedAt(now)
				.expiresAt(expiresAt)
				.claim("tokenUse", TOKEN_USE_USER)
				.claim("userId", user.getId().toString())
				.claim("email", user.getEmail())
				.claim("role", user.getRole().name())
				.claim("authorities", List.of(user.getRole().name()))
				.build();

		return encode(claims, expiresAt);
	}

	public TokenDetails generateClientCredentialsToken(String clientId, List<String> authorities) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(properties.getAccessTokenTtl());
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.getIssuer())
				.subject(clientId)
				.issuedAt(now)
				.expiresAt(expiresAt)
				.claim("tokenUse", TOKEN_USE_CLIENT_CREDENTIALS)
				.claim("clientId", clientId)
				.claim("authorities", authorities)
				.build();

		return encode(claims, expiresAt);
	}

	private TokenDetails encode(JwtClaimsSet claims, Instant expiresAt) {
		JwsHeader headers = JwsHeader.with(SignatureAlgorithm.RS256).type("JWT").build();
		Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims));
		return new TokenDetails(jwt.getTokenValue(), expiresAt);
	}

	public JwtClaims parseAndValidate(String token) {
		try {
			Jwt jwt = jwtDecoder.decode(token);
			if (!properties.getIssuer().equals(jwt.getClaimAsString("iss"))) {
				throw new IllegalArgumentException("Invalid JWT issuer");
			}

			Instant expiresAt = jwt.getExpiresAt();
			if (expiresAt == null) {
				throw new IllegalArgumentException("JWT token has no expiry");
			}
			if (expiresAt.isBefore(Instant.now())) {
				throw new IllegalArgumentException("JWT token has expired");
			}

			String tokenUse = jwt.getClaimAsString("tokenUse");
			List<String> authorities = readAuthorities(jwt);
			if (TOKEN_USE_CLIENT_CREDENTIALS.equals(tokenUse)) {
				return new JwtClaims(
						null,
						jwt.getSubject(),
						null,
						null,
						authorities,
						true,
						expiresAt);
			}

			UserRole role = UserRole.valueOf(jwt.getClaimAsString("role"));
			return new JwtClaims(
					UUID.fromString(jwt.getClaimAsString("userId")),
					jwt.getSubject(),
					jwt.getClaimAsString("email"),
					role,
					authorities.isEmpty() ? List.of(role.name()) : authorities,
					false,
					expiresAt);
		}
		catch (RuntimeException ex) {
			throw new IllegalArgumentException("Invalid JWT token", ex);
		}
	}

	private List<String> readAuthorities(Jwt jwt) {
		Object authorities = jwt.getClaims().get("authorities");
		if (!(authorities instanceof List<?> authorityValues)) {
			return List.of();
		}
		return authorityValues.stream()
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.toList();
	}

	private JwtEncoder createJwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
		RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
		JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
		return new NimbusJwtEncoder(jwkSource);
	}

	private RSAPrivateKey loadPrivateKey(JwtProperties properties) throws IOException, GeneralSecurityException {
		byte[] keyBytes = readPem(properties.getPrivateKeyLocation().getContentAsString(StandardCharsets.UTF_8));
		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
		return (RSAPrivateKey) privateKey;
	}

	private RSAPublicKey loadPublicKey(JwtProperties properties) throws IOException, GeneralSecurityException {
		byte[] keyBytes = readPem(properties.getPublicKeyLocation().getContentAsString(StandardCharsets.UTF_8));
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
		return (RSAPublicKey) publicKey;
	}

	private byte[] readPem(String pem) {
		String normalized = pem
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replace("-----BEGIN PUBLIC KEY-----", "")
				.replace("-----END PUBLIC KEY-----", "")
				.replaceAll("\\s", "");
		return Base64.getDecoder().decode(normalized);
	}

	public record TokenDetails(String token, Instant expiresAt) {
	}
}
