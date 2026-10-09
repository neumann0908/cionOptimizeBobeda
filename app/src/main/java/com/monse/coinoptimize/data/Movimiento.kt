package com.monse.coinoptimize.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movimientos")
data class Movimiento(
    @PrimaryKey val id: String,
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
