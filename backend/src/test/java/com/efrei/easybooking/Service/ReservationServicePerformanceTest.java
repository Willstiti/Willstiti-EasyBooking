package com.efrei.easybooking.Service;

import com.efrei.easybooking.Entity.Reservation;
import com.efrei.easybooking.Entity.Salle;
import com.efrei.easybooking.Entity.Utilisateur;
import com.efrei.easybooking.Repository.ReservationRepository;
import com.efrei.easybooking.Repository.SalleRepository;
import com.efrei.easybooking.Repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de performance pour ReservationService")
class ReservationServicePerformanceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private Salle salle;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        utilisateurRepository.deleteAll();
        salleRepository.deleteAll();

        salle = new Salle();
        salle.setNom("Salle de performance");
        salle.setLocation("Bâtiment A");
        salle = salleRepository.save(salle);

        utilisateur = new Utilisateur();
        utilisateur.setEmail("perf@test.com");
        utilisateur.setPassword("password123");
        utilisateur = utilisateurRepository.save(utilisateur);
    }

    @Test
    @DisplayName("Créer une réservation doit être rapide")
    void creerReservationDoitEtreRapide() {
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(1);
        LocalDateTime dateFin = dateDebut.plusHours(2);

        long startTime = System.currentTimeMillis();

        Reservation result = reservationService.creerReservation(
                salle.getId(),
                utilisateur.getId(),
                dateDebut,
                dateFin
        );

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 500, "La création de réservation doit prendre moins de 500ms, mais a pris " + duration + "ms");
        assertNotNull(result);
        assertNotNull(result.getId());
    }

    @Test
    @DisplayName("Créer plusieurs réservations pour différentes salles doit être rapide")
    void creerPlusieursReservationsPourDifferentesSalleDoitEtreRapide() {
        int nombreReservations = 50;
        List<Salle> salles = new ArrayList<>();

        // Créer 50 salles
        for (int i = 0; i < nombreReservations; i++) {
            Salle s = new Salle();
            s.setNom("Salle " + i);
            s.setLocation("Étage " + (i % 5));
            salles.add(salleRepository.save(s));
        }

        long startTime = System.currentTimeMillis();

        // Créer 50 réservations
        for (int i = 0; i < nombreReservations; i++) {
            LocalDateTime dateDebut = LocalDateTime.now().plusDays(i + 1);
            LocalDateTime dateFin = dateDebut.plusHours(2);
            reservationService.creerReservation(
                    salles.get(i).getId(),
                    utilisateur.getId(),
                    dateDebut,
                    dateFin
            );
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        long tempsMoyenParReservation = duration / nombreReservations;

        assertEquals(nombreReservations, reservationRepository.count());
        assertTrue(tempsMoyenParReservation < 100,
                "Chaque réservation doit prendre moins de 100ms en moyenne, mais a pris " + tempsMoyenParReservation + "ms");
    }

    @Test
    @DisplayName("Récupérer les réservations d'un utilisateur doit être rapide")
    void obtenirReservationsUtilisateurDoitEtreRapide() {
        // Créer 30 réservations
        int nombreReservations = 30;
        for (int i = 0; i < nombreReservations; i++) {
            Salle s = new Salle();
            s.setNom("Salle " + i);
            s.setLocation("Étage " + (i % 5));
            s = salleRepository.save(s);

            LocalDateTime dateDebut = LocalDateTime.now().plusDays(i + 1);
            LocalDateTime dateFin = dateDebut.plusHours(2);
            reservationService.creerReservation(s.getId(), utilisateur.getId(), dateDebut, dateFin);
        }

        long startTime = System.currentTimeMillis();

        List<Reservation> reservations = reservationService.getReservationsByUser(utilisateur.getId());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(nombreReservations, reservations.size());
        assertTrue(duration < 200, "La récupération des réservations doit prendre moins de 200ms, mais a pris " + duration + "ms");
    }

    @Test
    @DisplayName("Suppression d'une réservation doit être rapide")
    void supprimerReservationDoitEtreRapide() {
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(1);
        LocalDateTime dateFin = dateDebut.plusHours(2);

        Reservation reservation = reservationService.creerReservation(
                salle.getId(),
                utilisateur.getId(),
                dateDebut,
                dateFin
        );

        long startTime = System.currentTimeMillis();

        reservationService.supprimerReservationPourUtilisateur(reservation.getId(), utilisateur.getId());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 300, "La suppression de réservation doit prendre moins de 300ms, mais a pris " + duration + "ms");
        assertTrue(reservationRepository.findById(reservation.getId()).isEmpty());
    }

    @Test
    @DisplayName("Vérifier la performance avec vérification de conflits de réservation")
    void verifierConflitReservationDoitEtrePerformant() {
        int nombreReservationsConflictuelles = 100;
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(1);
        LocalDateTime dateFin = dateDebut.plusHours(2);

        // Créer 100 réservations sur la même salle à des moments différents
        for (int i = 0; i < nombreReservationsConflictuelles; i++) {
            Utilisateur u = new Utilisateur();
            u.setEmail("user" + i + "@test.com");
            u.setPassword("password");
            u = utilisateurRepository.save(u);

            LocalDateTime debut = LocalDateTime.now().plusDays(i + 10);
            LocalDateTime fin = debut.plusHours(2);

            reservationService.creerReservation(salle.getId(), u.getId(), debut, fin);
        }

        long startTime = System.currentTimeMillis();

        // Tenter de créer une réservation qui ne conflicte pas
        LocalDateTime debutNonConflictuel = LocalDateTime.now().plusDays(200);
        LocalDateTime finNonConflictuel = debutNonConflictuel.plusHours(2);

        Reservation result = reservationService.creerReservation(
                salle.getId(),
                utilisateur.getId(),
                debutNonConflictuel,
                finNonConflictuel
        );

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertNotNull(result);
        assertTrue(duration < 1000,
                "La vérification de conflits avec 100 réservations doit prendre moins de 1 seconde, mais a pris " + duration + "ms");
    }

    @Test
    @DisplayName("Suppression en masse de réservations doit être performante")
    void suppressionEnMasseDoitEtrePerformante() {
        int nombreReservations = 20;
        List<Reservation> reservations = new ArrayList<>();

        // Créer 20 réservations
        for (int i = 0; i < nombreReservations; i++) {
            Salle s = new Salle();
            s.setNom("Salle " + i);
            s.setLocation("Étage " + (i % 5));
            s = salleRepository.save(s);

            LocalDateTime dateDebut = LocalDateTime.now().plusDays(i + 1);
            LocalDateTime dateFin = dateDebut.plusHours(2);
            Reservation r = reservationService.creerReservation(s.getId(), utilisateur.getId(), dateDebut, dateFin);
            reservations.add(r);
        }

        long startTime = System.currentTimeMillis();

        // Supprimer toutes les réservations
        for (Reservation r : reservations) {
            reservationService.supprimerReservationPourUtilisateur(r.getId(), utilisateur.getId());
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        long tempsMoyenParSuppression = duration / nombreReservations;

        assertEquals(0, reservationRepository.count());
        assertTrue(tempsMoyenParSuppression < 100,
                "Chaque suppression doit prendre moins de 100ms en moyenne, mais a pris " + tempsMoyenParSuppression + "ms");
    }

    @Test
    @DisplayName("Récupération des réservations par salle et date doit être rapide")
    void obtenirReservationsBySalleAndDateDoitEtreRapide() {
        int nombreReservations = 50;

        // Créer 50 réservations sur la même salle à des jours différents pour éviter les conflits
        for (int i = 0; i < nombreReservations; i++) {
            Utilisateur u = new Utilisateur();
            u.setEmail("user" + i + "@test.com");
            u.setPassword("password");
            u = utilisateurRepository.save(u);

            LocalDateTime dateDebut = LocalDateTime.now().plusDays(i + 1).withHour(9);
            LocalDateTime dateFin = dateDebut.plusHours(2);

            reservationService.creerReservation(salle.getId(), u.getId(), dateDebut, dateFin);
        }

        long startTime = System.currentTimeMillis();

        // Récupérer les réservations du premier jour créé
        List<Reservation> reservations = reservationService.getReservationsBySalleAndDate(
                salle.getId(),
                LocalDateTime.now().plusDays(1).toLocalDate()
        );

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(reservations.size() > 0);
        assertTrue(duration < 300,
                "La récupération des réservations par salle et date doit prendre moins de 300ms, mais a pris " + duration + "ms");
    }

    @Test
    @DisplayName("Créer une réservation avec vérification de conflits doit rester performant")
    void creerReservationAvecVerificationConflitsDoitResterPerformant() {
        // Créer 10 réservations au préalable
        for (int i = 0; i < 10; i++) {
            Utilisateur u = new Utilisateur();
            u.setEmail("user" + i + "@test.com");
            u.setPassword("password");
            u = utilisateurRepository.save(u);

            LocalDateTime dateDebut = LocalDateTime.now().plusDays(i + 1);
            LocalDateTime dateFin = dateDebut.plusHours(2);

            reservationService.creerReservation(salle.getId(), u.getId(), dateDebut, dateFin);
        }

        long startTime = System.currentTimeMillis();

        // Créer une réservation à un moment sans conflit
        LocalDateTime dateDebut = LocalDateTime.now().plusDays(100);
        LocalDateTime dateFin = dateDebut.plusHours(2);

        Reservation result = reservationService.creerReservation(salle.getId(), utilisateur.getId(), dateDebut, dateFin);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertNotNull(result);
        assertTrue(duration < 500,
                "Créer une réservation avec vérification de 10 réservations existantes doit prendre moins de 500ms, mais a pris " + duration + "ms");
    }
}
