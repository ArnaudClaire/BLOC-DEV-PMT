package com.mooc.formulaone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * Point d'entrée principal de l'application Spring Boot PMT.
 */
public class FormulaoneApplication {

	/**
	 * Démarre le contexte Spring et l'ensemble des composants backend.
	 *
	 * @param args arguments de ligne de commande éventuels
	 */
	public static void main(String[] args) {
		SpringApplication.run(FormulaoneApplication.class, args);
	}

}
