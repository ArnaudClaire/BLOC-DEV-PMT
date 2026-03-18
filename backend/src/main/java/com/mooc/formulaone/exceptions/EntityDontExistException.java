package com.mooc.formulaone.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND)
/**
 * Exception metier levee lorsqu'une entite demandee n'existe pas
 * ou n'est plus presente en base de donnees.
 */
public class EntityDontExistException extends RuntimeException{
}
