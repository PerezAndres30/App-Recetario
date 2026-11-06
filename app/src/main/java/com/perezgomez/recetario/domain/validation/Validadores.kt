package com.perezgomez.recetario.domain.validation

/** Reglas de validación puras. Cada función devuelve el mensaje de error o null si el valor es válido. */
object Validadores {
    private val CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun obligatorio(valor: String, campo: String): String? =
        if (valor.isBlank()) "$campo es obligatorio" else null

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !CORREO.matches(valor.trim()) -> "Escribe un correo válido"
        else -> null
    }

    fun password(valor: String): String? = when {
        valor.isEmpty() -> "La contraseña es obligatoria"
        valor.length < 8 -> "Mínimo 8 caracteres"
        else -> null
    }

    fun confirmacion(password: String, confirmacion: String): String? = when {
        confirmacion.isEmpty() -> "Confirma tu contraseña"
        confirmacion != password -> "Las contraseñas no coinciden"
        else -> null
    }

    fun longitudMaxima(valor: String, campo: String, max: Int): String? =
        if (valor.length > max) "$campo admite máximo $max caracteres" else null

    /** Valida que [valor] sea un entero dentro de [min]..[max]. */
    fun enteroEnRango(valor: String, campo: String, min: Int, max: Int): String? {
        val numero = valor.trim().toIntOrNull()
            ?: return "$campo debe ser un número entero"
        return if (numero !in min..max) "$campo debe estar entre $min y $max" else null
    }
}
