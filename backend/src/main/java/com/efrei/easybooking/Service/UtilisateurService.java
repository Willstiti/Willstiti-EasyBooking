package com.efrei.easybooking.Service;

import com.efrei.easybooking.Entity.Utilisateur;
import com.efrei.easybooking.Repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurService {

    private UtilisateurRepository utilisateurRepository;

    public Utilisateur GetUser(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public List<Utilisateur> GetAllUsers() {
        return utilisateurRepository.findAll();
    }
}
