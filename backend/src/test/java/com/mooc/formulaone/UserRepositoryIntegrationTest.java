package com.mooc.formulaone;

import com.mooc.formulaone.dao.UserRepository;
import com.mooc.formulaone.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
/**
 * Couvre le comportement JPA du repository utilisateur avec une base H2 en memoire.
 */
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    /**
     * Verifie qu'un utilisateur peut etre persiste puis relu par son identifiant.
     * Controle egalement l'initialisation automatique des champs d'audit.
     */
    @Test
    void shouldPersistAndLoadUser() {
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash("hash-123");

        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("alice@example.com");
    }
}
