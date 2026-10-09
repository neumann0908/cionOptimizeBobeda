package com.monse.coinoptimize.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // CUENTAS
    @Query("SELECT * FROM cuentas")
    fun obtenerTodasLasCuentas(): Flow<List<Cuenta>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCuenta(cuenta: Cuenta)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCuentas(cuentas: List<Cuenta>)

    @Update
    suspend fun actualizarCuenta(cuenta: Cuenta)

    // MOVIMIENTOS
    @Query("SELECT * FROM movimientos ORDER BY id DESC")
    fun obtenerTodosLosMovimientos(): Flow<List<Movimiento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMovimiento(movimiento: Movimiento)

    @Delete
    suspend fun eliminarMovimiento(movimiento: Movimiento)

    // METAS DE AHORRO
    @Query("SELECT * FROM metas_ahorro")
    fun obtenerTodasLasMetas(): Flow<List<MetaAhorro>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMeta(meta: MetaAhorro)

    @Delete
    suspend fun eliminarMeta(meta: MetaAhorro)
}
