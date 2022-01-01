package com.chinedu.truststack.persist.repo;

import com.chinedu.truststack.persist.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface UserRepo extends JpaRepository<User, Long> {
    User findByLogin(String login);

    User findByEmail(String email);

    //@Query("SELECT p FROM Person p WHERE LOWER(p.lastName) = LOWER(:lastName)")
    //public List<User> find(@Param("lastName") String lastName);

}
