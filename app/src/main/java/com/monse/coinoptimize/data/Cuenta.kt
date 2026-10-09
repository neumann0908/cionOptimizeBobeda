package com.monse.coinoptimize.data

data class Cuenta(
    val id: String,
    val nombre: String,
    val saldoActual: Double,
    val tipo: TipoCuenta
)

enum class TipoCuenta {
    EFECTIVO,
    BANCO,
    AHORRO
}
