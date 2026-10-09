package com.monse.coinoptimize.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metas_ahorro")
data class MetaAhorro(
    @PrimaryKey val id: String,
    val titulo: String,
    val montoObjetivo: Double,
    val montoActual: Double = 0.0,
    val fechaObjetivo: String = ""
)
