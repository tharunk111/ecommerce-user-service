package com.ecommerce.user_service.auth.security;

import com.ecommerce.user_service.auth.JwtClaims;
import com.ecommerce.user_service.auth.JwtService;
import com.ecommerce.user_service.user.AccountStatus;
import com.ecommerce.user_service.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepository;

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			JwtClaims claims = jwtService.parseAndValidate(authorizationHeader.substring(7));
			if (claims.clientCredentials()) {
				AuthenticatedClient principal = new AuthenticatedClient(claims.subject(), claims.authorities());
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						principal,
						null,
						claims.authorities().stream().map(SimpleGrantedAuthority::new).toList());
				SecurityContextHolder.getContext().setAuthentication(authentication);
				filterChain.doFilter(request, response);
				return;
			}

			userRepository.findById(claims.userId())
					.filter(user -> user.getStatus() == AccountStatus.ACTIVE)
					.ifPresent(user -> {
						AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail(), user.getRole());
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
								principal,
								null,
								List.of(new SimpleGrantedAuthority(user.getRole().name())));
						SecurityContextHolder.getContext().setAuthentication(authentication);
					});
		}
		catch (IllegalArgumentException ex) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}
}
