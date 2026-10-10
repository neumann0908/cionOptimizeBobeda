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
    val cuentasPorPagar = mutableStateListOf<CuentaPorPagar>()

    var presupuestoMensual by mutableStateOf(0.0)
    var modoBalances by mutableStateOf("MENSUAL")
    var periodoActual by mutableStateOf("OCT 2026")

    init {
        viewModelScope.launch {
            dao.obtenerTodasLasCuentas().collect { listaCuentas ->
                cuentas.clear()
                if (listaCuentas.isEmpty()) {
                    // Cuentas iniciales en 0.0 para empezar completamente limpio
                    val cuentasIniciales = listOf(
                        Cuenta(id = "1", nombre = "EFECTIVO", saldoActual = 0.0, tipo = TipoCuenta.EFECTIVO),
                        Cuenta(id = "2", nombre = "BANCO PRINCIPAL", saldoActual = 0.0, tipo = TipoCuenta.BANCO)
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
                // Sin metas predeterminadas para iniciar en blanco
                metasAhorro.addAll(listaMetas)
            }
        }

        viewModelScope.launch {
            dao.obtenerCuentasPorPagar().collect { listaDeudas ->
                cuentasPorPagar.clear()
                cuentasPorPagar.addAll(listaDeudas)
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

    val totalDeudasPendientes: Double
        get() = cuentasPorPagar.sumOf { it.saldoPendiente }

    fun agregarMovimiento(concepto: String, monto: Double, tipo: TipoMovimiento, cuentaId: String, esFijo: Boolean = false) {
        viewModelScope.launch {
            val nuevo = Movimiento(
                id = UUID.randomUUID().toString(),
                concepto = concepto.ifBlank { "Sin descripción" },
                monto = monto,
                tipo = tipo,
                fecha = periodoActual,
                cuentaId = cuentaId,
                esFijo = esFijo
            )
            dao.insertarMovimiento(nuevo)
        }
    }

    fun editarMovimiento(mov: Movimiento) {
        viewModelScope.launch { dao.insertarMovimiento(mov) }
    }

    fun eliminarMovimiento(mov: Movimiento) {
        viewModelScope.launch { dao.eliminarMovimiento(mov) }
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

    fun agregarCuentaPorPagar(titulo: String, monto: Double, vencimiento: String) {
        viewModelScope.launch {
            val nueva = CuentaPorPagar(
                id = UUID.randomUUID().toString(),
                titulo = titulo.ifBlank { "CUENTA POR PAGAR" },
                montoTotal = monto,
                saldoPendiente = monto,
                fechaVencimiento = vencimiento.ifBlank { "FIN DE MES" }
            )
            dao.insertarCuentaPorPagar(nueva)
        }
    }

    fun abonarCuentaPorPagar(deuda: CuentaPorPagar, abono: Double, cuentaId: String) {
        if (abono <= 0) return
        viewModelScope.launch {
            val nuevoSaldo = (deuda.saldoPendiente - abono).coerceAtLeast(0.0)
            if (nuevoSaldo == 0.0) {
                dao.eliminarCuentaPorPagar(deuda)
            } else {
                dao.insertarCuentaPorPagar(deuda.copy(saldoPendiente = nuevoSaldo))
            }
            agregarMovimiento("Abono Deuda: ${deuda.titulo}", abono, TipoMovimiento.GASTO, cuentaId)
        }
    }

    fun agregarMetaAhorro(titulo: String, objetivo: Double) {
        viewModelScope.launch {
            dao.insertarMeta(MetaAhorro(UUID.randomUUID().toString(), titulo, objetivo, 0.0))
        }
    }

    fun abonarAMeta(meta: MetaAhorro, monto: Double, cuentaId: String) {
        if (monto <= 0) return
        viewModelScope.launch {
            dao.insertarMeta(meta.copy(montoActual = meta.montoActual + monto))
            agregarMovimiento("Ahorro: ${meta.titulo}", monto, TipoMovimiento.GASTO, cuentaId)
        }
    }

    fun eliminarMeta(meta: MetaAhorro) {
        viewModelScope.launch { dao.eliminarMeta(meta) }
    }

    fun actualizarPresupuesto(valor: Double) {
        presupuestoMensual = valor
    }
}
