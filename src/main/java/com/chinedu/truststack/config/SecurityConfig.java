package com.chinedu.truststack.config;


import com.chinedu.truststack.security.RestUnauthorizedEntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackages = {"com.chinedu.truststack.security"})
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    public static final String REMEMBER_ME_KEY = "rememberme_key";

    public SecurityConfig() {
        super();
    }

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RestUnauthorizedEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private AccessDeniedHandler restAccessDeniedHandler;

    @Autowired
    private AuthenticationSuccessHandler restAuthenticationSuccessHandler;

    @Autowired
    private AuthenticationFailureHandler restAuthenticationFailureHandler;

    @Autowired
    private RememberMeServices rememberMeServices;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean(name="myAuthenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**", "/index.html", "/login.html", "/home.html", "/resetpassword.html",
                "/changepassword.html", "/signup.html", "/partials/**", "/template/**", "/", "/error/**");
    }

    // Build the request matcher for CSFR protection
    RequestMatcher csrfRequestMatcher = new RequestMatcher() {

        // Disable CSFR protection on the following urls:
        private AntPathRequestMatcher[] requestMatchers = {
            new AntPathRequestMatcher("/user/session"),
            new AntPathRequestMatcher("/user/createaccount"),
            new AntPathRequestMatcher("/user/registrationConfirm"),
            new AntPathRequestMatcher("/user/resetPassword"),
            new AntPathRequestMatcher("/user/processResetPasswordUrl")
        };

        @Override
        public boolean matches(HttpServletRequest request) {
            // If the request match one url the CSFR protection will be disabled
            for (AntPathRequestMatcher rm : requestMatchers) {
                if (rm.matches(request)) { return false; }
            }
            return true;
        }

    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .headers().disable()
            //.csrf().disable()
            .authorizeRequests()
                .antMatchers("/failure").permitAll()
                .antMatchers("/user/session").permitAll()
                .antMatchers("/user/createaccount").permitAll()
                .antMatchers("/user/resetPassword").permitAll()
                .antMatchers("/user/processResetPasswordUrl").permitAll()
                .antMatchers("/user/changePassword").permitAll()
                .antMatchers("/user/registrationConfirm").permitAll()
                .antMatchers("/v2/api-docs").hasAnyAuthority("admin")
                .antMatchers("/users/**").hasAnyAuthority("admin")
                .anyRequest().authenticated()
                .and()
            .exceptionHandling()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler)
                .and()
            .formLogin()
                .loginProcessingUrl("/authenticate")
                .successHandler(restAuthenticationSuccessHandler)
                .failureHandler(restAuthenticationFailureHandler)
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
                .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                .deleteCookies("JSESSIONID")
                .permitAll()
                .and()
            .rememberMe()
                .rememberMeServices(rememberMeServices)
                .key(REMEMBER_ME_KEY)
                .and()
            .csrf()
                .requireCsrfProtectionMatcher(csrfRequestMatcher)
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }
}
