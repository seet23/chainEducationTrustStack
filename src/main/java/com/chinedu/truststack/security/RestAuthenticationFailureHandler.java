package com.chinedu.truststack.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;


@Component
public class RestAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private MessageSource messageSource;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = messageSource.getMessage("message.badCredentials", null, request.getLocale());

        if (exception.getMessage().contains("not enabled")) {
            errorMessage = messageSource.getMessage("auth.message.disabled", null, request.getLocale());
        } else if (exception.getMessage().equalsIgnoreCase("User account has expired")) {
            errorMessage = messageSource.getMessage("auth.message.expired", null, request.getLocale());
        }

        SecurityUtils.sendError(response, exception, HttpServletResponse.SC_UNAUTHORIZED, errorMessage);
    }
}
