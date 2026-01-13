package com.efrei.easybooking.Service;

import com.efrei.easybooking.Entity.Reservation;
import com.efrei.easybooking.Repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {
    private ReservationRepository reservationRepository;

    public Reservation findById(Long id){
        return reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pas de Réservation avec cette ID")
                );
    }

}
