package com.monse.coinoptimize

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monse.coinoptimize.data.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).appDao()

    val cuentas = mutableStateListOf<Cuenta>()
    val movimientos = mutableStateListOf<Movimiento>()
    val metasAhorro = mutableStateListOf<MetaAhorro>()

    // Presupuesto mensual configurable
    var presupuestoMensual by mutableStateOf(0.0)

    init {
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

        viewModelScope.launch {
            dao.obtenerTodosLosMovimientos().collect { listaMovimientos ->
                movimientos.clear()
                movimientos.addAll(listaMovimientos)
            }
        }

        viewModelScope.launch {
            dao.obtenerTodasLasMetas().collect { listaMetas ->
                metasAhorro.clear()
                if (listaMetas.isEmpty()) {
                    val metaInicial = MetaAhorro(
                        id = UUID.randomUUID().toString(),
                        titulo = "FONDO DE EMERGENCIA",
                        montoObjetivo = 1000.0,
                        montoActual = 250.0
                    )
                    dao.insertarMeta(metaInicial)
                } else {
                    metasAhorro.addAll(listaMetas)
                }
            }
        }
    }

    val balanceNeto: Double
        get() = cuentas.sumOf { obtenerSaldoRealCuenta(it) }

    fun obtenerSaldoRealCuenta(cuenta: Cuenta): Double {
        val ingresos = movimientos.filter { it.cuentaId == cuenta.id && it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto }
        val gastos = movimientos.filter { it.cuentaId == cuenta.id && it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto }
        return cuenta.saldoActual + ingresos - gastos
    }

    val totalIngresosMes: Double
        get() = movimientos.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto }

    val totalGastosMes: Double
        get() = movimientos.filter { it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto }

    val cantidadRegistros: Int
        get() = movimientos.size

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

    fun editarMovimiento(movimientoActualizado: Movimiento) {
        viewModelScope.launch {
            dao.insertarMovimiento(movimientoActualizado)
        }
    }

    fun eliminarMovimiento(movimiento: Movimiento) {
        viewModelScope.launch {
            dao.eliminarMovimiento(movimiento)
        }
    }

    fun realizarTransferencia(origenId: String, destinoId: String, monto: Double) {
        if (origenId == destinoId || monto <= 0) return
        viewModelScope.launch {
            val origen = cuentas.find { it.id == origenId }
            val destino = cuentas.find { it.id == destinoId }

            if (origen != null && destino != null) {
                agregarMovimiento("Transferencia a ${destino.nombre}", monto, TipoMovimiento.GASTO, origenId)
                agregarMovimiento("Transferencia desde ${origen.nombre}", monto, TipoMovimiento.INGRESO, destinoId)
            }
        }
    }

    fun agregarMetaAhorro(titulo: String, montoObjetivo: Double) {
        viewModelScope.launch {
            val nuevaMeta = MetaAhorro(
                id = UUID.randomUUID().toString(),
                titulo = titulo.ifBlank { "NUEVA META" },
                montoObjetivo = montoObjetivo,
                montoActual = 0.0
            )
            dao.insertarMeta(nuevaMeta)
        }
    }

    fun editarMeta(metaActualizada: MetaAhorro) {
        viewModelScope.launch {
            dao.insertarMeta(metaActualizada)
        }
    }

    fun abonarAMeta(meta: MetaAhorro, montoAbono: Double, cuentaId: String) {
        if (montoAbono <= 0) return
        viewModelScope.launch {
            val metaActualizada = meta.copy(montoActual = meta.montoActual + montoAbono)
            dao.insertarMeta(metaActualizada)
            agregarMovimiento("Abono a Meta: ${meta.titulo}", montoAbono, TipoMovimiento.GASTO, cuentaId)
        }
    }

    fun eliminarMeta(meta: MetaAhorro) {
        viewModelScope.launch {
            dao.eliminarMeta(meta)
        }
    }

    fun actualizarPresupuesto(nuevoPresupuesto: Double) {
        presupuestoMensual = nuevoPresupuesto
    }
}
