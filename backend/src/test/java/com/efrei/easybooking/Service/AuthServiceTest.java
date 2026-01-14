package com.efrei.easybooking.Service;

import com.efrei.easybooking.Entity.Utilisateur;
import com.efrei.easybooking.Repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void LoginRetourneUtilisateurQuandIdentifiantsCorrects() {
        String email = "test@test.com";
        String password = "password";

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setPassword(password);

        when(utilisateurRepository.findByEmail(eq(email)))
                .thenReturn(Optional.of(utilisateur));

        Utilisateur result = authService.login(email, password);

        assertEquals(utilisateur, result);
        verify(utilisateurRepository).findByEmail(email);
    }

    @Test
    void loginLanceExceptionQuandEmailInconnu() {
        String email = "test@test.comm";
        String password = "password";

        when(utilisateurRepository.findByEmail(eq(email)))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.login(email, password),
                "Identifiants incorrects");
        verify(utilisateurRepository).findByEmail(email);
    }

    @Test
    void loginLanceExceptionQuandMotDePasseIncorrect() {
        String email = "test@test.com";
        String password = "password";

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setPassword("autrePassword");

        when(utilisateurRepository.findByEmail(eq(email)))
                .thenReturn(Optional.of(utilisateur));

        assertThrows(RuntimeException.class,
                () -> authService.login(email, password),
                "Identifiants incorrects");
        verify(utilisateurRepository).findByEmail(email);
    }

    @Test
    void RegisterSauvegardeEtRetourneUtilisateurQuandEmailInexistant() {
        String email = "test@test.com";
        String password = "password";

        when(utilisateurRepository.findByEmail(eq(email)))
                .thenReturn(Optional.empty());

        Utilisateur utilisateurEnregistre = new Utilisateur();
        utilisateurEnregistre.setEmail(email);
        utilisateurEnregistre.setPassword(password);

        when(utilisateurRepository.save(any(Utilisateur.class)))
                .thenReturn(utilisateurEnregistre);

        Utilisateur result = authService.register(email, password);

        assertEquals(utilisateurEnregistre, result);
        assertEquals(email, result.getEmail());
        assertEquals(password, result.getPassword());
        verify(utilisateurRepository).findByEmail(email);
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    void registerLanceExceptionQuandEmailDejaExistant() {
        String email = "test@test.com";
        String password = "password";

        Utilisateur utilisateurExistant = new Utilisateur();
        utilisateurExistant.setEmail(email);
        utilisateurExistant.setPassword(password);

        when(utilisateurRepository.findByEmail(eq(email)))
                .thenReturn(Optional.of(utilisateurExistant));

        assertThrows(RuntimeException.class,
                () -> authService.register(email, password),
                "Utilisateur déjà existant");
        verify(utilisateurRepository).findByEmail(email);
    }
}