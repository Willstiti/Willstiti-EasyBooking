package com.efrei.easybooking.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Salle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String location;

    @OneToMany(mappedBy = "salle")
    private List<Reservation> reservations ;
}
