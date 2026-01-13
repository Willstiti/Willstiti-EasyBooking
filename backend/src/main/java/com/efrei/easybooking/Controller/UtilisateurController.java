package com.efrei.easybooking.Controller;

import com.efrei.easybooking.Entity.Utilisateur;
import com.efrei.easybooking.Service.UtilisateurService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping ("/user")
    public Utilisateur GetUser(Long id) {
        return utilisateurService.getUser(id);
    }

    @GetMapping("/allusers")
    public List<Utilisateur> GetAllUsers() {
        return utilisateurService.getAllUsers();
    }

}
