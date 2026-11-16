package com.example.pizzeriaapp.model

class Repartidor(
    nombre: String,
    telefono: String,
    email: String,
    val zona: String,
    val prioridadCercania: Int
) : Usuario(nombre, telefono, email) {

    var disponible: Boolean = true
        private set

    // ==MÉTODOS==

    // asignar un pedido al repartidor si está disponible
    fun asignarPedido(pedido: Pedido): Boolean {
        if (!disponible) {
            return false
        }

        val asignacionCorrecta = pedido.asignarRepartidor(this)

        if (asignacionCorrecta) {
            disponible = false
        }

        return asignacionCorrecta
    }

    // iniciar el despacho: el pedido cambia de LISTO a EN_CAMINO.
    fun iniciarEntrega(pedido: Pedido): Boolean {
        if (pedido.repartidor !== this) {
            return false
        }

        return pedido.cambiarEstado(EstadoPedido.EN_CAMINO)
    }

    // completar la entrega: el pedido cambia a ENTREGADO y se libera al repartidor.
    fun completarEntrega(pedido: Pedido): Boolean {
        if (
            pedido.repartidor !== this ||
            pedido.estado != EstadoPedido.EN_CAMINO
        ) {
            return false
        }

        val entregaCompletada =
            pedido.cambiarEstado(EstadoPedido.ENTREGADO)

        if (entregaCompletada) {
            disponible = true
        }

        return entregaCompletada
    }
}