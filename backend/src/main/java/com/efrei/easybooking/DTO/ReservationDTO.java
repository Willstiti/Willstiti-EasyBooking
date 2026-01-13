package com.efrei.easybooking.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReservationDTO {
    private Long salleId;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
}
