package com.efrei.easybooking.Service;

import com.efrei.easybooking.Entity.Reservation;
import com.efrei.easybooking.Entity.Salle;
import com.efrei.easybooking.Entity.Utilisateur;
import com.efrei.easybooking.Repository.ReservationRepository;
import com.efrei.easybooking.Repository.SalleRepository;
import com.efrei.easybooking.Repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SalleRepository salleRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void CreerReservationRetourneReservationQuandDonneesValides() {
        Long salleId = 1L;
        Long userId = 1L;
        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 12, 0);

        Salle salle = new Salle();
        salle.setId(salleId);
        salle.setNom("Salle A");

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(userId);
        utilisateur.setEmail("test@test.com");

        when(salleRepository.findById(eq(salleId)))
                .thenReturn(Optional.of(salle));
        when(utilisateurRepository.findById(eq(userId)))
                .thenReturn(Optional.of(utilisateur));
        when(reservationRepository.findAll())
                .thenReturn(new ArrayList<>());

        Reservation reservationSauvegardee = new Reservation();
        reservationSauvegardee.setSalle(salle);
        reservationSauvegardee.setUtilisateur(utilisateur);
        reservationSauvegardee.setDateDebut(dateDebut);
        reservationSauvegardee.setDateFin(dateFin);

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(reservationSauvegardee);

        Reservation result = reservationService.creerReservation(salleId, userId, dateDebut, dateFin);

        assertEquals(salle, result.getSalle());
        assertEquals(utilisateur, result.getUtilisateur());
        assertEquals(dateDebut, result.getDateDebut());
        assertEquals(dateFin, result.getDateFin());
        verify(salleRepository).findById(salleId);
        verify(utilisateurRepository).findById(userId);
        verify(reservationRepository).findAll();
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void CreerReservationLanceExceptionQuandSalleNonTrouvee() {
        Long salleId = 999L;
        Long userId = 1L;
        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 12, 0);

        when(salleRepository.findById(eq(salleId)))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> reservationService.creerReservation(salleId, userId, dateDebut, dateFin),
                "Salle non trouvée");
        verify(salleRepository).findById(salleId);
    }

    @Test
    void CreerReservationLanceExceptionQuandUtilisateurNonTrouve() {
        Long salleId = 1L;
        Long userId = 999L;
        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 12, 0);

        Salle salle = new Salle();
        salle.setId(salleId);
        salle.setNom("Salle A");

        when(salleRepository.findById(eq(salleId)))
                .thenReturn(Optional.of(salle));
        when(utilisateurRepository.findById(eq(userId)))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> reservationService.creerReservation(salleId, userId, dateDebut, dateFin),
                "Utilisateur non trouvé");
        verify(salleRepository).findById(salleId);
        verify(utilisateurRepository).findById(userId);
    }

    @Test
    void CreerReservationLanceExceptionQuandSalleDejaReservee() {
        Long salleId = 1L;
        Long userId = 1L;
        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 12, 0);

        Salle salle = new Salle();
        salle.setId(salleId);
        salle.setNom("Salle A");

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(userId);
        utilisateur.setEmail("test@test.com");

        Reservation reservationExistante = new Reservation();
        reservationExistante.setSalle(salle);
        reservationExistante.setDateDebut(LocalDateTime.of(2026, 1, 15, 11, 0));
        reservationExistante.setDateFin(LocalDateTime.of(2026, 1, 15, 13, 0));

        when(salleRepository.findById(eq(salleId)))
                .thenReturn(Optional.of(salle));
        when(utilisateurRepository.findById(eq(userId)))
                .thenReturn(Optional.of(utilisateur));
        when(reservationRepository.findAll())
                .thenReturn(List.of(reservationExistante));

        assertThrows(RuntimeException.class,
                () -> reservationService.creerReservation(salleId, userId, dateDebut, dateFin),
                "La salle est déjà réservée sur ce créneau");
        verify(salleRepository).findById(salleId);
        verify(utilisateurRepository).findById(userId);
        verify(reservationRepository).findAll();
    }

    @Test
    void CreerReservationLanceExceptionQuandSalleDejaReserveeChevauchementDebut() {
        Long salleId = 1L;
        Long userId = 1L;
        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 11, 30);

        Salle salle = new Salle();
        salle.setId(salleId);
        salle.setNom("Salle A");

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(userId);
        utilisateur.setEmail("test@test.com");

        Reservation reservationExistante = new Reservation();
        reservationExistante.setSalle(salle);
        reservationExistante.setDateDebut(LocalDateTime.of(2026, 1, 15, 10, 0));
        reservationExistante.setDateFin(LocalDateTime.of(2026, 1, 15, 12, 0));

        when(salleRepository.findById(eq(salleId)))
                .thenReturn(Optional.of(salle));
        when(utilisateurRepository.findById(eq(userId)))
                .thenReturn(Optional.of(utilisateur));
        when(reservationRepository.findAll())
                .thenReturn(List.of(reservationExistante));

        assertThrows(RuntimeException.class,
                () -> reservationService.creerReservation(salleId, userId, dateDebut, dateFin),
                "La salle est déjà réservée sur ce créneau");
    }

    @Test
    void CreerReservationLanceExceptionQuandSalleDejaReserveeChevauchementFin() {
        Long salleId = 1L;
        Long userId = 1L;
        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 11, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 13, 0);

        Salle salle = new Salle();
        salle.setId(salleId);
        salle.setNom("Salle A");

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(userId);
        utilisateur.setEmail("test@test.com");

        Reservation reservationExistante = new Reservation();
        reservationExistante.setSalle(salle);
        reservationExistante.setDateDebut(LocalDateTime.of(2026, 1, 15, 10, 0));
        reservationExistante.setDateFin(LocalDateTime.of(2026, 1, 15, 12, 0));

        when(salleRepository.findById(eq(salleId)))
                .thenReturn(Optional.of(salle));
        when(utilisateurRepository.findById(eq(userId)))
                .thenReturn(Optional.of(utilisateur));
        when(reservationRepository.findAll())
                .thenReturn(List.of(reservationExistante));

        assertThrows(RuntimeException.class,
                () -> reservationService.creerReservation(salleId, userId, dateDebut, dateFin),
                "La salle est déjà réservée sur ce créneau");
    }

    @Test
    void CreerReservationLanceExceptionQuandDateDebutApresDateFin() {
        Long salleId = 1L;
        Long userId = 1L;
        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 14, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 10, 0);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.creerReservation(salleId, userId, dateDebut, dateFin));

        assertEquals("La date de début doit être avant la date de fin", exception.getMessage());
    }

    @Test
    void CreerReservationReussitQuandReservationAutreSalle() {
        Long salleId = 1L;
        Long autreSalleId = 2L;
        Long userId = 1L;
        LocalDateTime dateDebut = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 1, 15, 12, 0);

        Salle salle = new Salle();
        salle.setId(salleId);
        salle.setNom("Salle A");

        Salle autreSalle = new Salle();
        autreSalle.setId(autreSalleId);
        autreSalle.setNom("Salle B");

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(userId);
        utilisateur.setEmail("test@test.com");

        Reservation reservationAutreSalle = new Reservation();
        reservationAutreSalle.setSalle(autreSalle);
        reservationAutreSalle.setDateDebut(LocalDateTime.of(2026, 1, 15, 10, 0));
        reservationAutreSalle.setDateFin(LocalDateTime.of(2026, 1, 15, 12, 0));

        when(salleRepository.findById(eq(salleId)))
                .thenReturn(Optional.of(salle));
        when(utilisateurRepository.findById(eq(userId)))
                .thenReturn(Optional.of(utilisateur));
        when(reservationRepository.findAll())
                .thenReturn(List.of(reservationAutreSalle));

        Reservation reservationSauvegardee = new Reservation();
        reservationSauvegardee.setSalle(salle);
        reservationSauvegardee.setUtilisateur(utilisateur);
        reservationSauvegardee.setDateDebut(dateDebut);
        reservationSauvegardee.setDateFin(dateFin);

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(reservationSauvegardee);

        Reservation result = reservationService.creerReservation(salleId, userId, dateDebut, dateFin);

        assertEquals(salle, result.getSalle());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void GetReservationsByUserRetourneListeReservationsQuandUtilisateurExiste() {
        Long userId = 1L;

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(userId);
        utilisateur.setEmail("test@test.com");

        Reservation reservation1 = new Reservation();
        reservation1.setId(1L);
        reservation1.setUtilisateur(utilisateur);

        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setUtilisateur(utilisateur);

        List<Reservation> reservations = List.of(reservation1, reservation2);

        when(utilisateurRepository.findById(eq(userId)))
                .thenReturn(Optional.of(utilisateur));
        when(reservationRepository.findByUtilisateur(eq(utilisateur)))
                .thenReturn(reservations);

        List<Reservation> result = reservationService.getReservationsByUser(userId);

        assertEquals(2, result.size());
        assertEquals(reservations, result);
        verify(utilisateurRepository).findById(userId);
        verify(reservationRepository).findByUtilisateur(utilisateur);
    }

    @Test
    void GetReservationsByUserRetourneListeVideQuandAucuneReservation() {
        Long userId = 1L;

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(userId);
        utilisateur.setEmail("test@test.com");

        when(utilisateurRepository.findById(eq(userId)))
                .thenReturn(Optional.of(utilisateur));
        when(reservationRepository.findByUtilisateur(eq(utilisateur)))
                .thenReturn(new ArrayList<>());

        List<Reservation> result = reservationService.getReservationsByUser(userId);

        assertEquals(0, result.size());
        verify(utilisateurRepository).findById(userId);
        verify(reservationRepository).findByUtilisateur(utilisateur);
    }

    @Test
    void GetReservationsByUserLanceExceptionQuandUtilisateurNonTrouve() {
        Long userId = 999L;

        when(utilisateurRepository.findById(eq(userId)))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> reservationService.getReservationsByUser(userId),
                "Utilisateur non trouvé");
        verify(utilisateurRepository).findById(userId);
    }
}
