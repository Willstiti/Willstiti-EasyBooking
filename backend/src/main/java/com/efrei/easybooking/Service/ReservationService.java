package com.efrei.easybooking.Service;

import java.time.LocalDate;
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
        if (dateDebut.isAfter(dateFin) || dateDebut.isEqual(dateFin)) {
            throw new RuntimeException("La date de début doit être avant la date de fin");
        }

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

    public void supprimerReservationPourUtilisateur(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        if (reservation.getUtilisateur() == null || reservation.getUtilisateur().getId() == null
                || !reservation.getUtilisateur().getId().equals(userId)) {
            throw new RuntimeException("Vous ne pouvez supprimer que vos propres réservations");
        }

        reservationRepository.delete(reservation);
    }

    public List<Reservation> getReservationsBySalleAndDate(Long salleId, LocalDate date) {
        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée"));

        List<Reservation> reservations = reservationRepository.findBySalle(salle);

        return reservations.stream()
                .filter(r -> r.getDateDebut() != null && r.getDateFin() != null)
                .filter(r -> r.getDateDebut().toLocalDate().equals(date) ||
                             r.getDateFin().toLocalDate().equals(date))
                .toList();
    }
}
