package com.monse.coinoptimize

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoinOptimizeApp()
        }
    }
}

// Colores basados en el diseño retro / neo-brutalista de "Caja Fuerte"
val BackgroundCream = Color(0xFFF5F0E6)
val PrimaryRed = Color(0xFFBD2A2A)
val CardBorderBlack = Color(0xFF111111)
val TextDark = Color(0xFF1A1A1A)

@Composable
fun CoinOptimizeApp() {
    // Estado simple para controlar la sección actual según nuestro esqueleto de navegación
    var seccionActual by remember { mutableStateOf("INICIO") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabecera Estilo Caja Fuerte
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, CardBorderBlack)
                    .background(PrimaryRed)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CAJA FUERTE · $seccionActual",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "OCT 2026",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Contenido Central de la Pantalla
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
                    .border(2.dp, CardBorderBlack)
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BALANCE NETO",
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$0.00",
                        color = PrimaryRed,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp
                    )
                }
            }

            // Barra de Navegación Inferior Simple
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, CardBorderBlack)
                    .background(Color.White)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BotonNavegacion("INICIO", seccionActual == "INICIO") { seccionActual = "INICIO" }
                BotonNavegacion("MOVIMIENTOS", seccionActual == "MOVIMIENTOS") { seccionActual = "MOVIMIENTOS" }
                BotonNavegacion("BALANCES", seccionActual == "BALANCES") { seccionActual = "BALANCES" }
            }
        }
    }
}

@Composable
fun BotonNavegacion(titulo: String, seleccionado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (seleccionado) PrimaryRed else Color.LightGray
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp), // Estilo retro cuadrado
        modifier = Modifier.border(1.dp, CardBorderBlack)
    ) {
        Text(
            text = titulo,
            color = if (seleccionado) Color.White else TextDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
