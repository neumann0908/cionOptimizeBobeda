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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.monse.coinoptimize.data.Cuenta
import com.monse.coinoptimize.data.Movimiento
import com.monse.coinoptimize.data.TipoMovimiento

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val viewModel = remember { MainViewModel() }
                CajaFuerteMainScreen(viewModel = viewModel)
            }
        }
    }
}

// --- PALETA DE COLORES NEO-BRUTALISTA ---
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
    
    // ESTADOS PARA EL FORMULARIO
    var mostrarFormulario by remember { mutableStateOf(false) }
    var tipoMovimientoForm by remember { mutableStateOf(TipoMovimiento.INGRESO) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ColorScreenBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. HEADER SUPERIOR OSCURO
            TopHeaderBar(seccionActual = seccionSeleccionada)

            // 2. CONTENIDO SCROLLABLE
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
                        onAbrirFormulario = { tipo ->
                            tipoMovimientoForm = tipo
                            mostrarFormulario = true
                        }
                    )
                    "MOVIMIENTOS" -> PantallaMovimientosContent(
                        viewModel = viewModel,
                        onAbrirFormulario = { tipo ->
                            tipoMovimientoForm = tipo
                            mostrarFormulario = true
                        }
                    )
                    else -> PantallaProximamenteContent(seccionSeleccionada)
                }
            }

            // 3. BARRA DE NAVEGACIÓN INFERIOR
            BottomNavigationBar(
                seccionActual = seccionSeleccionada,
                onSeccionSelected = { seccionSeleccionada = it }
            )
        }

        // DIÁLOGO / FORMULARIO INTERACTIVO
        if (mostrarFormulario) {
            FormularioMovimientoDialog(
                tipo = tipoMovimientoForm,
                cuentas = viewModel.cuentas,
                onDismiss = { mostrarFormulario = false },
                onGuardar = { concepto, monto, cuentaId ->
                    viewModel.agregarMovimiento(concepto, monto, tipoMovimientoForm, cuentaId)
                }
            )
        }
    }
}

// --- HEADER SUPERIOR ---
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
                modifier = Modifier
                    .size(36.dp)
                    .background(ColorPrimaryRed)
                    .border(1.5.dp, ColorBorderBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "CAJA FUERTE",
                    color = ColorPrimaryRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = seccionActual,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(ColorPrimaryRed)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "OCT 2026",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryRed),
                shape = RoundedCornerShape(0.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Salir",
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

// --- CONTENIDO PANTALLA INICIO ---
@Composable
fun PantallaInicioContent(
    viewModel: MainViewModel,
    onAbrirFormulario: (TipoMovimiento) -> Unit
) {
    NeoBrutalCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "BALANCE NETO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextDark,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${String.format("%.2f", viewModel.balanceNeto)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = ColorTextDark
                )
            }
            IconBox3D(icon = Icons.Default.Balance)
        }
    }

    NeoBrutalCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRESUPUESTO MENSUAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextDark
                )
                Box(
                    modifier = Modifier
                        .border(1.5.dp, ColorBorderBlack)
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SIN LÍMITE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(Color(0xFF333333))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "GASTADO $${String.format("%.2f", viewModel.totalGastosMes)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextMuted
                )
                Text(
                    text = "CONFIGURA EN AJUSTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextDark
                )
            }
        }
    }

    // Botones de Acción Interáctivos
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NeoBrutalButton(
            modifier = Modifier.weight(1f),
            containerColor = ColorPrimaryRed,
            contentColor = Color.White,
            onClick = { onAbrirFormulario(TipoMovimiento.INGRESO) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "INGRESO\nEXTRA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 13.sp
                )
            }
        }

        NeoBrutalButton(
            modifier = Modifier.weight(1f),
            containerColor = Color.White,
            contentColor = ColorTextDark,
            onClick = { onAbrirFormulario(TipoMovimiento.GASTO) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconBox3D(
                    icon = Icons.Default.AttachMoney,
                    size = 28.dp,
                    iconSize = 16.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "REGISTRAR\nPAGO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 13.sp
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PRÓXIMOS VENCIMIENTOS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextDark
        )
        Text(
            text = "VER TODO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ColorPrimaryRed,
            modifier = Modifier.clickable { }
        )
    }

    DashedContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconBox3D(
                icon = Icons.Default.DateRange,
                size = 54.dp,
                iconSize = 32.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SIN VENCIMIENTOS",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = ColorTextDark,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No tienes pagos fijos ni cuentas por pagar próximas este mes.",
                fontSize = 12.sp,
                color = ColorTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- CONTENIDO PANTALLA MOVIMIENTOS ---
@Composable
fun PantallaMovimientosContent(
    viewModel: MainViewModel,
    onAbrirFormulario: (TipoMovimiento) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricMiniCard(
            modifier = Modifier.weight(1f),
            title = "TOTAL DEL MES",
            value = "$${String.format("%.2f", viewModel.totalIngresosMes)}",
            icon = Icons.Default.TrendingUp,
            iconBg = ColorPrimaryRed,
            iconTint = Color.White
        )
        MetricMiniCard(
            modifier = Modifier.weight(1f),
            title = "REGISTROS",
            value = "${viewModel.cantidadRegistros}",
            icon = Icons.Default.Tag,
            iconBg = ColorGreyIconBox,
            iconTint = ColorTextDark
        )
        MetricMiniCard(
            modifier = Modifier.weight(1f),
            title = "PERIODO",
            value = "OCT 2026",
            icon = Icons.Default.CalendarToday,
            iconBg = Color(0xFF2E7D32),
            iconTint = Color.White
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "HISTORIAL",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = ColorTextDark
        )
        NeoBrutalButton(
            containerColor = ColorPrimaryRed,
            contentColor = Color.White,
            onClick = { onAbrirFormulario(TipoMovimiento.INGRESO) }
        ) {
            Text("+ NUEVO INGRESO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (viewModel.movimientos.isEmpty()) {
        DashedContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconBox3D(
                    icon = Icons.Default.Paid,
                    size = 54.dp,
                    iconSize = 32.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SIN INGRESOS EXTRA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = ColorTextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Anota ingresos puntuales fuera de tu flujo fijo y míralos sumar al balance del mes.",
                    fontSize = 12.sp,
                    color = ColorTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            viewModel.movimientos.forEach { movimiento ->
                ItemMovimientoCard(movimiento = movimiento)
            }
        }
    }
}

// --- FORMULARIO EMERGENTE NEO-BRUTALISTA ---
@Composable
fun FormularioMovimientoDialog(
    tipo: TipoMovimiento,
    cuentas: List<Cuenta>,
    onDismiss: () -> Unit,
    onGuardar: (concepto: String, monto: Double, cuentaId: String) -> Unit
) {
    var concepto by remember { mutableStateOf("") }
    var montoText by remember { mutableStateOf("") }
    var cuentaIdSeleccionada by remember { mutableStateOf(cuentas.firstOrNull()?.id ?: "1") }

    val esIngreso = tipo == TipoMovimiento.INGRESO
    val tituloHeader = if (esIngreso) "NUEVO INGRESO" else "REGISTRAR PAGO"

    Dialog(onDismissRequest = onDismiss) {
        Box {
            // Sombra desplazada
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(ColorBorderBlack)
            )
            // Contenedor principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorScreenBg)
                    .border(3.dp, ColorBorderBlack)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = tituloHeader,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = ColorTextDark,
                    letterSpacing = 0.5.sp
                )

                // Campo Concepto
                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text("Concepto / Descripción") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorBorderBlack,
                        unfocusedBorderColor = ColorBorderBlack,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo Monto
                OutlinedTextField(
                    value = montoText,
                    onValueChange = { montoText = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorBorderBlack,
                        unfocusedBorderColor = ColorBorderBlack,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Cuenta
                Text(
                    text = "SELECCIONA CUENTA:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextDark
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    cuentas.forEach { cuenta ->
                        val seleccionada = cuenta.id == cuentaIdSeleccionada
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (seleccionada) ColorPrimaryRed else Color.White)
                                .border(2.dp, ColorBorderBlack)
                                .clickable { cuentaIdSeleccionada = cuenta.id }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cuenta.nombre,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (seleccionada) Color.White else ColorTextDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Botones Cancelar / Guardar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NeoBrutalButton(
                        modifier = Modifier.weight(1f),
                        containerColor = Color.White,
                        contentColor = ColorTextDark,
                        onClick = onDismiss
                    ) {
                        Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    NeoBrutalButton(
                        modifier = Modifier.weight(1f),
                        containerColor = ColorPrimaryRed,
                        contentColor = Color.White,
                        onClick = {
                            val montoParsed = montoText.toDoubleOrNull() ?: 0.0
                            if (montoParsed > 0) {
                                onGuardar(concepto, montoParsed, cuentaIdSeleccionada)
                                onDismiss()
                            }
                        }
                    ) {
                        Text("GUARDAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- TARJETA PARA CADA MOVIMIENTO EN EL HISTORIAL ---
@Composable
fun ItemMovimientoCard(movimiento: Movimiento) {
    val esIngreso = movimiento.tipo == TipoMovimiento.INGRESO
    val signo = if (esIngreso) "+" else "-"
    val colorMonto = if (esIngreso) Color(0xFF2E7D32) else ColorPrimaryRed

    NeoBrutalCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBox3D(
                    icon = if (esIngreso) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    size = 36.dp,
                    iconSize = 20.dp,
                    bgColor = if (esIngreso) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    tint = colorMonto
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = movimiento.concepto,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextDark
                    )
                    Text(
                        text = movimiento.fecha,
                        fontSize = 10.sp,
                        color = ColorTextMuted
                    )
                }
            }
            Text(
                text = "$signo$${String.format("%.2f", movimiento.monto)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = colorMonto
            )
        }
    }
}

@Composable
fun PantallaProximamenteContent(seccion: String) {
    DashedContainer {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SECCIÓN $seccion\nEN DESARROLLO",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = ColorTextDark,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- COMPONENTES NEO-BRUTALISTAS ---
@Composable
fun NeoBrutalCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(ColorBorderBlack)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(2.dp, ColorBorderBlack)
        ) {
            content()
        }
    }
}

@Composable
fun NeoBrutalButton(
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 3.dp)
                .background(ColorBorderBlack)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor)
                .border(2.dp, ColorBorderBlack)
                .padding(vertical = 10.dp, horizontal = 12.dp),
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
    bgColor: Color = ColorGreyIconBox,
    tint: Color = ColorTextDark
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(bgColor)
            .border(2.dp, ColorBorderBlack),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun MetricMiniCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    NeoBrutalCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            IconBox3D(icon = icon, size = 32.dp, iconSize = 18.dp, bgColor = iconBg, tint = iconTint)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextMuted
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = ColorTextDark
            )
        }
    }
}

@Composable
fun DashedContainer(
    content: @Composable () -> Unit
) {
    val stroke = Stroke(
        width = 4f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                drawRect(
                    color = ColorBorderBlack,
                    style = stroke
                )
            }
            .background(ColorScreenBg)
    ) {
        content()
    }
}

// --- BARRA DE NAVEGACIÓN INFERIOR ---
@Composable
fun BottomNavigationBar(
    seccionActual: String,
    onSeccionSelected: (String) -> Unit
) {
    val items = listOf(
        Triple("INICIO", Icons.Default.AccountBalanceWallet, "INICIO"),
        Triple("MOVIMIENTOS", Icons.Default.SwapHoriz, "MOVIMIENTOS"),
        Triple("BALANCES", Icons.Default.Scale, "BALANCES"),
        Triple("AHORROS", Icons.Default.Savings, "AHORROS"),
        Triple("AJUSTES", Icons.Default.Settings, "AJUSTES")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(ColorBorderBlack)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorScreenBg)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (id, icon, label) ->
                val seleccionado = seccionActual == id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSeccionSelected(id) }
                        .padding(horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .then(
                                if (seleccionado) {
                                    Modifier
                                        .background(Color.White)
                                        .border(2.dp, ColorBorderBlack)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (seleccionado) ColorPrimaryRed else ColorTextDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (seleccionado) ColorPrimaryRed else ColorTextDark
                    )
                }
            }
        }
    }
}
