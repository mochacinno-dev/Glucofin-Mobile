package com.mochi.glucofin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GlucofinViewModel : ViewModel() {
    var patientProfile by mutableStateOf(PatientProfile())
        private set

    var riskResult by mutableStateOf<RiskResult?>(null)
        private set

    fun updateProfile(update: (PatientProfile) -> Unit) {
        val current = patientProfile.copy(history = patientProfile.history.toMutableList())
        update(current)
        patientProfile = current
    }

    fun calculateRisk() {
        riskResult = GlucofinCalculator.evaluateRisk(patientProfile)
    }

    fun addGlucoseReading(value: Double) {
        val newReading = GlucoseReading(value)
        val newList = patientProfile.history.toMutableList()
        newList.add(0, newReading) // Agregar al inicio para ver las más recientes primero
        updateProfile { it.history.clear(); it.history.addAll(newList) }
    }
}
