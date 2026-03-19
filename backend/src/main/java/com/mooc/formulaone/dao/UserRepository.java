package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Repository Spring Data pour les utilisateurs.
 */
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
}
