package com.monse.coinoptimize.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cuentas")
data class Cuenta(
    @PrimaryKey val id: String,
    val nombre: String,
    val saldoActual: Double,
    val tipo: TipoCuenta
)

enum class TipoCuenta {
    EFECTIVO,
    BANCO,
    AHORRO
}
