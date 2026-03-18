package com.mooc.formulaone;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
/**
 * Verifie que le contexte Spring Boot demarre correctement avec la configuration de test.
 */
class FormulaoneApplicationTests {

    /**
     * Valide le chargement complet du contexte applicatif.
     */
    @Test
    void contextLoads() {
    }

}
