package com.mooc.formulaone.services.impl;

import com.mooc.formulaone.dao.UserRepository;
import com.mooc.formulaone.exceptions.EntityDontExistException;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
/**
 * Implementation de {@link UserService} chargee des operations CRUD
 * sur les utilisateurs ainsi que du hashage du mot de passe a la creation.
 */
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Charge tous les utilisateurs depuis le repository et retourne
     * une liste materialisee pour simplifier son exploitation.
     *
     * @return la liste des utilisateurs existants
     */
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    /**
     * Retourne un utilisateur par identifiant ou leve une exception 404 metier
     * lorsqu'aucune ligne correspondante n'est trouvee.
     *
     * @param id identifiant recherche
     * @return l'utilisateur persiste
     */
    @Override
    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        }
        throw new EntityDontExistException();
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(EntityDontExistException::new);
    }

    @Override
    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Email ou mot de passe invalide.");
        }
        return user;
    }

    /**
     * Encode le mot de passe brut fourni par la couche web avant
     * de persister l'utilisateur puis retourne l'identifiant genere.
     *
     * @param user utilisateur a enregistrer
     * @return identifiant cree en base
     */
    @Override
    public Long create(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user).getId();
    }

    /**
     * Supprime l'utilisateur transmis.
     *
     * @param user utilisateur a supprimer
     */
    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }
}
