package com.efrei.easybooking.Service;

import com.efrei.easybooking.Entity.Utilisateur;
import com.efrei.easybooking.Repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;

    public AuthService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public Utilisateur Login (String email, String password) {
        return utilisateurRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(password))
                .orElseThrow(() -> new RuntimeException("Identifiants incorrects"));
    }

    public Utilisateur register(String email, String password) {
        if (utilisateurRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Utilisateur déjà existant");
        }

        Utilisateur user = new Utilisateur();
        user.setEmail(email);
        user.setPassword(password);

        return utilisateurRepository.save(user);
    }
}

