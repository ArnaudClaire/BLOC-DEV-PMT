package com.mooc.formulaone.services;

import com.mooc.formulaone.models.User;

import java.util.List;

public interface UserService {

    /**
     * Retourne l'ensemble des utilisateurs en base.
     *
     * @return la liste complete des utilisateurs
     */
    List<User> findAll();

    /**
     * Recherche un utilisateur par son identifiant.
     *
     * @param id identifiant technique de l'utilisateur
     * @return l'utilisateur trouve
     */
    User findById(Long id);

    /**
     * Recherche un utilisateur par son email.
     *
     * @param email adresse email unique de l'utilisateur
     * @return l'utilisateur trouve
     */
    User findByEmail(String email);

    /**
     * Authentifie un utilisateur a partir de son email et de son mot de passe.
     *
     * @param email email de connexion
     * @param password mot de passe brut
     * @return l'utilisateur authentifie
     */
    User authenticate(String email, String password);

    /**
     * Cree un nouvel utilisateur.
     *
     * @param user utilisateur a enregistrer
     * @return l'identifiant genere apres persistence
     */
    Long create(User user);

    /**
     * Supprime un utilisateur existant.
     *
     * @param user utilisateur a supprimer
     */
    void delete(User user);
}
