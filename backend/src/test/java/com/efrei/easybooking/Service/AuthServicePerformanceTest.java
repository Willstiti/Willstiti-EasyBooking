package com.efrei.easybooking.Service;

import com.efrei.easybooking.Entity.Utilisateur;
import com.efrei.easybooking.Repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServicePerformanceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @BeforeEach
    void setUp() {
        utilisateurRepository.deleteAll();
    }

    @Test
    void RegisterDoitEtreRapidePourUnUtilisateur() {
        long startTime = System.currentTimeMillis();
        
        authService.register("test@test.com", "password123");
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration < 1000, "L'inscription doit prendre moins de 1 seconde, mais a pris " + duration + "ms");
        assertTrue(utilisateurRepository.findByEmail("test@test.com").isPresent());
    }

    @Test
    void LoginDoitEtreRapidePourUnUtilisateur() {
        authService.register("test@test.com", "password123");
        
        long startTime = System.currentTimeMillis();
        
        Utilisateur result = authService.login("test@test.com", "password123");
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration < 500, "La connexion doit prendre moins de 500ms, mais a pris " + duration + "ms");
        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void RegisterDoitGererPlusieursUtilisateursRapidement() {
        int nombreUtilisateurs = 100;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < nombreUtilisateurs; i++) {
            authService.register("user" + i + "@test.com", "password123");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        long tempsMoyenParUtilisateur = duration / nombreUtilisateurs;
        
        assertEquals(nombreUtilisateurs, utilisateurRepository.count());
        assertTrue(tempsMoyenParUtilisateur < 50, 
                "L'inscription moyenne doit prendre moins de 50ms par utilisateur, mais a pris " + tempsMoyenParUtilisateur + "ms");
    }

    @Test
    void LoginDoitGererPlusieursConnexionsRapidement() {
        int nombreUtilisateurs = 100;
        
        for (int i = 0; i < nombreUtilisateurs; i++) {
            authService.register("user" + i + "@test.com", "password123");
        }
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < nombreUtilisateurs; i++) {
            authService.login("user" + i + "@test.com", "password123");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        long tempsMoyenParConnexion = duration / nombreUtilisateurs;
        
        assertTrue(tempsMoyenParConnexion < 30, 
                "La connexion moyenne doit prendre moins de 30ms par utilisateur, mais a pris " + tempsMoyenParConnexion + "ms");
    }

    @Test
    void RegisterAvecEmailExistantDoitEtreRapide() {
        authService.register("test@test.com", "password123");
        
        long startTime = System.currentTimeMillis();
        
        try {
            authService.register("test@test.com", "password123");
            fail("Devrait lancer une exception");
        } catch (RuntimeException e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            assertTrue(duration < 100, 
                    "La détection d'email existant doit être rapide (< 100ms), mais a pris " + duration + "ms");
            assertEquals("Utilisateur déjà existant", e.getMessage());
        }
    }

    @Test
    void LoginAvecIdentifiantsIncorrectsDoitEtreRapide() {
        authService.register("test@test.com", "password123");
        
        long startTime = System.currentTimeMillis();
        
        try {
            authService.login("test@test.com", "wrongPassword");
            fail("Devrait lancer une exception");
        } catch (RuntimeException e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            assertTrue(duration < 100, 
                    "La détection d'identifiants incorrects doit être rapide (< 100ms), mais a pris " + duration + "ms");
            assertEquals("Identifiants incorrects", e.getMessage());
        }
    }

    @Test
    void RegisterEtLoginSequenceDoitEtreEfficace() {
        int nombreIterations = 50;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < nombreIterations; i++) {
            String email = "user" + i + "@test.com";
            authService.register(email, "password123");
            authService.login(email, "password123");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        long tempsMoyenParIteration = duration / nombreIterations;
        
        assertEquals(nombreIterations, utilisateurRepository.count());
        assertTrue(tempsMoyenParIteration < 100, 
                "L'inscription + connexion moyenne doit prendre moins de 100ms, mais a pris " + tempsMoyenParIteration + "ms");
    }
}
