package com.efrei.easybooking.Controller;

import com.efrei.easybooking.DTO.ReservationDTO;
import com.efrei.easybooking.Entity.Reservation;
import com.efrei.easybooking.Service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                reservationDTO.getSalleId(), 
                userId, 
                reservationDTO.getDateDebut(), 
                reservationDTO.getDateFin()
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
}
