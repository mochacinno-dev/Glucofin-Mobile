package com.mochi.glucofin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mochi.glucofin.ui.theme.GlucofinTheme
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

// la puerta principal de la app, donde todo empieza
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // haciendo que la pantalla ocupe todo el espacio y las letras se vean "fancy"
        enableEdgeToEdge()
        setContent {
            // poniéndole el traje bonito de glucofin
            GlucofinTheme {
                GlucofinApp()
            }
        }
    }
}

// la casa grande que tiene todos los cuartos (pestañas)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlucofinApp(viewModel: GlucofinViewModel = viewModel()) {
    // recordando en que cuarto estamos parados
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Perfil", "Laboratorio", "Historial", "Reporte")
    // dibujitos para los botones de abajo
    val icons = listOf(Icons.Default.Person, Icons.Default.Science, Icons.Default.History, Icons.Default.Analytics)

    Scaffold(
        // el título de arriba que se cree muy importante
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("GLUCOFIN", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        // la barrita de abajo para saltar de un cuarto a otro
        bottomBar = {
            NavigationBar(tonalElevation = 8.dp) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        // un efecto de desvanecido para que no sea tan brusco el cambio
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Crossfade(targetState = selectedTab, label = "tabFade") { tab ->
                when (tab) {
                    0 -> PersonalDataScreen(viewModel)
                    1 -> LabDataScreen(viewModel)
                    2 -> HistoryScreen(viewModel)
                    3 -> ReportScreen(viewModel)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        SchoolBranding()
    }
}

// el cuarto donde le preguntamos cosas personales al humano
@Composable
fun PersonalDataScreen(viewModel: GlucofinViewModel) {
    val profile = viewModel.patientProfile
    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        SectionTitle("Información Personal", Icons.Default.Badge)

        OutlinedTextField(
            value = profile.name,
            onValueChange = { s -> viewModel.updateProfile { it.name = s } },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        // cajitas para edad y sexo puestas una al lado de la otra
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) {
                NumberInput("Edad", profile.age.toString()) { s -> viewModel.updateProfile { p -> p.age = s.toIntOrNull() ?: 0 } }
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                SexSelector(profile.sex) { s -> viewModel.updateProfile { p -> p.sex = s } }
            }
        }

        // cajitas para peso y altura
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) {
                NumberInput("Peso (kg)", profile.weight.toString()) { s -> viewModel.updateProfile { p -> p.weight = s.toDoubleOrNull() ?: 0.0 } }
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                NumberInput("Altura (m)", profile.height.toString()) { s -> viewModel.updateProfile { p -> p.height = s.toDoubleOrNull() ?: 0.0 } }
            }
        }
        
        NumberInput("Cintura (cm)", profile.waist.toString()) { s -> viewModel.updateProfile { p -> p.waist = s.toDoubleOrNull() ?: 0.0 } }

        Spacer(modifier = Modifier.height(16.dp))
        SectionTitle("Hábitos y Antecedentes", Icons.Default.Favorite)
        
        // botoncitos que se prenden y apagan para saber que hace el humano
        BooleanInput("Ejercicio diario (≥30 min)", profile.dailyExercise) { viewModel.updateProfile { it.dailyExercise = !it.dailyExercise } }
        BooleanInput("Consume frutas/verduras", profile.fruitsVegetables) { viewModel.updateProfile { it.fruitsVegetables = !it.fruitsVegetables } }
        BooleanInput("Hipertensión arterial", profile.hypertension) { viewModel.updateProfile { it.hypertension = !it.hypertension } }
        BooleanInput("Antecedentes glucosa alta", profile.glucoseHistory) { viewModel.updateProfile { it.glucoseHistory = !it.glucoseHistory } }

        Spacer(modifier = Modifier.height(24.dp))
        // el boton magico que hace que el cerebro empiece a pensar
        Button(
            onClick = { viewModel.calculateRisk() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GENERAR EVALUACIÓN", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
        SchoolBranding()
    }
}

// el cuarto de los cientificos y los tubos de ensayo
@Composable
fun LabDataScreen(viewModel: GlucofinViewModel) {
    val profile = viewModel.patientProfile
    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        SectionTitle("Marcadores Clínicos", Icons.Default.Biotech)
        Text("Si no tiene algún resultado, puede dejarlo en 0.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        
        // más cajitas para meter numeros de laboratorio
        NumberInput("Glucosa Ayunas (mg/dL)", profile.fastingGlucose.toString()) { s -> viewModel.updateProfile { it.fastingGlucose = s.toDoubleOrNull() ?: 0.0 } }
        NumberInput("Glucosa Post-Carga (mg/dL)", profile.postOgttGlucose.toString()) { s -> viewModel.updateProfile { it.postOgttGlucose = s.toDoubleOrNull() ?: 0.0 } }
        NumberInput("HbA1c (%)", profile.hba1c.toString()) { s -> viewModel.updateProfile { it.hba1c = s.toDoubleOrNull() ?: 0.0 } }
        NumberInput("Insulina Ayunas (µIU/mL)", profile.fastingInsulin.toString()) { s -> viewModel.updateProfile { it.fastingInsulin = s.toDoubleOrNull() ?: 0.0 } }

        Spacer(modifier = Modifier.height(24.dp))
        SchoolBranding()
    }
}

// el diario donde anotamos cada vez que nos picamos el dedo
@Composable
fun HistoryScreen(viewModel: GlucofinViewModel) {
    var newValue by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(16.dp)) {
        SectionTitle("Bitácora de Glucosa", Icons.Default.History)
        Row(verticalAlignment = Alignment.CenterVertically) {
            // cajita para el nuevo número de azucar
            OutlinedTextField(
                value = newValue,
                onValueChange = { newValue = it },
                label = { Text("Lectura mg/dL") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.width(12.dp))
            // el boton de mas para guardar la lectura
            FloatingActionButton(onClick = { 
                newValue.toDoubleOrNull()?.let { viewModel.addGlucoseReading(it); newValue = "" }
            }) {
                Icon(Icons.Default.Add, "Add")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // la lista de todas las veces que el humano ha sido valiente
        Column(Modifier.verticalScroll(rememberScrollState())) {
            viewModel.patientProfile.history.forEach { reading ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color.Red)
                        Spacer(Modifier.width(16.dp))
                        Text(reading.toString(), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        SchoolBranding()
    }
}

// el cuarto final donde te decimos si estás bien o no tan bien
@Composable
fun ReportScreen(viewModel: GlucofinViewModel) {
    val result = viewModel.riskResult
    
    // configurando la fiesta de papelitos de colores
    val party = remember {
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xbdef53),
            position = Position.Relative(0.5, 0.3),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            SectionTitle("Resultado de Evaluación", Icons.Default.Analytics)
            
            // si todavia no has puesto datos, te ponemos una carita triste (o algo asi)
            if (result == null) {
                EmptyReportState(viewModel)
            } else {
                if (viewModel.patientProfile.name.isNotBlank()) {
                    Text(
                        text = "Reporte para: ${viewModel.patientProfile.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // si todo salio bien, ¡fiesta de papelitos!
                if (result.level == RiskLevel.LOW) {
                    KonfettiView(
                        modifier = Modifier.fillMaxSize(),
                        parties = listOf(party)
                    )
                }

                // la tarjeta de color que te dice tu destino
                Card(
                    colors = CardDefaults.cardColors(containerColor = result.level.color.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, result.level.color),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // un circulo de color para que se vea mas dramatico
                            Box(Modifier.size(12.dp).clip(CircleShape).background(result.level.color))
                            Spacer(Modifier.width(8.dp))
                            Text(result.status, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = result.level.color)
                        }
                        
                        // si el resultado es feo, ponemos una carita triste que se mueve
                        if (result.level == RiskLevel.HIGH || result.level == RiskLevel.CRITICAL) {
                            val infiniteTransition = rememberInfiniteTransition(label = "sad")
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 0.9f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000),
                                    repeatMode = RepeatMode.Reverse
                                ), label = "scale"
                            )
                            
                            Icon(
                                imageVector = Icons.Default.SentimentVeryDissatisfied,
                                contentDescription = "sad face",
                                modifier = Modifier.size(80.dp).padding(16.dp).graphicsLayer(scaleX = scale, scaleY = scale),
                                tint = result.level.color
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(result.action, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Recomendaciones para ti", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                result.recommendations.forEach { advice ->
                    RecommendationRow(advice)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Métricas Detalladas", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                
                // más numeritos para los que les gusta leer mucho
                DetailRow("Puntaje FINDRISC", "${result.detail.findriscScore} / 26", Icons.Default.Score)
                DetailRow("Índice HOMA-IR", result.detail.homaIr?.let { "%.2f".format(it) } ?: "Sin datos", Icons.Default.Calculate)
                DetailRow("IMC", "${result.detail.bmi} (${result.detail.bmiClass})", Icons.Default.MonitorWeight)
            }
            Spacer(modifier = Modifier.height(24.dp))
            SchoolBranding()
        }
    }
}

@Composable
fun SchoolBranding() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.mipmap.cnab_foreground),
                contentDescription = "Escudo Colegio",
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "ARMADA REPÚBLICA DE COLOMBIA",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "COLEGIOS NAVALES",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// un titulito con dibujito para separar las cosas
@Composable
fun SectionTitle(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

// el switch que hace clic-clac
@Composable
fun BooleanInput(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}

// el menu que se abre para elegir si eres chico o chica
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SexSelector(current: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = if (current == "M") "Masculino" else "Femenino",
            onValueChange = {},
            readOnly = true,
            label = { Text("Sexo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Masculino") }, onClick = { onSelect("M"); expanded = false })
            DropdownMenuItem(text = { Text("Femenino") }, onClick = { onSelect("F"); expanded = false })
        }
    }
}

// la cajita donde escribes numeros
@Composable
fun NumberInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value.takeIf { it != "0" && it != "0.0" } ?: "",
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    )
}

// una fila con un dibujito y un valor para el reporte
@Composable
fun RecommendationRow(text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

// una fila con un dibujito y un valor para el reporte
@Composable
fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(12.dp))
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// lo que sale cuando el humano todavía no ha hecho nada
@Composable
fun EmptyReportState(viewModel: GlucofinViewModel = viewModel()) {
    val name = viewModel.patientProfile.name
    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Info, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(16.dp))
        Text(
            if (name.isBlank()) "No hay evaluación reciente" else "Hola $name, aún no hay evaluación",
            fontWeight = FontWeight.Medium, 
            color = MaterialTheme.colorScheme.outline,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text("Completa tu perfil para ver el riesgo.", style = MaterialTheme.typography.bodySmall)
    }
}
