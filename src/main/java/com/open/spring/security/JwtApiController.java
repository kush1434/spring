package com.open.spring.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin
public class JwtApiController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private PersonDetailsService personDetailsService;

	@Value("${server.servlet.context-path:}")
	private String contextPath;

    // @CrossOrigin(origins = "http://127.0.0.1:4500")
	@CrossOrigin(origins = "http://localhost:4500")
	@PostMapping("/authenticate")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody Person authenticationRequest) throws Exception {
		authenticate(authenticationRequest.getUid(), authenticationRequest.getPassword());
		final UserDetails userDetails = personDetailsService
				.loadUserByUsername(authenticationRequest.getUid());

		// Get the roles of the user
		List<String> roles = userDetails.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toList());

		// Generate the token with the roles
		final String token = jwtTokenUtil.generateToken(userDetails, roles);

		if (token == null) {
			return new ResponseEntity<>("Token generation failed", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// Build cookie with development-friendly settings
		// For localhost: allow HTTP and SameSite=Lax
		// For production: require HTTPS and SameSite=None; Secure
		ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("jwt_java_spring", token)
			.httpOnly(false)
			.path("/")
			.maxAge(3600);

		// Check if running on localhost (development) or production
		boolean isLocalhost = contextPath == null || contextPath.isEmpty();
		if (isLocalhost) {
			// Development: allow HTTP and use Lax SameSite
			cookieBuilder.secure(false).sameSite("Lax");
		} else {
			// Production: require HTTPS and use None; Secure
			cookieBuilder.secure(true).sameSite("None; Secure");
		}

		final ResponseCookie tokenCookie = cookieBuilder.build();

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(authenticationRequest.getUid() + " was authenticated successfully");
	}

	private void authenticate(String username, String password) throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	@RestController
	public class CustomLogoutController {

    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
	
		@PostMapping("/my/logout")
		public String performLogout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
			// Perform logout using SecurityContextLogoutHandler
			logoutHandler.logout(request, response, authentication);
	
			// Expire the JWT token immediately by setting a past expiration date
			ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("jwt_java_spring", "")
				.httpOnly(false)
				.path("/")
				.maxAge(0);  // Set maxAge to 0 to expire the cookie immediately

			// Use development-friendly settings for localhost
			boolean isLocalhost = contextPath == null || contextPath.isEmpty();
			if (isLocalhost) {
				cookieBuilder.secure(false).sameSite("Lax");
			} else {
				cookieBuilder.secure(true).sameSite("None; Secure");
			}

			ResponseCookie cookie = cookieBuilder.build();
	
			// Set the cookie in the response to effectively "remove" the JWT
			response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	
			// Optional: You can also clear the "Authorization" header if needed
			response.setHeader("Authorization", null);
	
			// Redirect user to home page after logout
			return "redirect:/home";
		}
}

}




	

