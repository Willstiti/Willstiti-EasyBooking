package com.efrei.easybooking;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.efrei.easybooking.Entity.Salle;
import com.efrei.easybooking.Repository.SalleRepository;

@SpringBootApplication
public class EasyBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyBookingApplication.class, args);
    }

    /**
     * Initialise une seule salle en base au démarrage, si aucune salle n'existe.
     */
    @Bean
    public CommandLineRunner initSalle(SalleRepository salleRepository) {
        return args -> {
            if (salleRepository.count() == 0) {
                Salle salle = new Salle();
                salle.setNom("Salle 1");
                salle.setLocation("Étage 1");
                salleRepository.save(salle);
            }
        };
    }
}
