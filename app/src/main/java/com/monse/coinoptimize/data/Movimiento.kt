package com.monse.coinoptimize.data

data class Movimiento(
    val id: String,
    val concepto: String,
    val monto: Double,
    val tipo: TipoMovimiento,
    val fecha: String,
    val cuentaId: String,
    val esFijo: Boolean = false
)

enum class TipoMovimiento {
    INGRESO,
    GASTO
}
