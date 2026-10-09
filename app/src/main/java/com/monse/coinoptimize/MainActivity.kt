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

// --- PALETA DE COLORES DINÁMICA (CLARO / OSCURO) ---
data class AppThemeColors(
    val headerBg: Color,
    val screenBg: Color,
    val cardBg: Color,
    val textDark: Color,
    val textMuted: Color,
    val primaryRed: Color,
    val borderBlack: Color,
    val iconBoxBg: Color
)

val LightTheme = AppThemeColors(
    headerBg = Color(0xFF141414),
    screenBg = Color(0xFFF4EFE6),
    cardBg = Color.White,
    textDark = Color(0xFF1A1A1A),
    textMuted = Color(0xFF666666),
    primaryRed = Color(0xFFC82323),
    borderBlack = Color(0xFF000000),
    iconBoxBg = Color(0xFFB5B5B5)
)

val DarkTheme = AppThemeColors(
    headerBg = Color(0xFF0A0A0A),
    screenBg = Color(0xFF121212),
    cardBg = Color(0xFF1E1E1E),
    textDark = Color(0xFFE0E0E0),
    textMuted = Color(0xFFA0A0A0),
    primaryRed = Color(0xFFE53935),
    borderBlack = Color(0xFFFFFFFF),
    iconBoxBg = Color(0xFF333333)
)

@Composable
fun CajaFuerteMainScreen(viewModel: MainViewModel) {
    var seccionSeleccionada by remember { mutableStateOf("INICIO") }
    var subSeccionMovimientos by remember { mutableStateOf("MENU") }

    // Estados Globales de Ajustes e Interfaz
    var esTemaOscuro by remember { mutableStateOf(false) }
    val theme = if (esTemaOscuro) DarkTheme else LightTheme

    var monedaSeleccionada by remember { mutableStateOf("MXN - PESO MEXICANO") }
    var formatoFecha by remember { mutableStateOf("DD/MM/AAAA") }
    
    // Navegador de Meses Global
    val mesesDisponibles = listOf("JUN 2026", "JUL 2026", "AGO 2026", "SEP 2026", "OCT 2026", "NOV 2026")
    var indexMesActual by remember { mutableStateOf(4) } // OCT 2026 por defecto
    val mesActualStr = mesesDisponibles[indexMesActual]

    var mostrarFormulario by remember { mutableStateOf(false) }
    var tipoMovimientoForm by remember { mutableStateOf(TipoMovimiento.INGRESO) }
    var esFijoForm by remember { mutableStateOf(false) }
    var mostrarTransferencia by remember { mutableStateOf(false) }
    var mostrarCrearMeta by remember { mutableStateOf(false) }
    var mostrarCrearDeuda by remember { mutableStateOf(false) }

    var movimientoSeleccionado by remember { mutableStateOf<Movimiento?>(null) }
    var metaAAbonar by remember { mutableStateOf<MetaAhorro?>(null) }
    var deudaAAbonar by remember { mutableStateOf<CuentaPorPagar?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = theme.screenBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopHeaderBar(
                seccionActual = if (seccionSeleccionada == "MOVIMIENTOS" && subSeccionMovimientos != "MENU") {
                    subSeccionMovimientos
                } else {
                    seccionSeleccionada
                },
                mesActual = mesActualStr,
                theme = theme
            )

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
                        theme = theme,
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
                        theme = theme,
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
                        theme = theme,
                        mesActual = mesActualStr,
                        onAnteriorMes = { if (indexMesActual > 0) indexMesActual-- },
                        onSiguienteMes = { if (indexMesActual < mesesDisponibles.size - 1) indexMesActual++ },
                        onAbrirTransferencia = { mostrarTransferencia = true }
                    )
                    "AHORROS" -> PantallaAhorrosContent(
                        viewModel = viewModel,
                        theme = theme,
                        onCrearMeta = { mostrarCrearMeta = true },
                        onAbonar = { metaAAbonar = it }
                    )
                    "AJUSTES" -> PantallaAjustesContent(
                        viewModel = viewModel,
                        theme = theme,
                        monedaSeleccionada = monedaSeleccionada,
                        onMonedaChange = { monedaSeleccionada = it },
                        formatoFecha = formatoFecha,
                        onFormatoFechaChange = { formatoFecha = it },
                        esTemaOscuro = esTemaOscuro,
                        onTemaChange = { esTemaOscuro = it }
                    )
                }
            }

            BottomNavigationBar(
                seccionActual = seccionSeleccionada,
                theme = theme,
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
                theme = theme,
                onDismiss = { mostrarFormulario = false },
                onGuardar = { concepto, monto, cuentaId, esFijo ->
                    viewModel.agregarMovimiento(concepto, monto, tipoMovimientoForm, cuentaId, esFijo)
                }
            )
        }

        if (mostrarTransferencia) {
            FormularioTransferenciaDialog(
                cuentas = viewModel.cuentas,
                theme = theme,
                onDismiss = { mostrarTransferencia = false },
                onTransferir = { origenId, destinoId, monto ->
                    viewModel.realizarTransferencia(origenId, destinoId, monto)
                }
            )
        }

        if (mostrarCrearMeta) {
            FormularioCrearMetaDialog(
                theme = theme,
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
                theme = theme,
                onDismiss = { metaAAbonar = null },
                onAbonar = { monto, cuentaId ->
                    viewModel.abonarAMeta(meta, monto, cuentaId)
                    metaAAbonar = null
                }
            )
        }

        if (mostrarCrearDeuda) {
            FormularioCrearDeudaDialog(
                theme = theme,
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
                theme = theme,
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
                theme = theme,
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
fun TopHeaderBar(seccionActual: String, mesActual: String, theme: AppThemeColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.headerBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(theme.primaryRed)
                    .border(1.5.dp, theme.borderBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "CAJA FUERTE",
                    color = theme.primaryRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = seccionActual.replace("_", " "),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(theme.primaryRed)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = when (seccionActual) {
                        "AHORROS" -> "METAS Y PROGRESO"
                        "AJUSTES" -> "CONFIGURACIÓN"
                        else -> mesActual
                    },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryRed),
                shape = RoundedCornerShape(0.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SALIR",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PantallaInicioContent(
    viewModel: MainViewModel,
    theme: AppThemeColors,
    onIrAjustes: () -> Unit,
    onAbrirFormulario: (TipoMovimiento, Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NeoBrutalCard(theme = theme, modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = "INGRESOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$${String.format("%.2f", viewModel.totalIngresosMes)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                }
                IconBox3D(icon = Icons.Default.ArrowDownward, size = 28.dp, iconSize = 14.dp, bgColor = Color(0xFFE8F5E9), tint = Color(0xFF2E7D32), theme = theme)
            }
        }
        NeoBrutalCard(theme = theme, modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = "GASTOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$${String.format("%.2f", viewModel.totalGastosMes)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = theme.primaryRed)
                }
                IconBox3D(icon = Icons.Default.ArrowUpward, size = 28.dp, iconSize = 14.dp, bgColor = Color(0xFFFFEBEE), tint = theme.primaryRed, theme = theme)
            }
        }
    }

    NeoBrutalCard(theme = theme) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "BALANCE NETO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "$${String.format("%.2f", viewModel.balanceNeto)}", fontSize = 26.sp, fontWeight = FontWeight.Black, color = theme.textDark)
            }
            IconBox3D(icon = Icons.Default.Balance, theme = theme)
        }
    }

    NeoBrutalCard(theme = theme) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "PRESUPUESTO MENSUAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
                Box(
                    modifier = Modifier
                        .border(1.5.dp, theme.borderBlack)
                        .background(theme.cardBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (viewModel.presupuestoMensual > 0) "$${String.format("%.0f", viewModel.presupuestoMensual)}" else "SIN LÍMITE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textDark
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val presupuesto = viewModel.presupuestoMensual
            val gastado = viewModel.totalGastosMes
            val progreso = if (presupuesto > 0) (gastado / presupuesto).toFloat().coerceIn(0f, 1f) else 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(Color(0xFF333333))
                    .border(1.dp, theme.borderBlack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progreso)
                        .background(theme.primaryRed)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "GASTADO $${String.format("%.2f", gastado)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
                Text(
                    text = "CONFIGURA EN AJUSTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryRed,
                    modifier = Modifier.clickable { onIrAjustes() }
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NeoBrutalButton(
            modifier = Modifier.weight(1f),
            containerColor = theme.primaryRed,
            contentColor = Color.White,
            theme = theme,
            onClick = { onAbrirFormulario(TipoMovimiento.INGRESO, false) }
        ) {
            Text(text = "+ INGRESO EXTRA", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        NeoBrutalButton(
            modifier = Modifier.weight(1f),
            containerColor = theme.cardBg,
            contentColor = theme.textDark,
            theme = theme,
            onClick = { onAbrirFormulario(TipoMovimiento.GASTO, false) }
        ) {
            Text(text = "REGISTRAR PAGO", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }

    Text(text = "PRÓXIMOS VENCIMIENTOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
    DashedContainer(theme = theme) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconBox3D(icon = Icons.Default.DateRange, size = 54.dp, iconSize = 32.dp, theme = theme)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "SIN VENCIMIENTOS", fontSize = 16.sp, fontWeight = FontWeight.Black, color = theme.textDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "No tienes pagos fijos ni cuentas por pagar próximas este mes.", fontSize = 12.sp, color = theme.textMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun PantallaCentroOperacionesContent(
    viewModel: MainViewModel,
    subSeccion: String,
    theme: AppThemeColors,
    onCambiarSubSeccion: (String) -> Unit,
    onAbrirFormulario: (TipoMovimiento, Boolean) -> Unit,
    onAbrirCrearDeuda: () -> Unit,
    onSeleccionarMovimiento: (Movimiento) -> Unit,
    onAbonarDeuda: (CuentaPorPagar) -> Unit
) {
    if (subSeccion == "MENU") {
        Text(text = "ELIGE UNA SECCIÓN PARA REGISTRAR Y REVISAR TUS MOVIMIENTOS.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)

        val opciones = listOf(
            Triple("PAGOS FIJOS", "Renta, servicios y suscripciones recurrentes.", "PAGOS_FIJOS"),
            Triple("INGRESOS FIJOS", "Sueldo y entradas que se repiten cada periodo.", "INGRESOS_FIJOS"),
            Triple("INGRESOS EXTRA", "Ventas, freelance y entradas eventuales.", "INGRESOS_EXTRA"),
            Triple("CUENTAS POR PAGAR", "Deudas y saldos pendientes con abonos.", "CUENTAS_POR_PAGAR")
        )

        opciones.forEach { (titulo, desc, id) ->
            NeoBrutalCard(theme = theme, modifier = Modifier.clickable { onCambiarSubSeccion(id) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconBox3D(
                            icon = when (id) {
                                "PAGOS_FIJOS" -> Icons.Default.Receipt
                                "INGRESOS_FIJOS" -> Icons.Default.Work
                                "INGRESOS_EXTRA" -> Icons.Default.TrendingUp
                                else -> Icons.Default.SwapHoriz
                            },
                            size = 40.dp,
                            iconSize = 22.dp,
                            theme = theme
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = titulo, fontSize = 14.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = desc, fontSize = 11.sp, color = theme.textMuted)
                        }
                    }
                    Text(text = ">", fontSize = 18.sp, fontWeight = FontWeight.Black, color = theme.primaryRed)
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeoBrutalButton(containerColor = theme.headerBg, contentColor = Color.White, theme = theme, onClick = { onCambiarSubSeccion("MENU") }) {
                Text(text = "< VOLVER", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            NeoBrutalButton(containerColor = theme.primaryRed, contentColor = Color.White, theme = theme, onClick = {
                when (subSeccion) {
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
            "PAGOS_FIJOS" -> ListaMovimientosSub(viewModel.movimientos.filter { it.tipo == TipoMovimiento.GASTO && it.esFijo }, "SIN PAGOS FIJOS", "Registra tus pagos recurrentes como renta o servicios.", theme, onSeleccionarMovimiento)
            "INGRESOS_FIJOS" -> ListaMovimientosSub(viewModel.movimientos.filter { it.tipo == TipoMovimiento.INGRESO && it.esFijo }, "SIN INGRESOS FIJOS", "Registra entradas recurrentes como salario o rentas.", theme, onSeleccionarMovimiento)
            "INGRESOS_EXTRA" -> ListaMovimientosSub(viewModel.movimientos.filter { it.tipo == TipoMovimiento.INGRESO && !it.esFijo }, "SIN INGRESOS EXTRA", "Anota ingresos puntuales fuera de tu flujo fijo.", theme, onSeleccionarMovimiento)
            "CUENTAS_POR_PAGAR" -> {
                if (viewModel.cuentasPorPagar.isEmpty()) {
                    DashedContainer(theme = theme) {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            IconBox3D(icon = Icons.Default.ReceiptLong, size = 54.dp, iconSize = 32.dp, theme = theme)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "SIN CUENTAS POR PAGAR", fontSize = 16.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Registra tus deudas y controla cada abono hasta liquidarlas.", fontSize = 12.sp, color = theme.textMuted, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        viewModel.cuentasPorPagar.forEach { deuda ->
                            NeoBrutalCard(theme = theme) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(text = deuda.titulo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
                                            Text(text = "Vence: ${deuda.fechaVencimiento}", fontSize = 10.sp, color = theme.textMuted)
                                        }
                                        Text(text = "$${String.format("%.2f", deuda.saldoPendiente)} / $${String.format("%.0f", deuda.montoTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = theme.primaryRed)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        NeoBrutalButton(containerColor = theme.headerBg, contentColor = Color.White, theme = theme, onClick = { onAbonarDeuda(deuda) }) {
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
fun ListaMovimientosSub(lista: List<Movimiento>, tituloVacio: String, descVacio: String, theme: AppThemeColors, onSeleccionar: (Movimiento) -> Unit) {
    if (lista.isEmpty()) {
        DashedContainer(theme = theme) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                IconBox3D(icon = Icons.Default.Paid, size = 54.dp, iconSize = 32.dp, theme = theme)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = tituloVacio, fontSize = 16.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = descVacio, fontSize = 12.sp, color = theme.textMuted, textAlign = TextAlign.Center)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            lista.forEach { mov -> ItemMovimientoCard(movimiento = mov, theme = theme, onClick = { onSeleccionar(mov) }) }
        }
    }
}

// --- PANTALLA BALANCES CON NAVEGADOR DE PERIODO ACTIVO ---
@Composable
fun PantallaBalancesContent(
    viewModel: MainViewModel,
    theme: AppThemeColors,
    mesActual: String,
    onAnteriorMes: () -> Unit,
    onSiguienteMes: () -> Unit,
    onAbrirTransferencia: () -> Unit
) {
    var modoBalances by remember { mutableStateOf("MENSUAL") }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.weight(1f).background(if (modoBalances == "SEMANAL") theme.primaryRed else theme.cardBg).border(2.dp, theme.borderBlack).clickable { modoBalances = "SEMANAL" }.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "SEMANAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (modoBalances == "SEMANAL") Color.White else theme.textDark)
        }
        Box(
            modifier = Modifier.weight(1f).background(if (modoBalances == "MENSUAL") theme.primaryRed else theme.cardBg).border(2.dp, theme.borderBlack).clickable { modoBalances = "MENSUAL" }.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "MENSUAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (modoBalances == "MENSUAL") Color.White else theme.textDark)
        }
    }

    NeoBrutalCard(theme = theme) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.background(theme.cardBg).border(1.5.dp, theme.borderBlack).clickable { onAnteriorMes() }.padding(8.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = theme.textDark, modifier = Modifier.size(16.dp))
            }
            Text(text = if (modoBalances == "SEMANAL") "SEM ACTUAL · $mesActual" else mesActual, fontSize = 13.sp, fontWeight = FontWeight.Black, color = theme.textDark)
            Box(modifier = Modifier.background(theme.cardBg).border(1.5.dp, theme.borderBlack).clickable { onSiguienteMes() }.padding(8.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = theme.textDark, modifier = Modifier.size(16.dp))
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricMiniCard(modifier = Modifier.weight(1f), title = "INGRESOS", value = "$${String.format("%.2f", viewModel.totalIngresosMes)}", icon = Icons.Default.TrendingUp, iconBg = Color(0xFF2E7D32), iconTint = Color.White, theme = theme)
        MetricMiniCard(modifier = Modifier.weight(1f), title = "GASTOS", value = "$${String.format("%.2f", viewModel.totalGastosMes)}", icon = Icons.Default.TrendingDown, iconBg = theme.primaryRed, iconTint = Color.White, theme = theme)
        MetricMiniCard(modifier = Modifier.weight(1f), title = "NETO", value = "$${String.format("%.2f", viewModel.balanceNeto)}", icon = Icons.Default.AccountBalance, iconBg = theme.headerBg, iconTint = Color.White, theme = theme)
    }

    GraficoIngresosVsGastosCard(ingresos = viewModel.totalIngresosMes, gastos = viewModel.totalGastosMes, theme = theme)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "DESGLOSE POR CATEGORÍA", fontSize = 12.sp, fontWeight = FontWeight.Black, color = theme.textDark)
        Box(modifier = Modifier.border(1.5.dp, theme.borderBlack).background(theme.cardBg).padding(horizontal = 8.dp, vertical = 2.dp)) {
            Text(text = "2 CAT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
        }
    }

    val deudasTotales = viewModel.totalDeudasPendientes
    val pagosFijosTotales = viewModel.movimientos.filter { it.esFijo && it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto }

    TarjetaCategoriaBarra(titulo = "CUENTAS POR PAGAR", monto = deudasTotales, porcentaje = if (deudasTotales > 0) 100 else 0, theme = theme)
    TarjetaCategoriaBarra(titulo = "PAGOS FIJOS", monto = pagosFijosTotales, porcentaje = if (pagosFijosTotales > 0) 100 else 0, theme = theme)

    GraficoTendencia6MesesCard(theme = theme)
}

@Composable
fun GraficoIngresosVsGastosCard(ingresos: Double, gastos: Double, theme: AppThemeColors) {
    val maximo = maxOf(ingresos, gastos, 1.0)
    val pctIngresos = (ingresos / maximo).toFloat().coerceIn(0.05f, 1f)
    val pctGastos = (gastos / maximo).toFloat().coerceIn(0.05f, 1f)

    NeoBrutalCard(theme = theme) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = theme.textDark, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "INGRESOS VS GASTOS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = theme.textDark)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(130.dp).border(2.dp, theme.borderBlack).background(theme.screenBg).padding(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.Bottom) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                        Text(text = "$${String.format("%.2f", ingresos)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.width(40.dp).fillMaxHeight(fraction = pctIngresos).background(theme.borderBlack))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "INGRESOS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                        Text(text = "$${String.format("%.2f", gastos)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.width(40.dp).fillMaxHeight(fraction = pctGastos).background(theme.borderBlack))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "GASTOS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaCategoriaBarra(titulo: String, monto: Double, porcentaje: Int, theme: AppThemeColors) {
    NeoBrutalCard(theme = theme) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = titulo, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
                Text(text = "$${String.format("%.2f", monto)}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = theme.textDark)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).height(14.dp).background(Color(0xFF333333)).border(1.dp, theme.borderBlack)) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = (porcentaje / 100f).coerceIn(0f, 1f)).background(theme.primaryRed))
                }
                Text(text = "$porcentaje%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
            }
        }
    }
}

@Composable
fun GraficoTendencia6MesesCard(theme: AppThemeColors) {
    NeoBrutalCard(theme = theme) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "TENDENCIA 6 MESES", fontSize = 12.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFF2E7D32)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "ING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(theme.primaryRed))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "GAS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(110.dp).border(2.dp, theme.borderBlack).background(theme.screenBg).padding(10.dp)) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.Bottom) {
                        listOf("10", "09", "08", "07", "06", "05").forEach { mes ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.width(20.dp).height(4.dp).background(theme.borderBlack))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = mes, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaAhorrosContent(
    viewModel: MainViewModel,
    theme: AppThemeColors,
    onCrearMeta: () -> Unit,
    onAbonar: (MetaAhorro) -> Unit
) {
    val totalAhorrado = viewModel.metasAhorro.sumOf { it.montoActual }
    val completadas = viewModel.metasAhorro.count { it.montoObjetivo > 0 && it.montoActual >= it.montoObjetivo }
    val activas = viewModel.metasAhorro.count { it.montoObjetivo > 0 && it.montoActual < it.montoObjetivo }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricMiniCard(modifier = Modifier.weight(1f), title = "TOTAL AHORRADO", value = "$${String.format("%.2f", totalAhorrado)}", icon = Icons.Default.AccountBalanceWallet, iconBg = theme.primaryRed, iconTint = Color.White, theme = theme)
        MetricMiniCard(modifier = Modifier.weight(1f), title = "COMPLETADAS", value = "$completadas", icon = Icons.Default.Adjust, iconBg = Color(0xFF2E7D32), iconTint = Color.White, theme = theme)
        MetricMiniCard(modifier = Modifier.weight(1f), title = "ACTIVAS", value = "$activas", icon = Icons.Default.TrendingUp, iconBg = theme.iconBoxBg, iconTint = theme.textDark, theme = theme)
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "METAS DE AHORRO", fontSize = 13.sp, fontWeight = FontWeight.Black, color = theme.textDark)
        NeoBrutalButton(containerColor = theme.primaryRed, contentColor = Color.White, theme = theme, onClick = onCrearMeta) {
            Text(text = "+ NUEVA META", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (viewModel.metasAhorro.isEmpty()) {
        DashedContainer(theme = theme) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                IconBox3D(icon = Icons.Default.Savings, size = 54.dp, iconSize = 30.dp, theme = theme)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "SIN METAS TODAVÍA", fontSize = 16.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Crea tu primera meta de ahorro y observa cómo crece tu progreso.", fontSize = 12.sp, color = theme.textMuted, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                NeoBrutalButton(containerColor = theme.primaryRed, contentColor = Color.White, theme = theme, onClick = onCrearMeta) {
                    Text(text = "CREAR META", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            viewModel.metasAhorro.forEach { meta ->
                val porcentaje = if (meta.montoObjetivo > 0) (meta.montoActual / meta.montoObjetivo) * 100 else 0.0
                NeoBrutalCard(theme = theme) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(text = meta.titulo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
                                Text(text = "${String.format("%.1f", porcentaje)}% completado", fontSize = 10.sp, color = theme.textMuted)
                            }
                            Text(text = "$${String.format("%.2f", meta.montoActual)} / $${String.format("%.0f", meta.montoObjetivo)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Color(0xFFE0E0E0)).border(1.dp, theme.borderBlack)) {
                            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = (porcentaje / 100).toFloat().coerceIn(0f, 1f)).background(Color(0xFF2E7D32)))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            NeoBrutalButton(containerColor = theme.headerBg, contentColor = Color.White, theme = theme, onClick = { onAbonar(meta) }) {
                                Text(text = "+ ABONAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PANTALLA AJUSTES CON MENÚS DESPLEGABLES Y TEMA DINÁMICO ---
@Composable
fun PantallaAjustesContent(
    viewModel: MainViewModel,
    theme: AppThemeColors,
    monedaSeleccionada: String,
    onMonedaChange: (String) -> Unit,
    formatoFecha: String,
    onFormatoFechaChange: (String) -> Unit,
    esTemaOscuro: Boolean,
    onTemaChange: (Boolean) -> Unit
) {
    var presupuestoInput by remember {
        mutableStateOf(if (viewModel.presupuestoMensual > 0) String.format("%.2f", viewModel.presupuestoMensual) else "0.00")
    }
    var guardado by remember { mutableStateOf(false) }

    var expandedMoneda by remember { mutableStateOf(false) }
    var expandedFecha by remember { mutableStateOf(false) }

    val monedasList = listOf("MXN - PESO MEXICANO", "USD - DÓLAR ESTADOUNIDENSE", "EUR - EURO", "VES - BOLÍVAR")
    val fechasList = listOf("DD/MM/AAAA", "MM/DD/AAAA", "AAAA-MM-DD")

    // 1. Tarjeta Moneda y Fecha Interactiva
    NeoBrutalCard(theme = theme) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "MONEDA Y FECHA", fontSize = 11.sp, fontWeight = FontWeight.Black, color = theme.textDark)
            
            Text(text = "MONEDA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, theme.borderBlack)
                    .background(theme.cardBg)
                    .clickable { expandedMoneda = true }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = monedaSeleccionada, fontSize = 13.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = theme.textDark, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = expandedMoneda, onDismissRequest = { expandedMoneda = false }) {
                    monedasList.forEach { mon ->
                        DropdownMenuItem(
                            text = { Text(mon, fontWeight = FontWeight.Bold) },
                            onClick = {
                                onMonedaChange(mon)
                                expandedMoneda = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(text = "FORMATO DE FECHA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, theme.borderBlack)
                    .background(theme.cardBg)
                    .clickable { expandedFecha = true }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = formatoFecha, fontSize = 13.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = theme.textDark, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = expandedFecha, onDismissRequest = { expandedFecha = false }) {
                    fechasList.forEach { fec ->
                        DropdownMenuItem(
                            text = { Text(fec, fontWeight = FontWeight.Bold) },
                            onClick = {
                                onFormatoFechaChange(fec)
                                expandedFecha = false
                            }
                        )
                    }
                }
            }
        }
    }

    // 2. Tarjeta Presupuesto Mensual
    NeoBrutalCard(theme = theme) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "PRESUPUESTO MENSUAL", fontSize = 11.sp, fontWeight = FontWeight.Black, color = theme.textDark)
            Text(text = "LÍMITE DE GASTO DEL MES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
            
            OutlinedTextField(
                value = presupuestoInput,
                onValueChange = { presupuestoInput = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.borderBlack,
                    unfocusedBorderColor = theme.borderBlack,
                    focusedContainerColor = theme.cardBg,
                    unfocusedContainerColor = theme.cardBg,
                    focusedTextColor = theme.textDark,
                    unfocusedTextColor = theme.textDark
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(text = "ALIMENTA LA BARRA DE PROGRESO DEL PANEL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
        }
    }

    // 3. Tarjeta Tema de la Interfaz (Claro / Oscuro Real)
    NeoBrutalCard(theme = theme) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "TEMA DE LA INTERFAZ", fontSize = 11.sp, fontWeight = FontWeight.Black, color = theme.textDark)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (!esTemaOscuro) theme.primaryRed else theme.cardBg)
                        .border(2.dp, theme.borderBlack)
                        .clickable { onTemaChange(false) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = if (!esTemaOscuro) Color.White else theme.textDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "CLARO", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (!esTemaOscuro) Color.White else theme.textDark)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (esTemaOscuro) theme.headerBg else theme.cardBg)
                        .border(2.dp, theme.borderBlack)
                        .clickable { onTemaChange(true) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.NightsStay, contentDescription = null, tint = if (esTemaOscuro) Color.White else theme.textDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "OSCURO", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (esTemaOscuro) Color.White else theme.textDark)
                    }
                }
            }
            Text(text = "CONSERVA LA PALETA BRUTALISTA EN AMBOS MODOS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
        }
    }

    // 4. Botón Guardar Ajustes
    NeoBrutalButton(
        modifier = Modifier.fillMaxWidth(),
        containerColor = theme.primaryRed,
        contentColor = Color.White,
        theme = theme,
        onClick = {
            viewModel.actualizarPresupuesto(presupuestoInput.toDoubleOrNull() ?: 0.0)
            guardado = true
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "GUARDAR AJUSTES", fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }

    if (guardado) {
        Text(text = "¡Ajustes guardados correctamente!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

// --- DIÁLOGOS Y COMPONENTES AUXILIARES ---
@Composable
fun FormularioCrearDeudaDialog(theme: AppThemeColors, onDismiss: () -> Unit, onGuardar: (String, Double, String) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var montoText by remember { mutableStateOf("") }
    var vencimiento by remember { mutableStateOf("31 OCT 2026") }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(theme.borderBlack))
            Column(
                modifier = Modifier.fillMaxWidth().background(theme.screenBg).border(3.dp, theme.borderBlack).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "REGISTRAR CUENTA POR PAGAR", fontSize = 16.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título / Deuda") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.borderBlack, unfocusedBorderColor = theme.borderBlack, focusedContainerColor = theme.cardBg, unfocusedContainerColor = theme.cardBg),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = montoText,
                    onValueChange = { montoText = it },
                    label = { Text("Monto Total ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.borderBlack, unfocusedBorderColor = theme.borderBlack, focusedContainerColor = theme.cardBg, unfocusedContainerColor = theme.cardBg),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = vencimiento,
                    onValueChange = { vencimiento = it },
                    label = { Text("Fecha de Vencimiento") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.borderBlack, unfocusedBorderColor = theme.borderBlack, focusedContainerColor = theme.cardBg, unfocusedContainerColor = theme.cardBg),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = theme.cardBg, contentColor = theme.textDark, theme = theme, onClick = onDismiss) {
                        Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = theme.primaryRed, contentColor = Color.White, theme = theme, onClick = {
                        val m = montoText.toDoubleOrNull() ?: 0.0
                        if (m > 0) onGuardar(titulo, m, vencimiento)
                    }) {
                        Text("GUARDAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FormularioAbonarDeudaDialog(deuda: CuentaPorPagar, cuentas: List<Cuenta>, theme: AppThemeColors, onDismiss: () -> Unit, onAbonar: (Double, String) -> Unit) {
    var abonoText by remember { mutableStateOf("") }
    var cuentaIdSeleccionada by remember { mutableStateOf(cuentas.firstOrNull()?.id ?: "1") }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(theme.borderBlack))
            Column(
                modifier = Modifier.fillMaxWidth().background(theme.screenBg).border(3.dp, theme.borderBlack).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "ABONAR A: ${deuda.titulo}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                OutlinedTextField(
                    value = abonoText,
                    onValueChange = { abonoText = it },
                    label = { Text("Monto del Abono ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.borderBlack, unfocusedBorderColor = theme.borderBlack, focusedContainerColor = theme.cardBg, unfocusedContainerColor = theme.cardBg),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(text = "ORIGEN DEL DINERO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cuentas.forEach { c ->
                        val sel = c.id == cuentaIdSeleccionada
                        Box(
                            modifier = Modifier.weight(1f).background(if (sel) theme.primaryRed else theme.cardBg).border(1.5.dp, theme.borderBlack).clickable { cuentaIdSeleccionada = c.id }.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(c.nombre, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else theme.textDark)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = theme.cardBg, contentColor = theme.textDark, theme = theme, onClick = onDismiss) {
                        Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = theme.primaryRed, contentColor = Color.White, theme = theme, onClick = {
                        val m = abonoText.toDoubleOrNull() ?: 0.0
                        if (m > 0) onAbonar(m, cuentaIdSeleccionada)
                    }) {
                        Text("ABONAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FormularioMovimientoDialog(tipo: TipoMovimiento, esFijoInicial: Boolean, cuentas: List<Cuenta>, theme: AppThemeColors, onDismiss: () -> Unit, onGuardar: (String, Double, String, Boolean) -> Unit) {
    var concepto by remember { mutableStateOf("") }
    var montoText by remember { mutableStateOf("") }
    var cuentaIdSeleccionada by remember { mutableStateOf(cuentas.firstOrNull()?.id ?: "1") }
    var esFijo by remember { mutableStateOf(esFijoInicial) }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(theme.borderBlack))
            Column(
                modifier = Modifier.fillMaxWidth().background(theme.screenBg).border(3.dp, theme.borderBlack).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = if (tipo == TipoMovimiento.INGRESO) "REGISTRAR INGRESO" else "REGISTRAR GASTO", fontSize = 18.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text("Concepto / Descripción") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.borderBlack, unfocusedBorderColor = theme.borderBlack, focusedContainerColor = theme.cardBg, unfocusedContainerColor = theme.cardBg),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = montoText,
                    onValueChange = { montoText = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.borderBlack, unfocusedBorderColor = theme.borderBlack, focusedContainerColor = theme.cardBg, unfocusedContainerColor = theme.cardBg),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(text = "CUENTA:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    cuentas.forEach { c ->
                        val sel = c.id == cuentaIdSeleccionada
                        Box(
                            modifier = Modifier.weight(1f).background(if (sel) theme.headerBg else theme.cardBg).border(1.5.dp, theme.borderBlack).clickable { cuentaIdSeleccionada = c.id }.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(c.nombre, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else theme.textDark)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = theme.cardBg, contentColor = theme.textDark, theme = theme, onClick = onDismiss) {
                        Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = theme.primaryRed, contentColor = Color.White, theme = theme, onClick = {
                        val m = montoText.toDoubleOrNull() ?: 0.0
                        if (m > 0) {
                            onGuardar(concepto, m, cuentaIdSeleccionada, esFijo)
                            onDismiss()
                        }
                    }) {
                        Text("GUARDAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FormularioTransferenciaDialog(cuentas: List<Cuenta>, theme: AppThemeColors, onDismiss: () -> Unit, onTransferir: (String, String, Double) -> Unit) {
    var origenId by remember { mutableStateOf(cuentas.firstOrNull()?.id ?: "1") }
    var destinoId by remember { mutableStateOf(cuentas.getOrNull(1)?.id ?: "2") }
    var montoText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(theme.borderBlack))
            Column(
                modifier = Modifier.fillMaxWidth().background(theme.screenBg).border(3.dp, theme.borderBlack).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "TRANSFERIR FONDOS", fontSize = 18.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                OutlinedTextField(
                    value = montoText,
                    onValueChange = { montoText = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.borderBlack, unfocusedBorderColor = theme.borderBlack, focusedContainerColor = theme.cardBg, unfocusedContainerColor = theme.cardBg),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = theme.cardBg, contentColor = theme.textDark, theme = theme, onClick = onDismiss) {
                        Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = theme.primaryRed, contentColor = Color.White, theme = theme, onClick = {
                        val m = montoText.toDoubleOrNull() ?: 0.0
                        if (m > 0) {
                            onTransferir(origenId, destinoId, m)
                            onDismiss()
                        }
                    }) {
                        Text("TRANSFERIR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FormularioCrearMetaDialog(theme: AppThemeColors, onDismiss: () -> Unit, onGuardar: (String, Double) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var montoText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(theme.borderBlack))
            Column(
                modifier = Modifier.fillMaxWidth().background(theme.screenBg).border(3.dp, theme.borderBlack).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "NUEVA META DE AHORRO", fontSize = 18.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título / Objetivo") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.borderBlack, unfocusedBorderColor = theme.borderBlack, focusedContainerColor = theme.cardBg, unfocusedContainerColor = theme.cardBg),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = montoText,
                    onValueChange = { montoText = it },
                    label = { Text("Monto Objetivo ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.borderBlack, unfocusedBorderColor = theme.borderBlack, focusedContainerColor = theme.cardBg, unfocusedContainerColor = theme.cardBg),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = theme.cardBg, contentColor = theme.textDark, theme = theme, onClick = onDismiss) {
                        Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = theme.primaryRed, contentColor = Color.White, theme = theme, onClick = {
                        val m = montoText.toDoubleOrNull() ?: 0.0
                        if (m > 0) onGuardar(titulo, m)
                    }) {
                        Text("CREAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FormularioAbonarMetaDialog(meta: MetaAhorro, cuentas: List<Cuenta>, theme: AppThemeColors, onDismiss: () -> Unit, onAbonar: (Double, String) -> Unit) {
    var montoText by remember { mutableStateOf("") }
    var cuentaIdSeleccionada by remember { mutableStateOf(cuentas.firstOrNull()?.id ?: "1") }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(theme.borderBlack))
            Column(
                modifier = Modifier.fillMaxWidth().background(theme.screenBg).border(3.dp, theme.borderBlack).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "ABONAR A: ${meta.titulo}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                OutlinedTextField(
                    value = montoText,
                    onValueChange = { montoText = it },
                    label = { Text("Monto del Abono ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = theme.borderBlack, unfocusedBorderColor = theme.borderBlack, focusedContainerColor = theme.cardBg, unfocusedContainerColor = theme.cardBg),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = theme.cardBg, contentColor = theme.textDark, theme = theme, onClick = onDismiss) {
                        Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    NeoBrutalButton(modifier = Modifier.weight(1f), containerColor = theme.primaryRed, contentColor = Color.White, theme = theme, onClick = {
                        val m = montoText.toDoubleOrNull() ?: 0.0
                        if (m > 0) onAbonar(m, cuentaIdSeleccionada)
                    }) {
                        Text("ABONAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OpcionesMovimientoDialog(movimiento: Movimiento, theme: AppThemeColors, onDismiss: () -> Unit, onEliminar: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box {
            Box(modifier = Modifier.matchParentSize().offset(x = 6.dp, y = 6.dp).background(theme.borderBlack))
            Column(
                modifier = Modifier.fillMaxWidth().background(theme.screenBg).border(3.dp, theme.borderBlack).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "ELIMINAR REGISTRO", fontSize = 16.sp, fontWeight = FontWeight.Black, color = theme.textDark)
                Text(text = "${movimiento.concepto} - $${String.format("%.2f", movimiento.monto)}", fontSize = 13.sp, color = theme.textMuted)
                NeoBrutalButton(containerColor = theme.primaryRed, contentColor = Color.White, theme = theme, onClick = onEliminar) {
                    Text("ELIMINAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                NeoBrutalButton(containerColor = theme.cardBg, contentColor = theme.textDark, theme = theme, onClick = onDismiss) {
                    Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ItemMovimientoCard(movimiento: Movimiento, theme: AppThemeColors, onClick: () -> Unit = {}) {
    val esIngreso = movimiento.tipo == TipoMovimiento.INGRESO
    val signo = if (esIngreso) "+" else "-"
    val colorMonto = if (esIngreso) Color(0xFF2E7D32) else theme.primaryRed

    NeoBrutalCard(theme = theme, modifier = Modifier.clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBox3D(
                    icon = if (esIngreso) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    size = 36.dp,
                    iconSize = 20.dp,
                    bgColor = if (esIngreso) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    tint = colorMonto,
                    theme = theme
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = movimiento.concepto, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textDark)
                    Text(text = movimiento.fecha, fontSize = 10.sp, color = theme.textMuted)
                }
            }
            Text(text = "$signo$${String.format("%.2f", movimiento.monto)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = colorMonto)
        }
    }
}

@Composable
fun NeoBrutalCard(theme: AppThemeColors, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier) {
        Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).background(theme.borderBlack))
        Box(modifier = Modifier.fillMaxWidth().background(theme.cardBg).border(2.dp, theme.borderBlack)) {
            content()
        }
    }
}

@Composable
fun NeoBrutalButton(
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    theme: AppThemeColors,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.clickable { onClick() }) {
        Box(modifier = Modifier.matchParentSize().offset(x = 3.dp, y = 3.dp).background(theme.borderBlack))
        Box(
            modifier = Modifier.fillMaxWidth().background(containerColor).border(2.dp, theme.borderBlack).padding(vertical = 10.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}

@Composable
fun IconBox3D(
    icon: ImageVector,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp,
    bgColor: Color = Color(0xFFB5B5B5),
    tint: Color = Color(0xFF1A1A1A),
    theme: AppThemeColors
) {
    Box(
        modifier = Modifier.size(size).background(bgColor).border(2.dp, theme.borderBlack),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun MetricMiniCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    theme: AppThemeColors
) {
    NeoBrutalCard(theme = theme, modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
            IconBox3D(icon = icon, size = 32.dp, iconSize = 18.dp, bgColor = iconBg, iconTint = iconTint, theme = theme)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.textMuted)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = theme.textDark)
        }
    }
}

@Composable
fun DashedContainer(theme: AppThemeColors, content: @Composable () -> Unit) {
    val stroke = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                drawRect(color = theme.borderBlack, style = stroke)
            }
            .background(theme.screenBg)
    ) {
        content()
    }
}

@Composable
fun BottomNavigationBar(seccionActual: String, theme: AppThemeColors, onSeccionSelected: (String) -> Unit) {
    val items = listOf(
        Triple("INICIO", Icons.Default.AccountBalanceWallet, "INICIO"),
        Triple("MOVIMIENTOS", Icons.Default.SwapHoriz, "MOVIMIENTOS"),
        Triple("BALANCES", Icons.Default.Scale, "BALANCES"),
        Triple("AHORROS", Icons.Default.Savings, "AHORROS"),
        Triple("AJUSTES", Icons.Default.Settings, "AJUSTES")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(theme.borderBlack))
        Row(
            modifier = Modifier.fillMaxWidth().background(theme.screenBg).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (id, icon, label) ->
                val seleccionado = seccionActual == id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).clickable { onSeccionSelected(id) }.padding(horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .then(if (seleccionado) Modifier.background(theme.cardBg).border(2.dp, theme.borderBlack) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (seleccionado) theme.primaryRed else theme.textDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (seleccionado) theme.primaryRed else theme.textDark
                    )
                }
            }
        }
    }
}
