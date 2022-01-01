package com.chinedu.truststack.persist.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chinedu.truststack.persist.entity.Authority;

public interface AuthorityRepo extends JpaRepository<Authority, Long> {
    Authority findByName(String name);
}