package com.perezgomez.recetario.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadoresTest {

    @Test
    fun obligatorio_conTextoValido_retornaNull() {
        val resultado = Validadores.obligatorio("Pastel de chocolate", "El título")
        assertNull(resultado)
    }

    @Test
    fun obligatorio_conTextoVacioOEspacios_retornaError() {
        val errorVacio = Validadores.obligatorio("", "El título")
        assertEquals("El título es obligatorio", errorVacio)

        val errorEspacios = Validadores.obligatorio("   ", "El título")
        assertEquals("El título es obligatorio", errorEspacios)
    }

    @Test
    fun correo_conFormatoValido_retornaNull() {
        assertNull(Validadores.correo("chef@recetario.com"))
        assertNull(Validadores.correo("juan.perez+test@dominio.org"))
    }

    @Test
    fun correo_vacio_retornaErrorObligatorio() {
        assertEquals("El correo es obligatorio", Validadores.correo(""))
        assertEquals("El correo es obligatorio", Validadores.correo("   "))
    }

    @Test
    fun correo_conFormatoInvalido_retornaErrorFormato() {
        assertEquals("Escribe un correo válido", Validadores.correo("sin_arroba.com"))
        assertEquals("Escribe un correo válido", Validadores.correo("usuario@"))
        assertEquals("Escribe un correo válido", Validadores.correo("@dominio.com"))
        assertEquals("Escribe un correo válido", Validadores.correo("usuario@dominio"))
    }

    @Test
    fun password_conLongitudValida_retornaNull() {
        // Límite exacto de 8 caracteres
        assertNull(Validadores.password("12345678"))
        // Más de 8 caracteres
        assertNull(Validadores.password("super_password_seguro_123"))
    }

    @Test
    fun password_vacia_retornaErrorObligatoria() {
        assertEquals("La contraseña es obligatoria", Validadores.password(""))
    }

    @Test
    fun password_conMenosDe8Caracteres_retornaErrorMinimo() {
        // Límite inválido inferior: 7 caracteres
        assertEquals("Mínimo 8 caracteres", Validadores.password("1234567"))
        assertEquals("Mínimo 8 caracteres", Validadores.password("a"))
    }

    @Test
    fun confirmacion_conCoincidencia_retornaNull() {
        assertNull(Validadores.confirmacion("clave123", "clave123"))
    }

    @Test
    fun confirmacion_vacia_retornaErrorVacio() {
        assertEquals("Confirma tu contraseña", Validadores.confirmacion("clave123", ""))
    }

    @Test
    fun confirmacion_noCoincide_retornaError() {
        assertEquals("Las contraseñas no coinciden", Validadores.confirmacion("clave123", "clave456"))
    }

    @Test
    fun longitudMaxima_dentroDelLimite_retornaNull() {
        assertNull(Validadores.longitudMaxima("Hola", "El título", 10))
        // Límite exacto de 10 caracteres
        assertNull(Validadores.longitudMaxima("1234567890", "El título", 10))
    }

    @Test
    fun longitudMaxima_excedida_retornaError() {
        // Límite superior + 1
        assertEquals("El título admite máximo 10 caracteres", Validadores.longitudMaxima("12345678901", "El título", 10))
    }

    @Test
    fun enteroEnRango_dentroDelRango_retornaNull() {
        assertNull(Validadores.enteroEnRango("30", "El tiempo", 0, 120))
        // Límites exactos
        assertNull(Validadores.enteroEnRango("0", "El tiempo", 0, 120))
        assertNull(Validadores.enteroEnRango("120", "El tiempo", 0, 120))
    }

    @Test
    fun enteroEnRango_fueraDeLimites_retornaErrorRango() {
        assertEquals("El tiempo debe estar entre 0 y 120", Validadores.enteroEnRango("-1", "El tiempo", 0, 120))
        assertEquals("El tiempo debe estar entre 0 y 120", Validadores.enteroEnRango("121", "El tiempo", 0, 120))
    }

    @Test
    fun enteroEnRango_noEnteroONoNumerico_retornaErrorFormato() {
        assertEquals("El tiempo debe ser un número entero", Validadores.enteroEnRango("treinta", "El tiempo", 0, 120))
        assertEquals("El tiempo debe ser un número entero", Validadores.enteroEnRango("15.5", "El tiempo", 0, 120))
        assertEquals("El tiempo debe ser un número entero", Validadores.enteroEnRango("", "El tiempo", 0, 120))
        assertEquals("El tiempo debe ser un número entero", Validadores.enteroEnRango("   ", "El tiempo", 0, 120))
    }
}
