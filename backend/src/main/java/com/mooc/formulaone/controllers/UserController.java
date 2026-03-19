package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.UserCreateRequest;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Utilisateurs",
        description = "Gestion des comptes utilisateurs PMT : consultation, création et suppression."
)
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
    @Operation(
            summary = "Lister les utilisateurs",
            description = "Retourne l'ensemble des comptes connus par l'application. Cet endpoint alimente notamment les sélecteurs d'assignation."
    )
    @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée avec succès.")
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
    @Operation(
            summary = "Récupérer un utilisateur",
            description = "Retourne le détail d'un utilisateur à partir de son identifiant technique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé."),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable.")
    })
    public User findById(
            @Parameter(description = "Identifiant technique de l'utilisateur.", example = "1")
            @PathVariable Long id
    ) {
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
    @Operation(
            summary = "Créer un utilisateur",
            description = "Crée un nouveau compte utilisateur. Le mot de passe brut est transmis au backend puis encodé dans la couche service."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Informations de création d'un compte utilisateur.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Création standard",
                    value = """
                            {
                              "username": "Alice Martin",
                              "email": "alice@pmt.fr",
                              "password": "MotDePasse123!"
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur créé."),
            @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà utilisé.")
    })
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
    @Operation(
            summary = "Supprimer un utilisateur",
            description = "Supprime définitivement un compte existant à partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur supprimé."),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable.")
    })
    public void delete(
            @Parameter(description = "Identifiant de l'utilisateur à supprimer.", example = "3")
            @PathVariable Long id
    ) {
        User user = userService.findById(id);
        userService.delete(user);
    }
}
