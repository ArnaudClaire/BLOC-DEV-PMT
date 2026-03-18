package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.UserCreateRequest;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
/**
 * Expose les endpoints REST de gestion des utilisateurs.
 */
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retourne la liste complete des utilisateurs.
     *
     * @return les utilisateurs existants
     */
    @GetMapping("/users")
    @ResponseStatus(code = HttpStatus.OK)
    public List<User> findAll() {
        return userService.findAll();
    }

    /**
     * Retourne un utilisateur a partir de son identifiant.
     *
     * @param id identifiant de l'utilisateur
     * @return l'utilisateur correspondant
     */
    @GetMapping("/users/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public User findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    /**
     * Cree un utilisateur depuis la charge utile HTTP.
     *
     * @param request utilisateur a enregistrer
     * @return identifiant genere
     */
    @PostMapping("/users")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long create(@Valid @RequestBody UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(request.password());
        return userService.create(user);
    }

    /**
     * Supprime un utilisateur s'il existe.
     *
     * @param id identifiant de l'utilisateur a supprimer
     */
    @DeleteMapping("/users/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public void delete(@PathVariable Long id) {
        User user = userService.findById(id);
        userService.delete(user);
    }
}
