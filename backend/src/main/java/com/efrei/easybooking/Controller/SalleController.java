package com.efrei.easybooking.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efrei.easybooking.Entity.Salle;
import com.efrei.easybooking.Service.SalleService;

@RestController
public class SalleController {

    private final SalleService salleService;

    public SalleController(SalleService salleService) {
        this.salleService = salleService;
    }

    @GetMapping("/salles")
    public List<Salle> getAllSalles() {
        return salleService.findAll();
    }
}
