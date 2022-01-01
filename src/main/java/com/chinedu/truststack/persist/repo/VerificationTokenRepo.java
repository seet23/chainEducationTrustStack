package com.chinedu.truststack.persist.repo;

import com.chinedu.truststack.persist.entity.User;
import com.chinedu.truststack.persist.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepo extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);

    VerificationToken findByUser(User user);
}