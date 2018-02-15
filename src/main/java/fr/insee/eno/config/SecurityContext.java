package fr.insee.eno.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityContext extends WebSecurityConfigurerAdapter {

	@Value("${fr.insee.eno.force.ssl}")
	Boolean requiresSSL;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		if (requiresSSL) {
			http.antMatcher("/**").requiresChannel().anyRequest().requiresSecure();
		}

	}

	
}
