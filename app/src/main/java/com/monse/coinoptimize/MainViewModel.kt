package com.monse.coinoptimize

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.monse.coinoptimize.data.Cuenta
import com.monse.coinoptimize.data.Movimiento
import com.monse.coinoptimize.data.TipoCuenta
import com.monse.coinoptimize.data.TipoMovimiento
import java.util.UUID

class MainViewModel : ViewModel() {

    // Lista de Cuentas en memoria
    val cuentas = mutableStateListOf<Cuenta>(
        Cuenta(id = "1", nombre = "EFECTIVO", saldoActual = 150.0, tipo = TipoCuenta.EFECTIVO),
        Cuenta(id = "2", nombre = "BANCO PRINCIPAL", saldoActual = 520.0, tipo = TipoCuenta.BANCO),
        Cuenta(id = "3", nombre = "CAJA FUERTE", saldoActual = 1200.0, tipo = TipoCuenta.AHORRO)
    )

    // Lista de Movimientos con datos de prueba
    val movimientos = mutableStateListOf<Movimiento>(
        Movimiento(
            id = "m1",
            concepto = "Pago Freelance Kotlin",
            monto = 350.0,
            tipo = TipoMovimiento.INGRESO,
            fecha = "05 OCT 2026",
            cuentaId = "2",
            esFijo = false
        ),
        Movimiento(
            id = "m2",
            concepto = "Supermercado y Despensa",
            monto = 85.0,
            tipo = TipoMovimiento.GASTO,
            fecha = "07 OCT 2026",
            cuentaId = "1",
            esFijo = false
        )
    )

    // --- CÁLCULOS DINÁMICOS ---

    // Balance Total = Suma de saldos de todas las cuentas + Ingresos - Gastos
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

    fun agregarMovimiento(concepto: String, monto: Double, tipo: TipoMovimiento, cuentaId: String) {
        val nuevoMovimiento = Movimiento(
            id = UUID.randomUUID().toString(),
            concepto = concepto.ifBlank { "Sin descripción" },
            monto = monto,
            tipo = tipo,
            fecha = "OCT 2026",
            cuentaId = cuentaId,
            esFijo = false
        )
        movimientos.add(0, nuevoMovimiento) // Agregar al inicio del historial
    }
}
