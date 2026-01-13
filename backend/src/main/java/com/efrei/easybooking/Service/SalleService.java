package com.efrei.easybooking.Service;

import com.efrei.easybooking.Entity.Salle;
import com.efrei.easybooking.Repository.SalleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalleService {
    private final SalleRepository salleRepository;

    public SalleService(SalleRepository salleRepository) {
        this.salleRepository = salleRepository;
    }

    public List<Salle> findAll(){
        return salleRepository.findAll();
    }
}
