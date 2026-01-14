package com.efrei.easybooking.DTO;

import java.time.LocalDateTime;

public record ReservationDTO(
        Long salleId,
        LocalDateTime dateDebut,
        LocalDateTime dateFin
)
{
}
