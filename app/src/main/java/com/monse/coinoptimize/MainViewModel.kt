package com.monse.coinoptimize

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monse.coinoptimize.data.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).appDao()

    val cuentas = mutableStateListOf<Cuenta>()
    val movimientos = mutableStateListOf<Movimiento>()

    init {
        // Escuchar Cuentas desde SQLite
        viewModelScope.launch {
            dao.obtenerTodasLasCuentas().collect { listaCuentas ->
                cuentas.clear()
                if (listaCuentas.isEmpty()) {
                    val cuentasIniciales = listOf(
                        Cuenta(id = "1", nombre = "EFECTIVO", saldoActual = 150.0, tipo = TipoCuenta.EFECTIVO),
                        Cuenta(id = "2", nombre = "BANCO PRINCIPAL", saldoActual = 520.0, tipo = TipoCuenta.BANCO),
                        Cuenta(id = "3", nombre = "CAJA FUERTE", saldoActual = 1200.0, tipo = TipoCuenta.AHORRO)
                    )
                    dao.insertarCuentas(cuentasIniciales)
                } else {
                    cuentas.addAll(listaCuentas)
                }
            }
        }

        // Escuchar Movimientos desde SQLite
        viewModelScope.launch {
            dao.obtenerTodosLosMovimientos().collect { listaMovimientos ->
                movimientos.clear()
                movimientos.addAll(listaMovimientos)
            }
        }
    }

    // --- CÁLCULOS DINÁMICOS ---
    val balanceNeto: Double
        get() {
            val saldoBaseCuentas = cuentas.sumOf { it.saldoActual }
            val totalIngresos = movimientos.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto }
            val totalGastos = movimientos.filter { it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto }
            return saldoBaseCuentas + totalIngresos - totalGastos
        }

    val totalIngresosMes: Double
        get() = movimientos.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto }

    val totalGastosMes: Double
        get() = movimientos.filter { it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto }

    val cantidadRegistros: Int
        get() = movimientos.size

    // --- ACCIONES ---
    fun agregarMovimiento(
        concepto: String, 
        monto: Double, 
        tipo: TipoMovimiento, 
        cuentaId: String,
        esFijo: Boolean = false
    ) {
        viewModelScope.launch {
            val nuevoMovimiento = Movimiento(
                id = UUID.randomUUID().toString(),
                concepto = concepto.ifBlank { "Sin descripción" },
                monto = monto,
                tipo = tipo,
                fecha = "OCT 2026",
                cuentaId = cuentaId,
                esFijo = esFijo
            )
            dao.insertarMovimiento(nuevoMovimiento)
        }
    }
}
