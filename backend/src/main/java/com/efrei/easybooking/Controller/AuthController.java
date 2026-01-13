package com.efrei.easybooking.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.efrei.easybooking.DTO.LoginDTO;
import com.efrei.easybooking.DTO.RegisterDTO;
import com.efrei.easybooking.Entity.Utilisateur;
import com.efrei.easybooking.Service.AuthService;

import jakarta.servlet.http.HttpSession;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO.getEmail(), registerDTO.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body("Compte créé avec succès");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        Utilisateur user = authService.login(loginDTO.getEmail(), loginDTO.getPassword());
        
        session.setAttribute("userId", user.getId());
        session.setAttribute("email", user.getEmail());
        
        return ResponseEntity.ok("Connexion réussie");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Déconnexion réussie");
    }
}
