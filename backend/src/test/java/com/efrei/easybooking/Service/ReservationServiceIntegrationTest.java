package com.efrei.easybooking.Service;

import com.efrei.easybooking.Entity.Reservation;
import com.efrei.easybooking.Entity.Salle;
import com.efrei.easybooking.Entity.Utilisateur;
import com.efrei.easybooking.Repository.ReservationRepository;
import com.efrei.easybooking.Repository.SalleRepository;
import com.efrei.easybooking.Repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ReservationService reservationService;

    private Salle salle;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        salle = new Salle();
        salle.setNom("Salle Test");
        salle.setLocation("Étage Test");
        salle = salleRepository.save(salle);

        utilisateur = new Utilisateur();
        utilisateur.setEmail("test@test.com");
        utilisateur.setPassword("password");
        utilisateur = utilisateurRepository.save(utilisateur);
    }

    @Test
    @Transactional
    void CreerReservationRetourneReservationQuandDonneesValides() {
        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 12, 0);

        Reservation result = reservationService.creerReservation(
                salle.getId(),
                utilisateur.getId(),
                dateDebut,
                dateFin
        );

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(salle.getId(), result.getSalle().getId());
        assertEquals(utilisateur.getId(), result.getUtilisateur().getId());
        assertEquals(dateDebut, result.getDateDebut());
        assertEquals(dateFin, result.getDateFin());

        Reservation savedReservation = reservationRepository.findById(result.getId()).orElse(null);
        assertNotNull(savedReservation);
        assertEquals(salle.getId(), savedReservation.getSalle().getId());
    }

    @Test
    @Transactional
    void CreerReservationLanceExceptionQuandSalleNonTrouvee() {
        Long salleIdInexistant = 999L;
        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 12, 0);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.creerReservation(
                        salleIdInexistant,
                        utilisateur.getId(),
                        dateDebut,
                        dateFin
                ));

        assertEquals("Salle non trouvée", exception.getMessage());
    }

    @Test
    @Transactional
    void CreerReservationLanceExceptionQuandUtilisateurNonTrouve() {
        Long userIdInexistant = 999L;
        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 12, 0);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.creerReservation(
                        salle.getId(),
                        userIdInexistant,
                        dateDebut,
                        dateFin
                ));

        assertEquals("Utilisateur non trouvé", exception.getMessage());
    }

    @Test
    @Transactional
    void CreerReservationLanceExceptionQuandSalleDejaReservee() {
        LocalDateTime dateDebut1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin1 = LocalDateTime.of(2026, 1, 15, 12, 0);

        Reservation reservationExistante = new Reservation();
        reservationExistante.setSalle(salle);
        reservationExistante.setUtilisateur(utilisateur);
        reservationExistante.setDateDebut(dateDebut1);
        reservationExistante.setDateFin(dateFin1);
        reservationRepository.save(reservationExistante);

        LocalDateTime dateDebut2 = LocalDateTime.of(2026, 1, 15, 11, 0);
        LocalDateTime dateFin2 = LocalDateTime.of(2026, 1, 15, 13, 0);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.creerReservation(
                        salle.getId(),
                        utilisateur.getId(),
                        dateDebut2,
                        dateFin2
                ));

        assertEquals("La salle est déjà réservée sur ce créneau", exception.getMessage());
    }

    @Test
    @Transactional
    void CreerReservationLanceExceptionQuandSalleDejaReserveeChevauchementDebut() {
        LocalDateTime dateDebut1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin1 = LocalDateTime.of(2026, 1, 15, 12, 0);

        Reservation reservationExistante = new Reservation();
        reservationExistante.setSalle(salle);
        reservationExistante.setUtilisateur(utilisateur);
        reservationExistante.setDateDebut(dateDebut1);
        reservationExistante.setDateFin(dateFin1);
        reservationRepository.save(reservationExistante);

        LocalDateTime dateDebut2 = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime dateFin2 = LocalDateTime.of(2026, 1, 15, 11, 30);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.creerReservation(
                        salle.getId(),
                        utilisateur.getId(),
                        dateDebut2,
                        dateFin2
                ));

        assertEquals("La salle est déjà réservée sur ce créneau", exception.getMessage());
    }

    @Test
    @Transactional
    void CreerReservationLanceExceptionQuandSalleDejaReserveeChevauchementFin() {
        LocalDateTime dateDebut1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin1 = LocalDateTime.of(2026, 1, 15, 12, 0);

        Reservation reservationExistante = new Reservation();
        reservationExistante.setSalle(salle);
        reservationExistante.setUtilisateur(utilisateur);
        reservationExistante.setDateDebut(dateDebut1);
        reservationExistante.setDateFin(dateFin1);
        reservationRepository.save(reservationExistante);

        LocalDateTime dateDebut2 = LocalDateTime.of(2026, 1, 15, 11, 0);
        LocalDateTime dateFin2 = LocalDateTime.of(2026, 1, 15, 13, 0);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.creerReservation(
                        salle.getId(),
                        utilisateur.getId(),
                        dateDebut2,
                        dateFin2
                ));

        assertEquals("La salle est déjà réservée sur ce créneau", exception.getMessage());
    }

    @Test
    @Transactional
    void CreerReservationReussitQuandReservationAutreSalle() {
        Salle autreSalle = new Salle();
        autreSalle.setNom("Salle B");
        autreSalle.setLocation("Étage 2");
        autreSalle = salleRepository.save(autreSalle);

        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 12, 0);

        Reservation reservationAutreSalle = new Reservation();
        reservationAutreSalle.setSalle(autreSalle);
        reservationAutreSalle.setUtilisateur(utilisateur);
        reservationAutreSalle.setDateDebut(dateDebut);
        reservationAutreSalle.setDateFin(dateFin);
        reservationRepository.save(reservationAutreSalle);

        Reservation result = reservationService.creerReservation(
                salle.getId(),
                utilisateur.getId(),
                dateDebut,
                dateFin
        );

        assertNotNull(result);
        assertEquals(salle.getId(), result.getSalle().getId());
    }

    @Test
    @Transactional
    void CreerReservationReussitQuandReservationMemeSalleMaisCreneauDifferent() {
        LocalDateTime dateDebut1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin1 = LocalDateTime.of(2026, 1, 15, 12, 0);

        Reservation reservationExistante = new Reservation();
        reservationExistante.setSalle(salle);
        reservationExistante.setUtilisateur(utilisateur);
        reservationExistante.setDateDebut(dateDebut1);
        reservationExistante.setDateFin(dateFin1);
        reservationRepository.save(reservationExistante);

        LocalDateTime dateDebut2 = LocalDateTime.of(2026, 1, 15, 14, 0);
        LocalDateTime dateFin2 = LocalDateTime.of(2026, 1, 15, 16, 0);

        Reservation result = reservationService.creerReservation(
                salle.getId(),
                utilisateur.getId(),
                dateDebut2,
                dateFin2
        );

        assertNotNull(result);
        assertEquals(salle.getId(), result.getSalle().getId());
        assertEquals(dateDebut2, result.getDateDebut());
        assertEquals(dateFin2, result.getDateFin());
    }

    @Test
    @Transactional
    void CreerReservationLanceExceptionQuandDateDebutApresDateFin() {
        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 14, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 10, 0);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.creerReservation(
                        salle.getId(),
                        utilisateur.getId(),
                        dateDebut,
                        dateFin
                ));

        assertEquals("La date de début doit être avant la date de fin", exception.getMessage());
    }

    @Test
    @Transactional
    void GetReservationsByUserRetourneListeReservationsQuandUtilisateurExiste() {
        LocalDateTime dateDebut1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin1 = LocalDateTime.of(2026, 1, 15, 12, 0);

        Reservation reservation1 = new Reservation();
        reservation1.setSalle(salle);
        reservation1.setUtilisateur(utilisateur);
        reservation1.setDateDebut(dateDebut1);
        reservation1.setDateFin(dateFin1);
        reservationRepository.save(reservation1);

        LocalDateTime dateDebut2 = LocalDateTime.of(2026, 1, 16, 10, 0);
        LocalDateTime dateFin2 = LocalDateTime.of(2026, 1, 16, 12, 0);

        Reservation reservation2 = new Reservation();
        reservation2.setSalle(salle);
        reservation2.setUtilisateur(utilisateur);
        reservation2.setDateDebut(dateDebut2);
        reservation2.setDateFin(dateFin2);
        reservationRepository.save(reservation2);

        List<Reservation> result = reservationService.getReservationsByUser(utilisateur.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getDateDebut().equals(dateDebut1)));
        assertTrue(result.stream().anyMatch(r -> r.getDateDebut().equals(dateDebut2)));
    }

    @Test
    @Transactional
    void GetReservationsByUserRetourneListeVideQuandAucuneReservation() {
        List<Reservation> result = reservationService.getReservationsByUser(utilisateur.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void GetReservationsByUserRetourneSeulementReservationsUtilisateur() {
        Utilisateur autreUtilisateur = new Utilisateur();
        autreUtilisateur.setEmail("autre@test.com");
        autreUtilisateur.setPassword("password");
        autreUtilisateur = utilisateurRepository.save(autreUtilisateur);

        LocalDateTime dateDebut1 = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin1 = LocalDateTime.of(2026, 1, 15, 12, 0);

        Reservation reservationUtilisateur = new Reservation();
        reservationUtilisateur.setSalle(salle);
        reservationUtilisateur.setUtilisateur(utilisateur);
        reservationUtilisateur.setDateDebut(dateDebut1);
        reservationUtilisateur.setDateFin(dateFin1);
        reservationRepository.save(reservationUtilisateur);

        Reservation reservationAutreUtilisateur = new Reservation();
        reservationAutreUtilisateur.setSalle(salle);
        reservationAutreUtilisateur.setUtilisateur(autreUtilisateur);
        reservationAutreUtilisateur.setDateDebut(dateDebut1);
        reservationAutreUtilisateur.setDateFin(dateFin1);
        reservationRepository.save(reservationAutreUtilisateur);

        List<Reservation> result = reservationService.getReservationsByUser(utilisateur.getId());

        assertEquals(1, result.size());
        assertEquals(utilisateur.getId(), result.get(0).getUtilisateur().getId());
    }

    @Test
    @Transactional
    void GetReservationsByUserLanceExceptionQuandUtilisateurNonTrouve() {
        Long userIdInexistant = 999L;

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.getReservationsByUser(userIdInexistant));

        assertEquals("Utilisateur non trouvé", exception.getMessage());
    }
}
