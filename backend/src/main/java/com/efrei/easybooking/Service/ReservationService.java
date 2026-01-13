package com.efrei.easybooking.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.efrei.easybooking.Entity.Reservation;
import com.efrei.easybooking.Entity.Salle;
import com.efrei.easybooking.Entity.Utilisateur;
import com.efrei.easybooking.Repository.ReservationRepository;
import com.efrei.easybooking.Repository.SalleRepository;
import com.efrei.easybooking.Repository.UtilisateurRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final SalleRepository salleRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ReservationService(ReservationRepository reservationRepository, 
                             SalleRepository salleRepository,
                             UtilisateurRepository utilisateurRepository) {
        this.reservationRepository = reservationRepository;
        this.salleRepository = salleRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public Reservation creerReservation(Long salleId, Long userId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
        
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        List<Reservation> reservationsDeLaSalle = reservationRepository.findAll().stream()
                .filter(r -> r.getSalle().getId().equals(salleId))
                .filter(r -> dateDebut.isBefore(r.getDateFin()) && dateFin.isAfter(r.getDateDebut()))
                .toList();
        
        if (!reservationsDeLaSalle.isEmpty()) {
            throw new RuntimeException("La salle est déjà réservée sur ce créneau");
        }

        Reservation reservation = new Reservation();
        reservation.setSalle(salle);
        reservation.setUtilisateur(utilisateur);
        reservation.setDateDebut(dateDebut);
        reservation.setDateFin(dateFin);

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getReservationsByUser(Long userId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return reservationRepository.findByUtilisateur(utilisateur);
    }
}
