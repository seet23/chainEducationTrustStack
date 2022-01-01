package com.chinedu.truststack.web.controller;

import com.chinedu.truststack.model.Error;
import com.chinedu.truststack.model.OnRegistrationCompleteEvent;
import com.chinedu.truststack.model.Response;
import com.chinedu.truststack.persist.entity.Authority;
import com.chinedu.truststack.persist.entity.PasswordResetToken;
import com.chinedu.truststack.persist.entity.VerificationToken;
import com.chinedu.truststack.persist.repo.AuthorityRepo;
import com.chinedu.truststack.persist.repo.UserRepo;
import com.chinedu.truststack.persist.entity.User;
import com.chinedu.truststack.security.SecurityUtils;
import com.chinedu.truststack.service.UserService;
import com.chinedu.truststack.utils.ResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.*;

@RestController
@Api(description = "Users management API")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private static boolean CONFIRM_REGISTRATION = true;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthorityRepo authRepo;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired @Qualifier("myAuthenticationManager")
    private AuthenticationManager authMgr;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MessageSource messages;

    @Autowired
    private JavaMailSender mailSender;

    private ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public @ResponseBody List<User> usersList() {
        logger.debug("get users list");
        return userRepo.findAll();
    }

    @RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)
    public @ResponseBody User getUser(@PathVariable Long userId) {
        logger.debug("get user");
        return userRepo.findOne(userId);
    }

    @RequestMapping(value = "/user/createaccount", method = RequestMethod.POST)
    public ResponseEntity<?> saveUser(HttpServletRequest request, @RequestBody User user) {
        ResponseEntity<User> responseEntity;
        try {
            logger.info("save user " + user.toString());
            user.setLogin(user.getEmail());
            if(CONFIRM_REGISTRATION) {
                user.setEnabled(false);
            } else {
                user.setEnabled(true);
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User existingUser = userRepo.findByLogin(user.getLogin());
            if (existingUser == null) {
                logger.info("user does not exist");
                userRepo.save(user);

                //add authority
                Authority authority = authRepo.findByName("user");
                if(authority != null) {
                    user.getAuthorities().add(authority);
                    userRepo.save(user);
                }

                if(CONFIRM_REGISTRATION) {
                    //send email verification
                    try {
                        String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() +
                                request.getContextPath();
                        eventPublisher.publishEvent(new OnRegistrationCompleteEvent
                                (user, request.getLocale(), appUrl));

                        String message = messages.getMessage("message.regSucc", null, request.getLocale());
                        Response response =  new Response(HttpStatus.OK.value(), message);
                        return new ResponseEntity<Response>(response, HttpStatus.OK);

                    } catch (Exception me) {
                        logger.error("registration event error " + me.getMessage(), me);
                    }

                } else {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getLogin());
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, user.getPassword(), userDetails.getAuthorities());

                    //login new user
                    try {
                        authMgr.authenticate(auth);
                        if (auth.isAuthenticated()) {
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            user.setPassword(null);
                            responseEntity = new ResponseEntity<User>(user, HttpStatus.OK);
                            return responseEntity;
                        }
                    } catch (Exception e) {
                        logger.error("Problem authenticating user" + user.getLogin(), e);
                    }
                }

            } else {
                user.setPassword(null);
                String message = messages.getMessage("message.regError", null, request.getLocale());
                return SecurityUtils.getErrorResponse("authError", message, HttpStatus.CONFLICT);
            }
        } catch(Exception e) {
            logger.error("saveUser " + e.getMessage(), e);
        }

        user.setPassword(null);
        String message = messages.getMessage("message.serverError", null, request.getLocale());
        return SecurityUtils.getErrorResponse("authError", message, HttpStatus.INTERNAL_SERVER_ERROR);

    }


    @RequestMapping(value = "/user/registrationConfirm", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> confirmRegistration(WebRequest request, @RequestParam("token") String token) {
        Locale locale = request.getLocale();

        VerificationToken verificationToken = userService.getVerificationToken(token);
        if (verificationToken == null) {
            String message = messages.getMessage("auth.message.invalidToken", null, locale);
            Response response =  new Response(HttpStatus.BAD_REQUEST.value(), message);
            return new ResponseEntity<Response>(response, HttpStatus.OK);
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            String message = messages.getMessage("auth.message.expired", null, locale);
            Response response =  new Response(HttpStatus.BAD_REQUEST.value(), message);
            return new ResponseEntity<Response>(response, HttpStatus.OK);
        }

        user.setEnabled(true);
        userService.saveRegisteredUser(user);

        String message = messages.getMessage("message.regConfirmation", null, locale);
        Response response =  new Response(HttpStatus.OK.value(), message);
        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/user/resetPassword", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> resetPassword(HttpServletRequest request, @RequestParam("email") String userEmail) {

        User user = userRepo.findByEmail(userEmail);
        if (user == null) {
            user = userRepo.findByLogin(userEmail);
            if(user == null) {
                Response response = new Response(HttpStatus.BAD_REQUEST.value(),
                        messages.getMessage("auth.message.invalidUser", null, request.getLocale()));
                return new ResponseEntity<Response>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(user, token);
        String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() +
                        request.getContextPath();
        SimpleMailMessage email =  constructResetTokenEmail(appUrl, request.getLocale(), token, user);

        try {
            mailSender.send(email);
        } catch (Exception e) {
            logger.error("Error sending mail: " + e.getMessage());
        }

        Response response =  new Response(HttpStatus.OK.value(),
                messages.getMessage("message.resetPasswordEmail", null, request.getLocale()));
        return new ResponseEntity<Response>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/user/processResetPasswordUrl", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> processResetPasswordUrl (
            HttpServletRequest request, @RequestParam("id") long id, @RequestParam("token") String token) {

        PasswordResetToken passToken = userService.getPasswordResetToken(token);
        User user = passToken.getUser();
        if (passToken == null || user.getId() != id) {
            String message = messages.getMessage("auth.message.invalidToken", null, request.getLocale());

            Response response =  new Response(HttpStatus.BAD_REQUEST.value(), message);
            return new ResponseEntity<Response>(response, HttpStatus.OK);
        }

        Calendar cal = Calendar.getInstance();
        if ((passToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            String message = messages.getMessage("auth.message.expired", null, request.getLocale());

            Response response =  new Response(HttpStatus.BAD_REQUEST.value(), message);
            return new ResponseEntity<Response>(response, HttpStatus.OK);
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, userDetailsService.loadUserByUsername(user.getLogin()).getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return new ResponseEntity<Response>(new Response(HttpStatus.OK.value()), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/changePassword", method = RequestMethod.POST)
    //@PreAuthorize("hasRole('READ_PRIVILEGE')")
    @ResponseBody
    public ResponseEntity<Response> changePassword(Locale locale, @RequestParam("password") String password) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.changeUserPassword(user, password);
        return new ResponseEntity<Response>(new Response(messages.getMessage("message.resetPasswordSuc", null, locale)), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/session", method = RequestMethod.GET)
    public @ResponseBody  String getUserSession()  {
        return new String("{}");
    }

    private SimpleMailMessage constructResetTokenEmail(
            String contextPath, Locale locale, String token, User user) {
        String url = contextPath + "/#/resetpassword/" + user.getId() + "/" + token;
        String message = messages.getMessage("message.resetPassword", null, locale);
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject("Reset Password");
        email.setText(message + " " + url);
        email.setFrom(messages.getMessage("support.email", null, locale));
        logger.info(email.getText());
        return email;
    }
}

 
