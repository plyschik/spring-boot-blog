package io.plyschik.springbootblog.configuration;

import io.plyschik.springbootblog.security.ApplicationPermissionEvaluator;
import io.plyschik.springbootblog.service.UserDetailsServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsServiceImplementation userDetailsService;

    @Autowired
    private ApplicationPermissionEvaluator applicationPermissionEvaluator;

    @Value("${security.remember-me.key}")
    private String rememberMeKey;

    @Value("${security.remember-me.token-validity-seconds}")
    private int rememberMeTokenValiditySeconds;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/", "/auth/**").permitAll()
            .antMatchers("/dashboard/**").hasRole("ADMINISTRATOR")
            .and()
            .formLogin()
            .loginPage("/auth/signin")
            .loginProcessingUrl("/auth/signin")
            .defaultSuccessUrl("/")
            .failureUrl("/auth/signin?failure")
            .and()
            .rememberMe()
            .userDetailsService(userDetailsService)
            .key(rememberMeKey)
            .tokenValiditySeconds(rememberMeTokenValiditySeconds)
            .and()
            .logout()
            .logoutUrl("/auth/signout")
            .logoutSuccessUrl("/");
    }

    @Override
    public void configure(WebSecurity web) {
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(applicationPermissionEvaluator);

        web.expressionHandler(expressionHandler);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userDetailsService);

        return authenticationProvider;
    }
}
