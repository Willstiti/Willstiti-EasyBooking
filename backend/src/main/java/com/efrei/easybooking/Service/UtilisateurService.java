package com.efrei.easybooking.Service;

import com.efrei.easybooking.Entity.Utilisateur;
import com.efrei.easybooking.Repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public Utilisateur getUser(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public List<Utilisateur> getAllUsers() {
        return utilisateurRepository.findAll();
    }
}
