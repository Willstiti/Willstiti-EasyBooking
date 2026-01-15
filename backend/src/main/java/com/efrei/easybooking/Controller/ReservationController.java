package com.efrei.easybooking.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.efrei.easybooking.DTO.ReservationDTO;
import com.efrei.easybooking.Entity.Reservation;
import com.efrei.easybooking.Service.ReservationService;

import jakarta.servlet.http.HttpSession;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<String> creerReservation(@RequestBody ReservationDTO reservationDTO, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vous devez être connecté");
        }

        try {
            reservationService.creerReservation(
                reservationDTO.salleId(), 
                userId, 
                reservationDTO.dateDebut(), 
                reservationDTO.dateFin()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body("Réservation créée avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/reservations")
    public ResponseEntity<?> getMesReservations(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vous devez être connecté");
        }

        List<Reservation> reservations = reservationService.getReservationsByUser(userId);
        return ResponseEntity.ok(reservations);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<String> supprimerReservation(@PathVariable("id") Long reservationId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vous devez être connecté");
        }

        try {
            reservationService.supprimerReservationPourUtilisateur(reservationId, userId);
            return ResponseEntity.ok("Réservation supprimée avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
