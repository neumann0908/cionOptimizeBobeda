package com.monse.coinoptimize.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// --- MODELOS DE DATOS BASE ---

enum class TipoMovimiento { 
    INGRESO, 
    GASTO 
}

data class Cuenta(
    val id: String, 
    val nombre: String, 
    var saldo: Double
)

data class Movimiento(
    val id: String, 
    val concepto: String, 
    val monto: Double, 
    val tipo: TipoMovimiento, 
    val cuentaId: String, 
    val esFijo: Boolean, 
    val fecha: String
)

data class MetaAhorro(
    val id: String, 
    val titulo: String, 
    val montoObjetivo: Double, 
    var montoActual: Double
)

data class CuentaPorPagar(
    val id: String, 
    val titulo: String, 
    val montoTotal: Double, 
    var saldoPendiente: Double, 
    val fechaVencimiento: String
)

// --- VIEWMODEL PRINCIPAL ---

class MainViewModel : ViewModel() {

    // 1. Cuentas principales con saldo inicial en 0.0
    val cuentas = mutableStateListOf(
        Cuenta("1", "Efectivo", 0.0),
        Cuenta("2", "Cuenta Bancaria", 0.0)
    )

    // 2. Listas de datos completamente vacías (Sin registros de prueba)
    val movimientos = mutableStateListOf<Movimiento>()
    val metasAhorro = mutableStateListOf<MetaAhorro>()
    val cuentasPorPagar = mutableStateListOf<CuentaPorPagar>()

    // 3. Presupuesto límite inicial en 0.0
    var presupuestoMensual by mutableStateOf(0.0)
        private set

    // --- CÁLCULOS AUTOMÁTICOS DE BALANCE ---

    val totalIngresosMes: Double
        get() = movimientos.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto }

    val totalGastosMes: Double
        get() = movimientos.filter { it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto }

    val balanceNeto: Double
        get() = totalIngresosMes - totalGastosMes

    val totalDeudasPendientes: Double
        get() = cuentasPorPagar.sumOf { it.saldoPendiente }

    // --- MÉTODOS DE GESTIÓN Y OPERACIÓN ---

    fun actualizarPresupuesto(nuevoPresupuesto: Double) {
        presupuestoMensual = nuevoPresupuesto
    }

    fun agregarMovimiento(concepto: String, monto: Double, tipo: TipoMovimiento, cuentaId: String, esFijo: Boolean) {
        val nuevoMovimiento = Movimiento(
            id = System.currentTimeMillis().toString(),
            concepto = concepto,
            monto = monto,
            tipo = tipo,
            cuentaId = cuentaId,
            esFijo = esFijo,
            fecha = "10 OCT 2026"
        )
        movimientos.add(nuevoMovimiento)

        // Actualizar el saldo de la cuenta elegida
        val cuenta = cuentas.find { it.id == cuentaId }
        cuenta?.let {
            if (tipo == TipoMovimiento.INGRESO) {
                it.saldo += monto
            } else {
                it.saldo -= monto
            }
        }
    }

    fun eliminarMovimiento(movimiento: Movimiento) {
        val cuenta = cuentas.find { it.id == movimiento.cuentaId }
        cuenta?.let {
            if (movimiento.tipo == TipoMovimiento.INGRESO) {
                it.saldo -= movimiento.monto
            } else {
                it.saldo += movimiento.monto
            }
        }
        movimientos.remove(movimiento)
    }

    fun realizarTransferencia(origenId: String, destinoId: String, monto: Double) {
        val origen = cuentas.find { it.id == origenId }
        val destino = cuentas.find { it.id == destinoId }
        if (origen != null && destino != null && origen.saldo >= monto) {
            origen.saldo -= monto
            destino.saldo += monto
        }
    }

    fun agregarMetaAhorro(titulo: String, montoObjetivo: Double) {
        metasAhorro.add(
            MetaAhorro(
                id = System.currentTimeMillis().toString(),
                titulo = titulo,
                montoObjetivo = montoObjetivo,
                montoActual = 0.0
            )
        )
    }

    fun abonarAMeta(meta: MetaAhorro, monto: Double, cuentaId: String) {
        val cuenta = cuentas.find { it.id == cuentaId }
        if (cuenta != null && cuenta.saldo >= monto) {
            cuenta.saldo -= monto
            meta.montoActual += monto
        } else if (cuenta != null) {
            meta.montoActual += monto
        }
    }

    fun agregarCuentaPorPagar(titulo: String, montoTotal: Double, fechaVencimiento: String) {
        cuentasPorPagar.add(
            CuentaPorPagar(
                id = System.currentTimeMillis().toString(),
                titulo = titulo,
                montoTotal = montoTotal,
                saldoPendiente = montoTotal,
                fechaVencimiento = fechaVencimiento
            )
        )
    }

    fun abonarCuentaPorPagar(deuda: CuentaPorPagar, monto: Double, cuentaId: String) {
        val cuenta = cuentas.find { it.id == cuentaId }
        if (cuenta != null) {
            cuenta.saldo -= monto
        }
        deuda.saldoPendiente = (deuda.saldoPendiente - monto).coerceAtLeast(0.0)
    }
}
