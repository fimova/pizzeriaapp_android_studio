package com.example.pizzeriaapp.service

import com.example.pizzeriaapp.model.Cliente
import com.example.pizzeriaapp.utils.validarEmail
import com.example.pizzeriaapp.utils.validarTelefono

// registra un cliente con datos recibidos, sin usar la consola
fun registrarClienteSimulado(
    nombreEntrada: String,
    emailEntrada: String,
    telefonoEntrada: String,
    direccionEntrada: String,
    clientesRegistrados: MutableSet<Cliente>
): Cliente? {
    val nombre = nombreEntrada.trim().ifBlank { "Sin nombre" }
    val email = validarEmail(emailEntrada) ?: return null

    // revisar si el cliente ya existe antes de usar el teléfono y la dirección
    val clienteExistente = clientesRegistrados.firstOrNull {
        it.nombre == nombre && it.email == email
    }
    if (clienteExistente != null) return clienteExistente

    val telefono = validarTelefono(telefonoEntrada) ?: return null
    val direccion = direccionEntrada.trim().ifBlank { "Sin dirección" }

    val nuevoCliente = Cliente(nombre, telefono, email, direccion)
    clientesRegistrados.add(nuevoCliente)
    return nuevoCliente
}