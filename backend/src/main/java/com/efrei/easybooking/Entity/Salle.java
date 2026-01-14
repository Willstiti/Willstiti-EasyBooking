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
public class Salle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String location;

    @OneToMany(mappedBy = "salle")
    @JsonIgnore // on n'expose pas les réservations pour éviter la boucle Salle -> Reservation -> Salle ...
    private List<Reservation> reservations;
}
