package com.mochi.glucofin

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.roundToLong

// el número mágico para que el azúcar tenga sentido
const val CONSTANTE_MOLAR_GLUCOSA = 18.01559

// una cajita que guarda cuanta azúcar tienes y cuando te picaste el dedo
data class GlucoseReading(
    val valueMgdl: Double,
    val dateTime: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String {
        // haciendo que la fecha se vea bonita y no como un robot
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        return "[${dateTime.format(formatter)}]  ${"%.1f".format(valueMgdl)} mg/dL"
    }
}

// colores para saber si estamos felices o asustados
enum class RiskLevel(val color: Color) {
    LOW(Color(0xFF2ECC71)),      // todo tranqui (verde)
    MODERATE(Color(0xFFF1C40F)), // mmm ojito (amarillo)
    HIGH(Color(0xFFE67E22)),     // ya me dio miedo (naranja)
    CRITICAL(Color(0xFFE74C3C)), // corre al doctor (rojo)
    NONE(Color(0xFF95A5A6))      // no se nada todavia (gris)
}

// aqui guardamos todo lo que sabemos del humano
data class PatientProfile(
    var fastingGlucose: Double = 0.0,
    var postOgttGlucose: Double = 0.0,
    var hba1c: Double = 0.0,
    var fastingInsulin: Double = 0.0,
    var weight: Double = 0.0,
    var height: Double = 0.0,
    var waist: Double = 0.0,
    var sex: String = "M",
    var age: Int = 0,
    var dailyExercise: Boolean = false,
    var fruitsVegetables: Boolean = false,
    var hypertension: Boolean = false,
    var glucoseHistory: Boolean = false,
    var familyHistory: Int = 0,
    val history: MutableList<GlucoseReading> = mutableListOf()
)

// el cerebro que hace las cuentas matemagicas
object GlucofinCalculator {

    // cuenta para saber que tan grande es el humano
    fun calculateBmi(weight: Double, height: Double): Double {
        if (height <= 0) return 0.0
        return (weight / height.pow(2) * 100.0).roundToLong() / 100.0
    }

    // poniendole nombres al tamaño del humano
    fun classifyBmi(bmi: Double): String = when {
        bmi < 18.5 -> "Bajo peso"
        bmi < 25.0 -> "Normal"
        bmi < 30.0 -> "Sobrepeso"
        else -> "Obesidad"
    }

    // contando puntitos para ver si el monstruo de la diabetes esta cerca
    fun calculateFindrisc(p: PatientProfile): Int {
        var pts = 0
        // mas viejo = mas puntos (que mal)
        pts += when {
            p.age < 45 -> 0
            p.age <= 54 -> 2
            p.age <= 64 -> 3
            else -> 4
        }
        // mas chonk = mas puntos
        val bmi = calculateBmi(p.weight, p.height)
        pts += when {
            bmi < 25 -> 0
            bmi <= 30 -> 1
            else -> 3
        }
        // la pancita tambien cuenta
        if (p.sex == "M") {
            pts += when { p.waist < 94 -> 0; p.waist <= 102 -> 1; else -> 3 }
        } else {
            pts += when { p.waist < 80 -> 0; p.waist <= 88 -> 1; else -> 3 }
        }
        // si no te mueves o no comes verduras, el monstruo se alegra
        if (!p.dailyExercise) pts += 2
        if (!p.fruitsVegetables) pts += 1
        if (p.hypertension) pts += 2
        if (p.glucoseHistory) pts += 5
        pts += when (p.familyHistory) { 1 -> 3; 2 -> 5; else -> 0 }
        return pts.coerceAtMost(26)
    }

    // aqui decidimos si el humano esta a salvo o no
    fun evaluateRisk(p: PatientProfile): RiskResult {
        val findrisc = calculateFindrisc(p)
        val bmi = calculateBmi(p.weight, p.height)
        val homa = if (p.fastingInsulin > 0 && p.fastingGlucose > 0) calculateHomaIr(p.fastingGlucose, p.fastingInsulin) else null

        val detail = RiskDetail(
            findriscScore = findrisc,
            homaIr = homa,
            bmi = bmi,
            bmiClass = classifyBmi(bmi)
        )

        // la hora de la verdad... chan chan chan
        return when {
            // si el azucar esta super alta, ya valio
            (p.hba1c >= 6.5 && p.hba1c > 0) || (p.fastingGlucose >= 126 && p.fastingGlucose > 0) ->
                RiskResult("Diabetes Detectada", "Requiere atención médica inmediata.", RiskLevel.CRITICAL, detail)
            
            // si esta mas o menos alta, cuidado
            (p.hba1c in 5.7..6.4) || (p.fastingGlucose in 100.0..125.0) ->
                RiskResult("Prediabetes", "Riesgo alto. Cambie hábitos alimenticios.", RiskLevel.HIGH, detail)

            // si los puntos findrisc estan por las nubes
            findrisc >= 15 -> RiskResult("Riesgo Muy Alto", "Puntuación FINDRISC crítica.", RiskLevel.HIGH, detail)
            
            // si estas en el medio
            findrisc >= 12 || (homa != null && homa >= 2.9) -> 
                RiskResult("Riesgo Moderado", "Considere chequeos médicos.", RiskLevel.MODERATE, detail)

            // yuju! estas bien
            else -> RiskResult("Riesgo Bajo", "Continúe con su estilo de vida saludable.", RiskLevel.LOW, detail)
        }
    }

    // una cuenta rara para la insulina
    private fun calculateHomaIr(g: Double, i: Double) = (i * g / 405.0 * 100.0).roundToLong() / 100.0
}

// una cajita para el resultado final
data class RiskResult(
    val status: String,
    val action: String,
    val level: RiskLevel,
    val detail: RiskDetail
)

// mas cajitas con numeritos detallados
data class RiskDetail(
    val findriscScore: Int,
    val homaIr: Double?,
    val bmi: Double,
    val bmiClass: String
)
