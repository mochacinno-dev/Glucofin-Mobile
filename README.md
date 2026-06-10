<div align="center">

<img src="logo_android.png" width="250" alt="Glucofin logo"/>

# Glucofin — Android

**Detección de riesgo de diabetes en tu bolsillo**

[![Android](https://img.shields.io/badge/Android-26%2B-C0392B?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-922B21?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-2026.02-7B241C?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material_3-Dynamic_Color-E74C3C?style=flat-square&logo=material-design&logoColor=white)](https://m3.material.io)
[![Licencia MIT](https://img.shields.io/badge/Licencia-MIT-C0392B?style=flat-square)](LICENSE)

</div>

---

Glucofin estima tu riesgo de desarrollar diabetes tipo 2 combinando el cuestionario clínico **FINDRISC** (validado por la OMS) con datos opcionales de laboratorio. Sin glucómetro, sin conta de sangre, directo desde tu teléfono.

> ⚕ Glucofin es una herramienta de orientación. No reemplaza el diagnóstico médico.

---

## Pantallas

| Perfil | Laboratorio | Historial | Reporte |
|:---:|:---:|:---:|:---:|
| Datos personales, hábitos y antecedentes | Glucosa, HbA1c, insulina | Registro de lecturas con fecha | Resultado con código de color |

---

## Funciones

| Módulo | Descripción |
|---|---|
| **Perfil personal** | Edad, peso, altura, cintura, sexo, hábitos y antecedentes familiares |
| **Análisis clínicos** | Glucosa en ayunas, OGTT 2h, HbA1c e insulina |
| **Historial glucémico** | Registro de lecturas individuales con marca de tiempo |
| **Reporte de riesgo** | Clasificación FINDRISC + HOMA-IR + IMC con color semántico |

---

## Instalación  y Dependencias

```bash
# 1. JDK 21
sudo pacman -S jdk21-openjdk

# 2. Android Studio (AUR)
yay -S android-studio
# — o Android SDK solo (sin IDE):
yay -S android-sdk android-sdk-build-tools android-sdk-platform-tools

# 3. Aceptar licencias
yes | sdkmanager --licenses

# 4. Clonar y compilar
git clone https://github.com/mochacinno-dev/glucofin.git
cd glucofin
./gradlew assembleDebug

# 5. Instalar en dispositivo / emulador conectado
./gradlew installDebug
```

> En Android Studio: `File → Open` → selecciona la carpeta del proyecto → `Run ▶`

---

## Estructura del proyecto

```
app/src/main/
├── java/com/mochi/glucofin/
│   ├── GlucofinLogic.kt      # Cálculos FINDRISC, HOMA-IR, IMC
│   ├── GlucofinViewModel.kt  # Estado reactivo de la app
│   └── MainActivity.kt       # UI completa en Jetpack Compose
└── res/
    └── values/
        ├── font_certs.xml    # Certificados Google Fonts
        └── strings.xml
```

---

## Métricas calculadas

- **FINDRISC** — 0 a 26 pts, cinco niveles de riesgo
- **HOMA-IR** — índice de resistencia a la insulina
- **IMC** — clasificación OMS (bajo peso → obesidad)

---

<div align="center">
<sub>Cuestionario FINDRISC © Finnish Diabetes Association</sub>
</div>
