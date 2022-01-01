package com.chinedu.truststack.service;

import com.chinedu.truststack.persist.entity.PasswordResetToken;
import com.chinedu.truststack.persist.entity.User;
import com.chinedu.truststack.persist.entity.VerificationToken;
import com.chinedu.truststack.persist.repo.PasswordResetTokenRepo;
import com.chinedu.truststack.persist.repo.UserRepo;
import com.chinedu.truststack.persist.repo.VerificationTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    public static final String TOKEN_INVALID = "invalidToken";
    public static final String TOKEN_EXPIRED = "expired";
    public static final String TOKEN_VALID = "valid";

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private VerificationTokenRepo verificationTokenRepo;

    @Autowired
    private PasswordResetTokenRepo passwordTokenRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private boolean emailExist(String email) {
        User user = userRepo.findByEmail(email);
        if (user != null) {
            return true;
        }
        return false;
    }

    @Override
    public User findUserByEmail(String email) {
        User user = userRepo.findByEmail(email);
        return user;
    }

    @Override
    public User getUser(String verificationToken) {
        User user = verificationTokenRepo.findByToken(verificationToken).getUser();
        return user;
    }

    @Override
    public VerificationToken getVerificationToken(String VerificationToken) {
        return verificationTokenRepo.findByToken(VerificationToken);
    }

    @Override
    public void saveRegisteredUser(User user) {
        userRepo.save(user);
    }

    @Override
    public void createVerificationToken(User user, String token) {
        VerificationToken myToken = new VerificationToken(token, user);
        verificationTokenRepo.save(myToken);
    }

    @Override
    public void createPasswordResetTokenForUser(final User user, final String token) {
        final PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordTokenRepo.save(myToken);
    }

    @Override
    public PasswordResetToken getPasswordResetToken(final String token) {
        return passwordTokenRepo.findByToken(token);
    }

    @Override
    public User getUserByPasswordResetToken(final String token) {
        return passwordTokenRepo.findByToken(token).getUser();
    }

    @Override
    public User getUserByID(final long id) {
        return userRepo.findOne(id);
    }

    @Override
    public void changeUserPassword(final User user, final String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepo.save(user);
    }

    @Override
    public boolean checkIfValidOldPassword(final User user, final String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    @Override
    public String validateVerificationToken(String token) {
        final VerificationToken verificationToken = verificationTokenRepo.findByToken(token);
        if (verificationToken == null) {
            return TOKEN_INVALID;
        }

        final User user = verificationToken.getUser();
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            verificationTokenRepo.delete(verificationToken);
            return TOKEN_EXPIRED;
        }

        user.setEnabled(true);
        // tokenRepository.delete(verificationToken);
        userRepo.save(user);
        return TOKEN_VALID;
    }

}
