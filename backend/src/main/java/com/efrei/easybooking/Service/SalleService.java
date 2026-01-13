package com.efrei.easybooking.Service;

import com.efrei.easybooking.Entity.Salle;
import com.efrei.easybooking.Repository.SalleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalleService {
    private SalleRepository salleRepository;

    public Salle findByID(Long id){
        return salleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pas de Salle avec cette ID")
                );
    }

    public List<Salle> findAll(){
        return salleRepository.findAll();
    }
}
