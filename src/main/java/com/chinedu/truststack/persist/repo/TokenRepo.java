package com.chinedu.truststack.persist.repo;

import com.chinedu.truststack.persist.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepo extends JpaRepository<Token, String> {
}
