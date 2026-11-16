package com.example.pizzeriaapp.service

import com.example.pizzeriaapp.model.CatalogoProductos
import com.example.pizzeriaapp.model.Pedido
import com.example.pizzeriaapp.model.Producto

// revisa con reflection y devuelve los productos que tengan la anotación
fun obtenerProductosPromocionables(): List<Producto> {
    return CatalogoProductos.productos.filter { it.esPromocionable() }
}

// revisar cuáles productos promocionables aparecen en un pedido
fun productosPromocionablesDelPedido(pedido: Pedido): List<Producto> {
    return pedido.productos
        .filter { it.esPromocionable() }
        .distinct()
}