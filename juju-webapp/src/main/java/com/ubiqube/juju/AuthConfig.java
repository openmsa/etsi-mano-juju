package com.ubiqube.juju;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

//@EnableWebSecurity
@Configuration
public class AuthConfig {
//	@SuppressWarnings("static-method")
//	@Bean
//	SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
//		http
//				.authorizeHttpRequests()
//				.requestMatchers("/error").permitAll()
//				.requestMatchers("/**").hasAnyAuthority("SCOPE_juju")
//				.anyRequest().authenticated()
//				.and()
//				.oauth2ResourceServer()
//				.jwt();
//		return http.build();
//	}

	@SuppressWarnings("static-method")
	@Bean
	WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring()
				.requestMatchers("/**");
	}
}
