package com.efrei.easybooking.Entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    @JsonIgnoreProperties("reservations") // on ignore la liste des réservations côté JSON
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "salle_id", nullable = false)
    @JsonIgnoreProperties("reservations")
    private Salle salle;
}
