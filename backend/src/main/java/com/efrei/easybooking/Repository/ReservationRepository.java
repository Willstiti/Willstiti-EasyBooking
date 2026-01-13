package com.efrei.easybooking.Repository;

import com.efrei.easybooking.Entity.Reservation;
import com.efrei.easybooking.Entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUtilisateur(Utilisateur utilisateur);
}
