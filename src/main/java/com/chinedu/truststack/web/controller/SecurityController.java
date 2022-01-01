package com.chinedu.truststack.web.controller;

import com.chinedu.truststack.persist.repo.TokenRepo;
import com.chinedu.truststack.persist.entity.Token;
import com.chinedu.truststack.persist.entity.User;
import com.chinedu.truststack.persist.repo.UserRepo;
import com.chinedu.truststack.security.SecurityUtils;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Api(description = "Users management API")
public class SecurityController {


    @Autowired
    private UserRepo userRepo;


    @Autowired
    private TokenRepo tokenRepo;

    @RequestMapping(value = "/security/account", method = RequestMethod.GET)
    public @ResponseBody
    User getUserAccount()  {
        User user = userRepo.findByLogin(SecurityUtils.getCurrentLogin());
        if(user != null) {
            user.setPassword(null);
            return user;
        } else {
            return new User();
        }

    }

    @PreAuthorize("hasAuthority('admin')")
    @RequestMapping(value = "/security/tokens", method = RequestMethod.GET)
    public @ResponseBody
    List<Token> getTokens () {
        List<Token> tokens = tokenRepo.findAll();
        for(Token t:tokens) {
            t.setSeries(null);
            t.setValue(null);
        }
        return tokens;
    }



}
