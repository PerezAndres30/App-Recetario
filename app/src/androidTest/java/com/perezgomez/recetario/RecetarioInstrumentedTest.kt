package com.perezgomez.recetario

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.perezgomez.recetario.core.hardware.tts.domain.TtsEngine
import com.perezgomez.recetario.di.FakeAndroidAuthRepository
import com.perezgomez.recetario.di.FakeAndroidRecetaRepository
import com.perezgomez.recetario.features.auth.domain.entities.Usuario
import com.perezgomez.recetario.features.recetas.domain.tts.RecetaTtsService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecetarioInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: FakeAndroidAuthRepository

    @Inject
    lateinit var recetaRepository: FakeAndroidRecetaRepository

    @Inject
    lateinit var ttsService: RecetaTtsService

    @Inject
    lateinit var ttsEngine: TtsEngine

    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        hiltRule.inject()
        recetaRepository.reiniciarDatosDePrueba()
    }

    @After
    fun tearDown() {
        ttsService.detener()
        scenario?.close()
        scenario = null
    }

    private fun iniciarActividad() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        composeRule.waitForIdle()
    }

    private fun esperarTexto(texto: String, timeoutMs: Long = 8000L) {
        composeRule.waitUntil(timeoutMs) {
            composeRule.onAllNodesWithText(texto, substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun guardarCaptura(nombreArchivo: String) {
        composeRule.waitForIdle()
        Thread.sleep(500)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val destino = File(dir, nombreArchivo)
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        assertTrue("No se pudo capturar pantalla para $nombreArchivo", bitmap != null)
        FileOutputStream(destino).use { out ->
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    @Test
    fun test01_explorarComoVisitante_conRecetas_yRestricciones() {
        authRepository.establecerSesion(null)
        iniciarActividad()

        esperarTexto("Explorar como visitante")
        composeRule.onNodeWithText("Explorar como visitante").performClick()

        esperarTexto("Explorar recetas")
        esperarTexto("Mole Poblano Comunitario")
        esperarTexto("Chilaquiles Rojos Caseros")

        // Restricciones de visitante: botón Iniciar sesión visible, sin FAB Nueva receta ni botón Eliminar receta
        composeRule.onNodeWithText("Iniciar sesión").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Nueva receta").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Eliminar receta").assertDoesNotExist()

        guardarCaptura("paso4_05_explorar_con_recetas.png")
    }

    @Test
    fun test02_misRecetas_autoria_yNavegacionExplorar() {
        authRepository.establecerSesion(Usuario("user-1", "Carlos", "carlos@test.com"))
        iniciarActividad()

        // MainActivity debe abrir Mis recetas cuando hay sesión y GetMisRecetasUseCase filtra solo propias
        esperarTexto("Mis recetas")
        esperarTexto("Hola, Carlos")
        esperarTexto("Chilaquiles Rojos Caseros")
        composeRule.onNodeWithText("Mole Poblano Comunitario").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Nueva receta").assertIsDisplayed()

        guardarCaptura("paso4_06_mis_recetas.png")

        // Cambiar a Explorar (catálogo público) y abrir receta ajena: sin Editar ni Eliminar
        composeRule.onNodeWithText("Explorar").performClick()
        esperarTexto("Mole Poblano Comunitario")
        composeRule.onNodeWithText("Mole Poblano Comunitario").performClick()

        esperarTexto("01 LECTURA POR VOZ ASISTIDA")
        composeRule.onNodeWithContentDescription("Editar").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Eliminar").assertDoesNotExist()

        composeRule.onNodeWithContentDescription("Volver").performClick()
        esperarTexto("Explorar recetas")
    }

    @Test
    fun test03_detalleYTextToSpeechNativo_flujoCompletoEInterrupcion() {
        authRepository.establecerSesion(Usuario("user-1", "Carlos", "carlos@test.com"))
        iniciarActividad()

        esperarTexto("Chilaquiles Rojos Caseros")
        composeRule.onNodeWithText("Chilaquiles Rojos Caseros").performClick()

        esperarTexto("01 LECTURA POR VOZ ASISTIDA")
        composeRule.onNodeWithContentDescription("Editar").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Eliminar").assertIsDisplayed()
        composeRule.onNodeWithText("Lista").assertIsDisplayed()
        composeRule.onNodeWithText("Reproducir").assertIsEnabled()
        composeRule.onNodeWithText("Continuar").assertIsNotEnabled()
        composeRule.onNodeWithText("Repetir").assertIsNotEnabled()

        guardarCaptura("paso4_07_detalle_inicial.png")

        // Iniciar reproducción con AndroidTtsEngine real y esperar onStart de intro
        composeRule.onNodeWithText("Reproducir").performClick()
        esperarTexto("Leyendo introducción")
        esperarTexto("Leyendo")
        composeRule.onNodeWithText("Continuar").assertIsEnabled()
        composeRule.onNodeWithText("Repetir").assertIsEnabled()
        Thread.sleep(600)
        guardarCaptura("paso4_08_detalle_reproduciendo.png")

        // Continuar al paso 1 de 3, verificar resaltado y esperar onDone del motor real
        composeRule.onNodeWithText("Continuar").performClick()
        esperarTexto("Paso 1 de 3")
        composeRule.onNodeWithText("Leyendo este paso...").performScrollTo().assertIsDisplayed()
        esperarTexto("En pausa", timeoutMs = 12000L)
        guardarCaptura("paso4_09_detalle_paso1.png")

        // Volver a la tarjeta de controles TTS para Repetir y completar pasos
        composeRule.onNodeWithText("Repetir").performScrollTo().performClick()
        esperarTexto("Paso 1 de 3")

        // Paso 2 de 3
        composeRule.onNodeWithText("Continuar").performScrollTo().performClick()
        esperarTexto("Paso 2 de 3")

        // Paso 3 de 3
        composeRule.onNodeWithText("Continuar").performScrollTo().performClick()
        esperarTexto("Paso 3 de 3")

        // Finalización de lectura y espera de onDone para pasar de "Leyendo" a "Finalizada"
        composeRule.onNodeWithText("Continuar").performScrollTo().performClick()
        esperarTexto("Receta completada")
        esperarTexto("Finalizada", timeoutMs = 12000L)
        composeRule.onNodeWithText("Finalizada").assertIsDisplayed()
        composeRule.onNodeWithText("Continuar").assertIsNotEnabled()
        composeRule.onNodeWithText("Reiniciar").assertIsEnabled()
        guardarCaptura("paso4_10_detalle_finalizado.png")

        // Reiniciar lectura con botón Reiniciar (reproducir de nuevo) y verificar interrupción al salir
        composeRule.onNodeWithText("Reiniciar").performClick()
        esperarTexto("Leyendo introducción")

        composeRule.onNodeWithContentDescription("Volver").performClick()
        esperarTexto("Mis recetas")
        composeRule.waitForIdle()
        // Al salir de DetalleScreen, DisposableEffect + onCleared detienen la locución activa
        assertTrue(!ttsService.estado.value.estaReproduciendo)
    }

    @Test
    fun test04_formularioCreacionEdicion_yEliminarRecetaPropia() {
        authRepository.establecerSesion(Usuario("user-1", "Carlos", "carlos@test.com"))
        iniciarActividad()

        esperarTexto("Mis recetas")
        composeRule.onNodeWithContentDescription("Nueva receta").performClick()

        // Modo creación
        esperarTexto("Nueva receta")
        composeRule.onNodeWithText("Título").performScrollTo().performTextInput("Flan Napolitano")
        composeRule.onNodeWithText("Descripción (opcional)").performScrollTo().performTextInput("Postre clásico horneado.")
        composeRule.onNodeWithText("postre").performScrollTo().performClick()
        composeRule.onNodeWithText("Tiempo (min)").performScrollTo().performTextInput("45")
        composeRule.onNodeWithText("Porciones").performScrollTo().performTextReplacement("6")
        composeRule.onNodeWithText("Ingrediente 1").performScrollTo().performTextInput("Leche condensada")
        composeRule.onNodeWithText("Paso 1").performScrollTo().performTextInput("Hornear a baño María.")
        guardarCaptura("paso4_11_form_crear.png")

        composeRule.onNodeWithText("Guardar receta").performScrollTo().performClick()

        // Regreso a Mis recetas con snackbar y receta creada visible
        esperarTexto("Receta creada")
        esperarTexto("Flan Napolitano")

        // Abrir detalle y entrar a modo edición
        composeRule.onNodeWithText("Flan Napolitano").performClick()
        esperarTexto("01 LECTURA POR VOZ ASISTIDA")
        composeRule.onNodeWithContentDescription("Editar").performClick()

        esperarTexto("Editar receta")
        composeRule.onNodeWithText("Flan Napolitano").performScrollTo().performTextReplacement("Flan de Vainilla")
        guardarCaptura("paso4_12_form_editar.png")

        composeRule.onNodeWithText("Guardar cambios").performScrollTo().performClick()
        esperarTexto("Receta actualizada")
        esperarTexto("Flan de Vainilla")

        // Eliminar usando EliminarRecetaPropiaUseCase desde DetalleScreen
        composeRule.onNodeWithContentDescription("Eliminar").performClick()
        esperarTexto("Eliminar receta")
        composeRule.onNodeWithText("Eliminar").performClick()

        esperarTexto("Receta eliminada")
        esperarTexto("Mis recetas")
        composeRule.onNodeWithText("Flan de Vainilla").assertDoesNotExist()
    }

    @Test
    fun test05_likesYFavoritos_visitanteYRegistrado_sincronizacionYCierreSesion() {
        // 1) Visitante: da like en Explorar y verifica en Detalle sin acceso a Favoritos
        authRepository.establecerSesion(null)
        iniciarActividad()

        esperarTexto("Explorar como visitante")
        composeRule.onNodeWithText("Explorar como visitante").performClick()

        esperarTexto("Mole Poblano Comunitario")
        composeRule.onNodeWithContentDescription("Guardar en favoritos").assertDoesNotExist()

        composeRule.onAllNodesWithContentDescription("Dar me gusta, 0 me gusta").onFirst().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Quitar me gusta, 1 me gusta").assertIsDisplayed()
        guardarCaptura("etapa3_01_visitante_like_explorar.png")

        // 2) Iniciar sesión y guardar receta ajena en Favoritos
        composeRule.onNodeWithText("Iniciar sesión").performClick()
        esperarTexto("Bienvenido de vuelta")
        composeRule.onNodeWithText("Correo").performTextInput("carlos@test.com")
        composeRule.onNodeWithText("Contraseña").performTextInput("Password123")
        composeRule.onNodeWithText("Iniciar sesión").performScrollTo().performClick()

        esperarTexto("Mis recetas")
        composeRule.onNodeWithText("Explorar").performClick()
        esperarTexto("Mole Poblano Comunitario")

        composeRule.onAllNodesWithContentDescription("Guardar en favoritos").onFirst().performClick()
        composeRule.waitForIdle()

        // 3) Abrir pestaña Favoritos y verificar receta guardada con estado real
        composeRule.onNodeWithText("Favoritos").performClick()
        esperarTexto("Colección guardada")
        esperarTexto("Mole Poblano Comunitario")
        guardarCaptura("etapa3_02_favoritos_con_recetas.png")

        // 4) Abrir detalle desde Favoritos y verificar sincronización de Like + Favorito + TTS
        composeRule.onNodeWithText("Mole Poblano Comunitario").performClick()
        esperarTexto("1 me gusta")
        composeRule.onNodeWithText("Guardada").assertIsDisplayed()
        guardarCaptura("etapa3_03_detalle_sincronizado.png")

        composeRule.onNodeWithContentDescription("Volver").performClick()
        esperarTexto("Mole Poblano Comunitario")

        // 5) Quitar de Favoritos -> pasa a estado vacío real y cerrar sesión vuelve a Bienvenida
        composeRule.onNodeWithContentDescription("Quitar de favoritos").performClick()
        esperarTexto("Aún no tienes recetas en Favoritos")
        guardarCaptura("etapa3_04_favoritos_vacio.png")

        composeRule.onNodeWithContentDescription("Cerrar sesión").performClick()
        esperarTexto("Explorar como visitante")
    }
}
