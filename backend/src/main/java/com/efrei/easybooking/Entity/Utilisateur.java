package com.efrei.easybooking.Entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;

    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore // on n'expose pas non plus les réservations pour éviter Utilisateur -> Reservation -> Utilisateur ...
    private List<Reservation> reservations;
}
