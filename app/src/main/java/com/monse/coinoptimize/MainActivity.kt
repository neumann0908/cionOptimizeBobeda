package com.monse.coinoptimize

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monse.coinoptimize.data.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val viewModel: MainViewModel = viewModel(
                    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
                        context.applicationContext as android.app.Application
                    )
                )
                CajaFuerteMainScreen(viewModel = viewModel)
            }
        }
    }
}

val ColorHeaderBg = Color(0xFF141414)
val ColorScreenBg = Color(0xFFF4EFE6)
val ColorPrimaryRed = Color(0xFFC82323)
val ColorBorderBlack = Color(0xFF000000)
val ColorGreyIconBox = Color(0xFFB5B5B5)
val ColorTextDark = Color(0xFF1A1A1A)
val ColorTextMuted = Color(0xFF666666)

@Composable
fun CajaFuerteMainScreen(viewModel: MainViewModel) {
    var seccionSeleccionada by remember { mutableStateOf("INICIO") }
    var subSeccionMovimientos by remember { mutableStateOf("MENU") } // "MENU", "PAGOS_FIJOS", "INGRESOS_FIJOS", "INGRESOS_EXTRA", "CUENTAS_POR_PAGAR"

    var mostrarFormulario by remember { mutableStateOf(false) }
    var tipoMovimientoForm by remember { mutableStateOf(TipoMovimiento.INGRESO) }
    var esFijoForm by remember { mutableStateOf(false) }
    var mostrarTransferencia by remember { mutableStateOf(false) }
    var mostrarCrearMeta by remember { mutableStateOf(false) }
    var mostrarCrearDeuda by remember { mutableStateOf(false) }

    var movimientoSeleccionado by remember { mutableStateOf<Movimiento?>(null) }
    var metaAAbonar by remember { mutableStateOf<MetaAhorro?>(null) }
    var deudaAAbonar by remember { mutableStateOf<CuentaPorPagar?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = ColorScreenBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopHeaderBar(seccionActual = if (seccionSeleccionada == "MOVIMIENTOS" && subSeccionMovimientos != "MENU") subSeccionMovimientos else seccionSeleccionada)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (seccionSeleccionada) {
                    "INICIO" -> PantallaInicioContent(
                        viewModel = viewModel,
                        onIrAjustes = { seccionSeleccionada = "AJUSTES" },
                        onAbrirFormulario = { tipo, fijo ->
                            tipoMovimientoForm = tipo
                            esFijoForm = fijo
                            mostrarFormulario = true
                        }
                    )
                    "MOVIMIENTOS" -> PantallaCentroOperacionesContent(
                        viewModel = viewModel,
                        subSeccion = subSeccionMovimientos,
                        onCambiarSubSeccion = { subSeccionMovimientos = it },
                        onAbrirFormulario = { tipo, fijo ->
                            tipoMovimientoForm = tipo
                            esFijoForm = fijo
                            mostrarFormulario = true
                        },
                        onAbrirCrearDeuda = { mostrarCrearDeuda = true },
                        onSeleccionarMovimiento = { movimientoSeleccionado = it },
                        onAbonarDeuda = { deudaAAbonar = it }
                    )
                    "BALANCES" -> PantallaBalancesContent(
                        viewModel = viewModel,
                        onAbrirTransferencia = { mostrarTransferencia = true }
                    )
                    "AHORROS" -> PantallaAhorrosContent(
                        viewModel = viewModel,
                        onCrearMeta = { mostrarCrearMeta = true },
                        onAbonar = { metaAAbonar = it }
                    )
                    "AJUSTES" -> PantallaAjustesContent(viewModel = viewModel)
                }
            }

            BottomNavigationBar(
                seccionActual = seccionSeleccionada,
                onSeccionSelected = {
                    seccionSeleccionada = it
                    subSeccionMovimientos = "MENU"
                }
            )
        }

        if (mostrarFormulario) {
            FormularioMovimientoDialog(
                tipo = tipoMovimientoForm,
                esFijoInicial = esFijoForm,
                cuentas = viewModel.cuentas,
                onDismiss = { mostrarFormulario = false },
                onGuardar = { concepto, monto, cuentaId, esFijo ->
                    viewModel.agregarMovimiento(concepto, monto, tipoMovimientoForm, cuentaId, esFijo)
                }
            )
        }

        if (mostrarTransferencia) {
            FormularioTransferenciaDialog(
                cuentas = viewModel.cuentas,
                onDismiss = { mostrarTransferencia = false },
                onTransferir = { origenId, destinoId, monto ->
                    viewModel.realizarTransferencia(origenId, destinoId, monto)
                }
            )
        }

        if (mostrarCrearMeta) {
            FormularioCrearMetaDialog(
                onDismiss = { mostrarCrearMeta = false },
                onGuardar = { titulo, monto ->
                    viewModel.agregarMetaAhorro(titulo, monto)
                    mostrarCrearMeta = false
                }
            )
        }

        metaAAbonar?.let { meta ->
            FormularioAbonarMetaDialog(
                meta = meta,
                cuentas = viewModel.cuentas,
                onDismiss = { metaAAbonar = null },
                onAbonar = { monto, cuentaId ->
                    viewModel.abonarAMeta(meta, monto, cuentaId)
                    metaAAbonar = null
                }
            )
        }

        if (mostrarCrearDeuda) {
            FormularioCrearDeudaDialog(
                onDismiss = { mostrarCrearDeuda = false },
                onGuardar = { titulo, monto, vencimiento ->
                    viewModel.agregarCuentaPorPagar(titulo, monto, vencimiento)
                    mostrarCrearDeuda = false
                }
            )
        }

        deudaAAbonar?.let { deuda ->
            FormularioAbonarDeudaDialog(
                deuda = deuda,
                cuentas = viewModel.cuentas,
                onDismiss = { deudaAAbonar = null },
                onAbonar = { monto, cuentaId ->
                    viewModel.abonarCuentaPorPagar(deuda, monto, cuentaId)
                    deudaAAbonar = null
                }
            )
        }

        movimientoSeleccionado?.let { mov ->
            OpcionesMovimientoDialog(
                movimiento = mov,
                onDismiss = { movimientoSeleccionado = null },
                onEliminar = {
                    viewModel.eliminarMovimiento(mov)
                    movimientoSeleccionado = null
                }
            )
        }
    }
}

@Composable
fun TopHeaderBar(seccionActual: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorHeaderBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(ColorPrimaryRed).border(1.5.dp, ColorBorderBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = "CAJA FUERTE", color = ColorPrimaryRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(text = seccionActual.replace("_", " "), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(ColorPrimaryRed).padding(horizontal = 8.dp, vertical = 6.dp)) {
                Text(text = "OCT 2026", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryRed),
                shape = RoundedCornerShape(0.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "SALIR", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- PANTALLA INICIO (Exacta Referencia `1000295007_2.jpg`) ---
@Composable
fun PantallaInicioContent(
    viewModel: MainViewModel,
    onIrAjustes: () -> Unit,
    onAbrirFormulario: (TipoMovimiento, Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NeoBrutalCard(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(text = "INGRESOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$${String.format("%.2f", viewModel.totalIngresosMes)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                }
                IconBox3D(icon = Icons.Default.ArrowDownward, size = 28.dp, iconSize = 14.dp, bgColor = Color(0xFFE8F5E9), tint = Color(0xFF2E7D32))
            }
        }
        NeoBrutalCard(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(text = "GASTOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$${String.format("%.2f", viewModel.totalGastosMes)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ColorPrimaryRed)
                }
                IconBox3D(icon = Icons.Default.ArrowUpward, size = 28.dp, iconSize = 14.dp, bgColor = Color(0xFFFFEBEE), tint = ColorPrimaryRed)
            }
        }
    }

    NeoBrutalCard {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = "BALANCE NETO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorTextDark)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "$${String.format("%.2f", viewModel.balanceNeto)}", fontSize = 26.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
            }
            IconBox3D(icon = Icons.Default.Balance)
        }
    }

    NeoBrutalCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "PRESUPUESTO MENSUAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorTextDark)
                Box(modifier = Modifier.border(1.5.dp, ColorBorderBlack).background(Color.White).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(text = if (viewModel.presupuestoMensual > 0) "$${String.format("%.0f", viewModel.presupuestoMensual)}" else "SIN LÍMITE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val presupuesto = viewModel.presupuestoMensual
            val gastado = viewModel.totalGastosMes
            val progreso = if (presupuesto > 0) (gastado / presupuesto).toFloat().coerceIn(0f, 1f) else 0f

            Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(Color(0xFF333333)).border(1.dp, ColorBorderBlack)) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = progreso).background(ColorPrimaryRed))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "GASTADO $${String.format("%.2f", gastado)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorTextMuted)
                Text(text = "CONFIGURA EN AJUSTES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorPrimaryRed, modifier = Modifier.clickable { onIrAjustes() })
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = ColorPrimaryRed, contentColor = Color.White, onClick = { onAbrirFormulario(TipoMovimiento.INGRESO, false) }) {
            Text(text = "+ INGRESO EXTRA", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = Color.White, contentColor = ColorTextDark, onClick = { onAbrirFormulario(TipoMovimiento.GASTO, false) }) {
            Text(text = "REGISTRAR PAGO", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }

    Text(text = "PRÓXIMOS VENCIMIENTOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorTextDark)
    DashedContainer {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconBox3D(icon = Icons.Default.DateRange, size = 54.dp, iconSize = 32.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "SIN VENCIMIENTOS", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "No tienes pagos fijos ni cuentas por pagar próximas este mes.", fontSize = 12.sp, color = ColorTextMuted, textAlign = TextAlign.Center)
        }
    }
}

// --- CENTRO DE OPERACIONES MOVIMIENTOS (Referencia `1000295006_2.jpg`) ---
@Composable
fun PantallaCentroOperacionesContent(
    viewModel: MainViewModel,
    subSeccion: String,
    onCambiarSubSeccion: (String) -> Unit,
    onAbrirFormulario: (TipoMovimiento, Boolean) -> Unit,
    onAbrirCrearDeuda: () -> Unit,
    onSeleccionarMovimiento: (Movimiento) -> Unit,
    onAbonarDeuda: (CuentaPorPagar) -> Unit
) {
    if (subSeccion == "MENU") {
        Text(text = "ELIGE UNA SECCIÓN PARA REGISTRAR Y REVISAR TUS MOVIMIENTOS.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorTextMuted)

        val opciones = listOf(
            Triple("PAGOS FIJOS", "Renta, servicios y suscripciones recurrentes.", "PAGOS_FIJOS"),
            Triple("INGRESOS FIJOS", "Sueldo y entradas que se repiten cada periodo.", "INGRESOS_FIJOS"),
            Triple("INGRESOS EXTRA", "Ventas, freelance y entradas eventuales.", "INGRESOS_EXTRA"),
            Triple("CUENTAS POR PAGAR", "Deudas y saldos pendientes con abonos.", "CUENTAS_POR_PAGAR")
        )

        opciones.forEach { (titulo, desc, id) ->
            NeoBrutalCard(modifier = Modifier.clickable { onCambiarSubSeccion(id) }) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconBox3D(
                            icon = when(id) {
                                "PAGOS_FIJOS" -> Icons.Default.Receipt
                                "INGRESOS_FIJOS" -> Icons.Default.Work
                                "INGRESOS_EXTRA" -> Icons.Default.TrendingUp
                                else -> Icons.Default.SwapHoriz
                            },
                            size = 40.dp,
                            iconSize = 22.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = titulo, fontSize = 14.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = desc, fontSize = 11.sp, color = ColorTextMuted)
                        }
                    }
                    Text(text = ">", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ColorPrimaryRed)
                }
            }
        }
    } else {
        // Sub-vista específica
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            NeoBrutalButton(containerColor = ColorHeaderBg, contentColor = Color.White, onClick = { onCambiarSubSeccion("MENU") }) {
                Text(text = "< VOLVER", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            NeoBrutalButton(containerColor = ColorPrimaryRed, contentColor = Color.White, onClick = {
                when(subSeccion) {
                    "PAGOS_FIJOS" -> onAbrirFormulario(TipoMovimiento.GASTO, true)
                    "INGRESOS_FIJOS" -> onAbrirFormulario(TipoMovimiento.INGRESO, true)
                    "INGRESOS_EXTRA" -> onAbrirFormulario(TipoMovimiento.INGRESO, false)
                    "CUENTAS_POR_PAGAR" -> onAbrirCrearDeuda()
                }
            }) {
                Text(text = "+ NUEVO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        when (subSeccion) {
            "PAGOS_FIJOS" -> {
                val lista = viewModel.movimientos.filter { it.tipo == TipoMovimiento.GASTO && it.esFijo }
                ListaMovimientosSub(lista = lista, tituloVacio = "SIN PAGOS FIJOS", descVacio = "Registra tus pagos recurrentes como renta o servicios.", onSeleccionar = onSeleccionarMovimiento)
            }
            "INGRESOS_FIJOS" -> {
                val lista = viewModel.movimientos.filter { it.tipo == TipoMovimiento.INGRESO && it.esFijo }
                ListaMovimientosSub(lista = lista, tituloVacio = "SIN INGRESOS FIJOS", descVacio = "Registra entradas recurrentes como salario o rentas.", onSeleccionar = onSeleccionarMovimiento)
            }
            "INGRESOS_EXTRA" -> {
                val lista = viewModel.movimientos.filter { it.tipo == TipoMovimiento.INGRESO && !it.esFijo }
                ListaMovimientosSub(lista = lista, tituloVacio = "SIN INGRESOS EXTRA", descVacio = "Anota ingresos puntuales fuera de tu flujo fijo.", onSeleccionar = onSeleccionarMovimiento)
            }
            "CUENTAS_POR_PAGAR" -> {
                if (viewModel.cuentasPorPagar.isEmpty()) {
                    DashedContainer {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            IconBox3D(icon = Icons.Default.ReceiptLong, size = 54.dp, iconSize = 32.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "SIN CUENTAS POR PAGAR", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Registra tus deudas y controla cada abono hasta liquidarlas.", fontSize = 12.sp, color = ColorTextMuted, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        viewModel.cuentasPorPagar.forEach { deuda ->
                            NeoBrutalCard {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(text = deuda.titulo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorTextDark)
                                            Text(text = "Vence: ${deuda.fechaVencimiento}", fontSize = 10.sp, color = ColorTextMuted)
                                        }
                                        Text(text = "$${String.format("\%.2f", deuda.saldoPendiente)} / $${String.format("%.0f", deuda.montoTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = ColorPrimaryRed)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        NeoBrutalButton(containerColor = ColorHeaderBg, contentColor = Color.White, onClick = { onAbonarDeuda(deuda) }) {
                                            Text(text = "+ ABONAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListaMovimientosSub(lista: List<Movimiento>, tituloVacio: String, descVacio: String, onSeleccionar: (Movimiento) -> Unit) {
    if (lista.isEmpty()) {
        DashedContainer {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                IconBox3D(icon = Icons.Default.Paid, size = 54.dp, iconSize = 32.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = tituloVacio, fontSize = 16.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = descVacio, fontSize = 12.sp, color = ColorTextMuted, textAlign = TextAlign.Center)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            lista.forEach { mov ->
                ItemMovimientoCard(movimiento = mov, onClick = { onSeleccionar(mov) })
            }
        }
    }
}

// --- PANTALLA BALANCES (Con selector Semanal/Mensual exacto a referencia `1000295002_2.jpg`) ---
@Composable
fun PantallaBalancesContent(viewModel: MainViewModel, onAbrirTransferencia: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.weight(1f).background(if (viewModel.modoBalances == "SEMANAL") ColorPrimaryRed else Color.White).border(2.dp, ColorBorderBlack).clickable { viewModel.modoBalances = "SEMANAL" }.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "SEMANAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (viewModel.modoBalances == "SEMANAL") Color.White else ColorTextDark)
        }
        Box(
            modifier = Modifier.weight(1f).background(if (viewModel.modoBalances == "MENSUAL") ColorPrimaryRed else Color.White).border(2.dp, ColorBorderBlack).clickable { viewModel.modoBalances = "MENSUAL" }.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "MENSUAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (viewModel.modoBalances == "MENSUAL") Color.White else ColorTextDark)
        }
    }

    NeoBrutalCard {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(Color.White).border(1.5.dp, ColorBorderBlack).clickable { }.padding(8.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(text = if (viewModel.modoBalances == "SEMANAL") "SEM 41 · 2026" else viewModel.periodoActual, fontSize = 13.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
            Box(modifier = Modifier.background(Color.White).border(1.5.dp, ColorBorderBlack).clickable { }.padding(8.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricMiniCard(modifier = Modifier.weight(1f), title = "INGRESOS", value = "$${String.format("\%.2f", viewModel.totalIngresosMes)}", icon = Icons.Default.TrendingUp, iconBg = Color(0xFF2E7D32), iconTint = Color.White)         MetricMiniCard(modifier = Modifier.weight(1f), title = "GASTOS", value = "$${String.format("%.2f", viewModel.totalGastosMes)}", icon = Icons.Default.TrendingDown, iconBg = ColorPrimaryRed, iconTint = Color.White)
        MetricMiniCard(modifier = Modifier.weight(1f), title = "NETO", value = "$${String.format("\%.2f", viewModel.balanceNeto)}", icon = Icons.Default.AccountBalance, iconBg = ColorHeaderBg, iconTint = Color.White)     }      NeoBrutalCard {         Column(modifier = Modifier.padding(16.dp)) {             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {                 Text(text = "BÓVEDAS Y CUENTAS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = ColorTextDark)                 NeoBrutalButton(containerColor = ColorPrimaryRed, contentColor = Color.White, onClick = onAbrirTransferencia) {                     Text(text = "TRANSFERIR", fontSize = 10.sp, fontWeight = FontWeight.Bold)                 }             }             Spacer(modifier = Modifier.height(12.dp))             viewModel.cuentas.forEach { cuenta ->                 val saldo = viewModel.obtenerSaldoRealCuenta(cuenta)                 Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {                     Text(text = cuenta.nombre, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorTextMuted)                     Text(text = "$${String.format("%.2f", saldo)}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                }
            }
        }
    }
}

// --- PANTALLA AHORROS ---
@Composable
fun PantallaAhorrosContent(viewModel: MainViewModel, onCrearMeta: () -> Unit, onAbonar: (MetaAhorro) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "METAS DE AHORRO", fontSize = 14.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
        NeoBrutalButton(containerColor = ColorPrimaryRed, contentColor = Color.White, onClick = onCrearMeta) {
            Text(text = "+ NUEVA META", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (viewModel.metasAhorro.isEmpty()) {
        DashedContainer {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                IconBox3D(icon = Icons.Default.Savings, size = 54.dp, iconSize = 32.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "SIN METAS TODAVÍA", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Crea tu primera meta de ahorro y observa cómo crece tu progreso.", fontSize = 12.sp, color = ColorTextMuted, textAlign = TextAlign.Center)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            viewModel.metasAhorro.forEach { meta ->
                val porcentaje = if (meta.montoObjetivo > 0) (meta.montoActual / meta.montoObjetivo) * 100 else 0.0
                NeoBrutalCard {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(text = meta.titulo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorTextDark)
                                Text(text = "${String.format("%.1f", porcentaje)}% completado", fontSize = 10.sp, color = ColorTextMuted)
                            }
                            Text(text = "$${String.format("\%.2f", meta.montoActual)} / $${String.format("%.0f", meta.montoObjetivo)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Color(0xFFE0E0E0)).border(1.dp, ColorBorderBlack)) {
                            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = (porcentaje / 100).toFloat().coerceIn(0f, 1f)).background(Color(0xFF2E7D32)))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            NeoBrutalButton(containerColor = ColorHeaderBg, contentColor = Color.White, onClick = { onAbonar(meta) }) {
                                Text(text = "+ ABONAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PANTALLA AJUSTES ---
@Composable
fun PantallaAjustesContent(viewModel: MainViewModel) {
    var presupuestoInput by remember { mutableStateOf(if (viewModel.presupuestoMensual > 0) viewModel.presupuestoMensual.toString() else "") }
    var guardado by remember { mutableStateOf(false) }

    NeoBrutalCard {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(text = "CONFIGURACIÓN DE PRESUPUESTO", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
            OutlinedTextField(
                value = presupuestoInput,
                onValueChange = { presupuestoInput = it },
                label = { Text("Presupuesto Mensual ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBorderBlack, unfocusedBorderColor = ColorBorderBlack, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            NeoBrutalButton(containerColor = ColorPrimaryRed, contentColor = Color.White, onClick = {
                viewModel.actualizarPresupuesto(presupuestoInput.toDoubleOrNull() ?: 0.0)
                guardado = true
            }) {
                Text("GUARDAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            if (guardado) {
                Text("¡Guardado correctamente!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
        }
    }
}

// --- DIÁLOGOS DE CREACIÓN Y ABONOS ---
@Composable
fun FormularioCrearDeudaDialog(onDismiss: () -> Unit, onGuardar: (String, Double, String) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var montoText by remember { mutableStateOf("") }
    var vencimiento by remember { mutableStateOf("31 OCT 2026") }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(ColorBorderBlack))
            Column(modifier = Modifier.fillMaxWidth().background(ColorScreenBg).border(3.dp, ColorBorderBlack).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "REGISTRAR CUENTA POR PAGAR", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título / Deuda") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBorderBlack, unfocusedBorderColor = ColorBorderBlack, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = montoText, onValueChange = { montoText = it }, label = { Text("Monto Total ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBorderBlack, unfocusedBorderColor = ColorBorderBlack, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = vencimiento, onValueChange = { vencimiento = it }, label = { Text("Fecha de Vencimiento") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBorderBlack, unfocusedBorderColor = ColorBorderBlack, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White), singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = Color.White, contentColor = ColorTextDark, onClick = onDismiss) { Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = ColorPrimaryRed, contentColor = Color.White, onClick = {
                        val m = montoText.toDoubleOrNull() ?: 0.0
                        if (m > 0) onGuardar(titulo, m, vencimiento)
                    }) { Text("GUARDAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun FormularioAbonarDeudaDialog(deuda: CuentaPorPagar, cuentas: List<Cuenta>, onDismiss: () -> Unit, onAbonar: (Double, String) -> Unit) {
    var abonoText by remember { mutableStateOf("") }
    var cuentaIdSeleccionada by remember { mutableStateOf(cuentas.firstOrNull()?.id ?: "1") }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(ColorBorderBlack))
            Column(modifier = Modifier.fillMaxWidth().background(ColorScreenBg).border(3.dp, ColorBorderBlack).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "ABONAR A: ${deuda.titulo}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                OutlinedTextField(value = abonoText, onValueChange = { abonoText = it }, label = { Text("Monto del Abono ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBorderBlack, unfocusedBorderColor = ColorBorderBlack, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White), singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("ORIGEN DEL DINERO:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cuentas.forEach { c ->
                        val sel = c.id == cuentaIdSeleccionada
                        Box(modifier = Modifier.weight(1f).background(if (sel) ColorPrimaryRed else Color.White).border(1.5.dp, ColorBorderBlack).clickable { cuentaIdSeleccionada = c.id }.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                            Text(c.nombre, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else ColorTextDark)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = Color.White, contentColor = ColorTextDark, onClick = onDismiss) { Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = ColorPrimaryRed, contentColor = Color.White, onClick = {
                        val m = abonoText.toDoubleOrNull() ?: 0.0
                        if (m > 0) onAbonar(m, cuentaIdSeleccionada)
                    }) { Text("ABONAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// (Resto de diálogos estándar ya existentes)
@Composable
fun FormularioMovimientoDialog(tipo: TipoMovimiento, esFijoInicial: Boolean, cuentas: List<Cuenta>, onDismiss: () -> Unit, onGuardar: (String, Double, String, Boolean) -> Unit) {
    var concepto by remember { mutableStateOf("") }
    var montoText by remember { mutableStateOf("") }
    var cuentaIdSeleccionada by remember { mutableStateOf(cuentas.firstOrNull()?.id ?: "1") }
    var esFijo by remember { mutableStateOf(esFijoInicial) }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(ColorBorderBlack))
            Column(modifier = Modifier.fillMaxWidth().background(ColorScreenBg).border(3.dp, ColorBorderBlack).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = if (tipo == TipoMovimiento.INGRESO) "REGISTRAR INGRESO" else "REGISTRAR GASTO", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                OutlinedTextField(value = concepto, onValueChange = { concepto = it }, label = { Text("Concepto / Descripción") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBorderBlack, unfocusedBorderColor = ColorBorderBlack, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = montoText, onValueChange = { montoText = it }, label = { Text("Monto ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBorderBlack, unfocusedBorderColor = ColorBorderBlack, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White), singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("CUENTA:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cuentas.forEach { c ->
                        val sel = c.id == cuentaIdSeleccionada
                        Box(modifier = Modifier.weight(1f).background(if (sel) ColorHeaderBg else Color.White).border(1.5.dp, ColorBorderBlack).clickable { cuentaIdSeleccionada = c.id }.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                            Text(c.nombre, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else ColorTextDark)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = Color.White, contentColor = ColorTextDark, onClick = onDismiss) { Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = ColorPrimaryRed, contentColor = Color.White, onClick = {
                        val m = montoText.toDoubleOrNull() ?: 0.0
                        if (m > 0) {
                            onGuardar(concepto, m, cuentaIdSeleccionada, esFijo)
                            onDismiss()
                        }
                    }) { Text("GUARDAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun FormularioTransferenciaDialog(cuentas: List<Cuenta>, onDismiss: () -> Unit, onTransferir: (String, String, Double) -> Unit) {
    var origenId by remember { mutableStateOf(cuentas.firstOrNull()?.id ?: "1") }
    var destinoId by remember { mutableStateOf(cuentas.getOrNull(1)?.id ?: "2") }
    var montoText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(ColorBorderBlack))
            Column(modifier = Modifier.fillMaxWidth().background(ColorScreenBg).border(3.dp, ColorBorderBlack).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "TRANSFERIR FONDOS", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                OutlinedTextField(value = montoText, onValueChange = { montoText = it }, label = { Text("Monto ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBorderBlack, unfocusedBorderColor = ColorBorderBlack, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White), singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = Color.White, contentColor = ColorTextDark, onClick = onDismiss) { Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = ColorPrimaryRed, contentColor = Color.White, onClick = {
                        val m = montoText.toDoubleOrNull() ?: 0.0
                        if (m > 0) {
                            onTransferir(origenId, destinoId, m)
                            onDismiss()
                        }
                    }) { Text("TRANSFERIR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun FormularioCrearMetaDialog(onDismiss: () -> Unit, onGuardar: (String, Double) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var montoText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(ColorBorderBlack))
            Column(modifier = Modifier.fillMaxWidth().background(ColorScreenBg).border(3.dp, ColorBorderBlack).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "NUEVA META DE AHORRO", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título / Objetivo") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBorderBlack, unfocusedBorderColor = ColorBorderBlack, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = montoText, onValueChange = { montoText = it }, label = { Text("Monto Objetivo ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBorderBlack, unfocusedBorderColor = ColorBorderBlack, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White), singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = Color.White, contentColor = ColorTextDark, onClick = onDismiss) { Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = ColorPrimaryRed, contentColor = Color.White, onClick = {
                        val m = montoText.toDoubleOrNull() ?: 0.0
                        if (m > 0) onGuardar(titulo, m)
                    }) { Text("CREAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun FormularioAbonarMetaDialog(meta: MetaAhorro, cuentas: List<Cuenta>, onDismiss: () -> Unit, onAbonar: (Double, String) -> Unit) {
    var montoText by remember { mutableStateOf("") }
    var cuentaIdSeleccionada by remember { mutableStateOf(cuentas.firstOrNull()?.id ?: "1") }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(ColorBorderBlack))
            Column(modifier = Modifier.fillMaxWidth().background(ColorScreenBg).border(3.dp, ColorBorderBlack).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "ABONAR A: ${meta.titulo}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                OutlinedTextField(value = montoText, onValueChange = { montoText = it }, label = { Text("Monto del Abono ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBorderBlack, unfocusedBorderColor = ColorBorderBlack, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White), singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = Color.White, contentColor = ColorTextDark, onClick = onDismiss) { Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = ColorPrimaryRed, contentColor = Color.White, onClick = {
                        val m = montoText.toDoubleOrNull() ?: 0.0
                        if (m > 0) onAbonar(m, cuentaIdSeleccionada)
                    }) { Text("ABONAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun OpcionesMovimientoDialog(movimiento: Movimiento, onDismiss: () -> Unit, onEliminar: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(ColorBorderBlack))
            Column(modifier = Modifier.fillMaxWidth().background(ColorScreenBg).border(3.dp, ColorBorderBlack).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "ELIMINAR REGISTRO", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
                Text(text = "${movimiento.concepto} -$${String.format("%.2f", movimiento.monto)}", fontSize = 13.sp, color = ColorTextMuted)
                NeoBrutalButton(containerColor = ColorPrimaryRed, contentColor = Color.White, onClick = onEliminar) { Text("ELIMINAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                NeoBrutalButton(containerColor = Color.White, contentColor = ColorTextDark, onClick = onDismiss) { Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun ItemMovimientoCard(movimiento: Movimiento, onClick: () -> Unit = {}) {
    val esIngreso = movimiento.tipo == TipoMovimiento.INGRESO
    val signo = if (esIngreso) "+" else "-"
    val colorMonto = if (esIngreso) Color(0xFF2E7D32) else ColorPrimaryRed

    NeoBrutalCard(modifier = Modifier.clickable { onClick() }) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBox3D(icon = if (esIngreso) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward, size = 36.dp, iconSize = 20.dp, bgColor = if (esIngreso) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), tint = colorMonto)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = movimiento.concepto, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorTextDark)
                    Text(text = movimiento.fecha, fontSize = 10.sp, color = ColorTextMuted)
                }
            }
            Text(text = "$signo$${String.format("%.2f", movimiento.monto)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = colorMonto)
        }
    }
}

@Composable
fun NeoBrutalCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier) {
        Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).background(ColorBorderBlack))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White).border(2.dp, ColorBorderBlack)) { content() }
    }
}

@Composable
fun NeoBrutalButton(modifier: Modifier = Modifier, containerColor: Color, contentColor: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(modifier = modifier.clickable { onClick() }) {
        Box(modifier = Modifier.matchParentSize().offset(x = 3.dp, y = 3.dp).background(ColorBorderBlack))
        Box(modifier = Modifier.fillMaxWidth().background(containerColor).border(2.dp, ColorBorderBlack).padding(vertical = 10.dp, horizontal = 12.dp), contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalContentColor provides contentColor) { content() }
        }
    }
}

@Composable
fun IconBox3D(icon: ImageVector, size: Dp = 38.dp, iconSize: Dp = 20.dp, bgColor: Color = ColorGreyIconBox, tint: Color = ColorTextDark) {
    Box(modifier = Modifier.size(size).background(bgColor).border(2.dp, ColorBorderBlack), contentAlignment = Alignment.Center) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun MetricMiniCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, iconBg: Color, iconTint: Color) {
    NeoBrutalCard(modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
            IconBox3D(icon = icon, size = 32.dp, iconSize = 18.dp, bgColor = iconBg, tint = iconTint)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ColorTextMuted)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = ColorTextDark)
        }
    }
}

@Composable
fun DashedContainer(content: @Composable () -> Unit) {
    val stroke = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f))
    Box(modifier = Modifier.fillMaxWidth().drawWithContent { drawContent(); drawRect(color = ColorBorderBlack, style = stroke) }.background(ColorScreenBg)) { content() }
}

@Composable
fun BottomNavigationBar(seccionActual: String, onSeccionSelected: (String) -> Unit) {
    val items = listOf(
        Triple("INICIO", Icons.Default.AccountBalanceWallet, "INICIO"),
        Triple("MOVIMIENTOS", Icons.Default.SwapHoriz, "MOVIMIENTOS"),
        Triple("BALANCES", Icons.Default.Scale, "BALANCES"),
        Triple("AHORROS", Icons.Default.Savings, "AHORROS"),
        Triple("AJUSTES", Icons.Default.Settings, "AJUSTES")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(ColorBorderBlack))
        Row(modifier = Modifier.fillMaxWidth().background(ColorScreenBg).padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            items.forEach { (id, icon, label) ->
                val seleccionado = seccionActual == id
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSeccionSelected(id) }.padding(horizontal = 4.dp)) {
                    Box(modifier = Modifier.size(32.dp).then(if (seleccionado) Modifier.background(Color.White).border(2.dp, ColorBorderBlack) else Modifier), contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = label, tint = if (seleccionado) ColorPrimaryRed else ColorTextDark, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (seleccionado) ColorPrimaryRed else ColorTextDark)
                }
            }
        }
    }
}
