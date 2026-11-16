package com.example.pizzeriaapp.service

import com.example.pizzeriaapp.model.CatalogoProductos
import com.example.pizzeriaapp.model.Cliente
import com.example.pizzeriaapp.model.EstadoPedido
import com.example.pizzeriaapp.model.ItemPedido
import com.example.pizzeriaapp.model.Pedido
import com.example.pizzeriaapp.model.PromocionesDemo
import com.example.pizzeriaapp.model.Repartidor
import com.example.pizzeriaapp.model.Producto
import com.example.pizzeriaapp.utils.leerCantidadPositiva
import com.example.pizzeriaapp.utils.validarZona
import com.example.pizzeriaapp.utils.TASA_DESCUENTO
import com.example.pizzeriaapp.utils.inspeccionarClase
import java.time.LocalDate

private fun seleccionarProductoSimulado(
    menu: List<Producto>,
    opcionEntrada: String
): Producto? {
    val opcion = opcionEntrada.trim().toIntOrNull() ?: return null

    return if (opcion in 1..menu.size) {
        menu[opcion - 1]
    } else {
        null
    }
}

fun ejecutarDemostracionPizzeria() {
    val inventario = mutableMapOf(
        "masa" to 50,
        "queso" to 50,
        "salsa" to 40,
        "pepperoni" to 30,
        "piña" to 20
    )

    val clientesRegistrados = mutableSetOf<Cliente>()

    val cliente = checkNotNull(
        registrarClienteSimulado(
            nombreEntrada = "Cliente de prueba",
            emailEntrada = "cliente@pizzeria.cl",
            telefonoEntrada = "912345678",
            direccionEntrada = "Av. Central 123",
            clientesRegistrados = clientesRegistrados
        )
    ) {
        "Los datos simulados del cliente no pasaron la validación."
    }

    val zona = checkNotNull(validarZona("Centro")) {
        "La zona simulada no es válida."
    }

    val menu = CatalogoProductos.productos

    // la opción 3 corresponde a Hawaiana en el catálogo actual
    val producto = checkNotNull(
        seleccionarProductoSimulado(menu, "3")
    ) {
        "La opción simulada no corresponde a un producto."
    }

    val cantidad = checkNotNull(leerCantidadPositiva("2")) {
        "La cantidad simulada no es válida."
    }

    val fechaPedido = LocalDate.now()
    val promocion = PromocionesDemo.crearPromocion(fechaPedido)

    val pedido = Pedido(
        id = 1001L,
        cliente = cliente,
        items = listOf(ItemPedido(producto, cantidad)),
        notas = "Pedido de demostración",
        fechaPedido = fechaPedido,
        promocion = promocion
    )

    // comprueba y actualiza el inventario una sola vez para este pedido
    val resultadoInventario =
        verificarYActualizarInventario(pedido, inventario)

    check(resultadoInventario.inventarioActualizado) {
        "No hay inventario suficiente para el pedido de demostración."
    }

    val total = pedido.calcularTotal()
    check(total.descuentoPorPromocion)
    check(!total.descuentoPorCantidad)

    check(
        productosPromocionablesDelPedido(pedido)
            .any { it.nombre == producto.nombre }
    )

    val repartidores = listOf(
        Repartidor(
            "Ana",
            "+56 (9) 3333-3333",
            "ana@pizza.com",
            zona,
            1
        )
    )

    check(pedido.cambiarEstado(EstadoPedido.LISTO))

    val repartidor = checkNotNull(
        buscarRepartidorDisponible(zona, repartidores)
    )

    check(repartidor.asignarPedido(pedido))
    check(repartidor.iniciarEntrega(pedido))
    check(repartidor.completarEntrega(pedido))
    check(pedido.estado == EstadoPedido.ENTREGADO)
    check(repartidor.disponible)

    // segundo pedido usado solo para probar la combinación
    // no se vuelve a descontar inventario ni a despachar
    val segundoPedido = Pedido(
        id = 1002L,
        cliente = cliente,
        items = listOf(
            ItemPedido(CatalogoProductos.hawaiana, 1),
            ItemPedido(CatalogoProductos.margarita, 2)
        ),
        notas = "Segundo pedido de demostración",
        fechaPedido = fechaPedido,
        promocion = promocion
    )

    val pedidosCompatibles = checkNotNull(
        buscarPedidosCompatibles(arrayOf(pedido, segundoPedido))
    ) {
        "No se encontraron pedidos compatibles."
    }

    val (primerPedidoCompatible, segundoPedidoCompatible) = pedidosCompatibles
    val pedidoCombinado = primerPedidoCompatible + segundoPedidoCompatible

    // extrae cliente, ítems con sus cantidades y total
    val (clienteCombinado, itemsCombinados, totalCombinado) = pedidoCombinado

    val hawaianaCombinada = itemsCombinados.single {
        it.producto == CatalogoProductos.hawaiana
    }
    val margaritaCombinada = itemsCombinados.single {
        it.producto == CatalogoProductos.margarita
    }

    check(hawaianaCombinada.cantidad == cantidad + 1)
    check(margaritaCombinada.cantidad == 2)

    // también comprueba la desestructuración del cliente
    val (nombre, email, telefono) = clienteCombinado
    check(nombre == cliente.nombre)
    check(email == cliente.email)
    check(telefono == cliente.telefono)

    // se cumplen cantidad y promoción, pero se aplica un solo descuento
    check(totalCombinado.descuentoPorCantidad)
    check(totalCombinado.descuentoPorPromocion)
    check(
        kotlin.math.abs(
            totalCombinado.descuento -
                    totalCombinado.subtotal * TASA_DESCUENTO
        ) < 0.001
    )

    verificarIgualdadYReflexion()

    verificarReportePedidos(arrayOf(pedido))
}

private fun buscarPedidosCompatibles(
    pedidosDia: Array<Pedido>
): Pair<Pedido, Pedido>? {
    for (i in pedidosDia.indices) {
        for (j in i + 1 until pedidosDia.size) {
            val primero = pedidosDia[i]
            val segundo = pedidosDia[j]

            val sonCompatibles =
                primero.cliente == segundo.cliente &&
                        primero.fechaPedido == segundo.fechaPedido &&
                        primero.promocion == segundo.promocion

            if (sonCompatibles) {
                return primero to segundo
            }
        }
    }

    return null
}

private fun verificarReportePedidos(pedidosDia: Array<Pedido>) {
    var pedidosRecorridos = 0

    for (pedido in pedidosDia) {
        val total = pedido.calcularTotal()
        val productos = pedido.items.joinToString(", ") {
            "${it.producto.nombre} x${it.cantidad}"
        }

        check(total.totalFinal > 0.0)
        check(productos.isNotBlank())
        pedidosRecorridos++
    }

    val listosOEnCamino = pedidosDia.filter {
        it.estado == EstadoPedido.LISTO ||
                it.estado == EstadoPedido.EN_CAMINO
    }
    val entregados = pedidosDia.filter {
        it.estado == EstadoPedido.ENTREGADO
    }
    val pendientes = pedidosDia.filter {
        it.estado == EstadoPedido.EN_PREPARACION
    }

    check(pedidosRecorridos == pedidosDia.size)
    check(listosOEnCamino.isEmpty())
    check(entregados.size == 1)
    check(pendientes.isEmpty())
}

private fun verificarIgualdadYReflexion() {
    // mismo nombre y categoria, aunque tengan precios distintos
    val producto1 = Producto("Margarita", 5000.0, emptyMap())
    val producto2 = Producto("Margarita", 9999.0, emptyMap())
    check(producto1 == producto2)

    // mismo nombre y correo: el set debe rechazar al cliente duplicado
    val cliente1 = Cliente("Juan", "912345678", "juan@mail.com", "Calle 1")
    val cliente2 = Cliente("Juan", "998877665", "juan@mail.com", "Calle 2")
    val clientes = mutableSetOf(cliente1)
    check(!clientes.add(cliente2))

    // el catalogo ya contiene Hawaiana y debe rechazar el duplicado
    val hawaianaDuplicada = Producto("Hawaiana", 1111.0, emptyMap())
    check(!CatalogoProductos.registrarProducto(hawaianaDuplicada))

    // ejecuta la inspeccion por reflection de estas tres clases
    val inspeccionPedido = inspeccionarClase(Pedido::class)
    val inspeccionProducto = inspeccionarClase(Producto::class)
    val inspeccionCliente = inspeccionarClase(Cliente::class)

    check(inspeccionPedido.firstOrNull() == "Clase: Pedido")
    check(inspeccionProducto.firstOrNull() == "Clase: Producto")
    check(inspeccionCliente.firstOrNull() == "Clase: Cliente")
}