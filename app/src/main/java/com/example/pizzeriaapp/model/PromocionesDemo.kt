package com.example.pizzeriaapp.model

import java.time.LocalDate

object PromocionesDemo {

    fun crearPromocion(
        fechaReferencia: LocalDate = LocalDate.now()
    ): Promocion {
        return Promocion(
            nombre = "Promoción de demostración",
            fechaInicio = fechaReferencia.minusDays(7),
            fechaFin = fechaReferencia.plusDays(7)
        )
    }

    val promocionActual = crearPromocion()
}