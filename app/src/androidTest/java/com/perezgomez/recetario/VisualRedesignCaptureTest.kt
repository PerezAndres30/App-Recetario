package com.perezgomez.recetario

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.perezgomez.recetario.core.ui.components.ConfirmarEliminarDialog
import com.perezgomez.recetario.features.auth.presentation.screens.BienvenidaScreen
import com.perezgomez.recetario.features.auth.presentation.screens.LoginContent
import com.perezgomez.recetario.features.auth.presentation.screens.LoginUiState
import com.perezgomez.recetario.features.auth.presentation.screens.RegisterContent
import com.perezgomez.recetario.features.auth.presentation.screens.RegisterUiState
import com.perezgomez.recetario.features.recetas.domain.entities.Ingrediente
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.tts.EstadoLectura
import com.perezgomez.recetario.features.recetas.domain.tts.EtapaLectura
import com.perezgomez.recetario.features.recetas.presentation.screens.DetalleContent
import com.perezgomez.recetario.features.recetas.presentation.screens.DetalleContenido
import com.perezgomez.recetario.features.recetas.presentation.screens.DetalleUiState
import com.perezgomez.recetario.features.recetas.presentation.screens.FavoritosContent
import com.perezgomez.recetario.features.recetas.presentation.screens.FavoritosListaUi
import com.perezgomez.recetario.features.recetas.presentation.screens.FormContent
import com.perezgomez.recetario.features.recetas.presentation.screens.FormErrores
import com.perezgomez.recetario.features.recetas.presentation.screens.FormUiState
import com.perezgomez.recetario.features.recetas.presentation.screens.HomeContent
import com.perezgomez.recetario.features.recetas.presentation.screens.HomeUiState
import com.perezgomez.recetario.features.recetas.presentation.screens.IngredienteForm
import com.perezgomez.recetario.features.recetas.presentation.screens.ListaUi
import com.perezgomez.recetario.features.recetas.presentation.screens.ModoHome
import com.perezgomez.recetario.ui.theme.RecetarioTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.io.File
import java.io.FileOutputStream
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Renderiza y captura en el emulador Pixel_8_API35 las 24 pantallas o estados definidos en Figma
 * de la sección «02 · Rediseño Editorial Definitivo · Auditoría Completa (24 Pantallas)».
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class VisualRedesignCaptureTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
    }

    private fun mostrarYCapturar(
        nombreArchivo: String,
        nombreSecundario: String? = null,
        contenido: @Composable () -> Unit,
    ) {
        val launched = ActivityScenario.launch(MainActivity::class.java)
        scenario = launched
        launched.onActivity { activity ->
            activity.setContent {
                RecetarioTheme {
                    contenido()
                }
            }
        }
        composeRule.waitForIdle()
        Thread.sleep(600)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        assertTrue("No se pudo capturar $nombreArchivo", bitmap != null)

        val destino = File(dir, nombreArchivo)
        FileOutputStream(destino).use { out ->
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        if (nombreSecundario != null) {
            val destinoSec = File(dir, nombreSecundario)
            FileOutputStream(destinoSec).use { out ->
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }

    private fun recetasMuestra() = listOf(
        Receta(
            id = "m1",
            titulo = "Tarta de Fresa y Vainilla",
            descripcion = "Base crujiente de mantequilla con crema suave y fresas frescas.",
            categoria = "postre",
            tiempoMinutos = 50,
            porciones = 8,
            ingredientes = listOf(
                Ingrediente("Fresas frescas", "300", "g"),
                Ingrediente("Crema para batir", "250", "ml"),
                Ingrediente("Vainilla", "1", "cdta"),
            ),
            pasos = listOf(
                "Hornear la base de galleta durante 15 minutos a 180 °C.",
                "Batir la crema con vainilla hasta lograr picos suaves.",
                "Decorar con las fresas laminadas y refrigerar.",
            ),
            esPropia = true,
            esFavorito = true,
            likesCount = 34,
            leDiLike = true,
            creadaEn = "2026-11-27",
        ),
        Receta(
            id = "m2",
            titulo = "Chilaquiles Rojos Caseros",
            descripcion = "Totopos crujientes bañados en salsa tibia de jitomate.",
            categoria = "desayuno",
            tiempoMinutos = 25,
            porciones = 2,
            ingredientes = listOf(Ingrediente("Totopos", "250", "g")),
            pasos = listOf("Preparar salsa y servir con queso fresco."),
            esPropia = false,
            esFavorito = true,
            likesCount = 19,
            leDiLike = false,
            creadaEn = "2026-11-26",
        ),
        Receta(
            id = "m3",
            titulo = "Limonada de Fresa y Menta",
            descripcion = "Bebida refrescante sin fotografía, con hierbabuena de huerto.",
            categoria = "bebida",
            tiempoMinutos = 10,
            porciones = 4,
            ingredientes = listOf(Ingrediente("Limones", "4", "pzas")),
            pasos = listOf("Mezclar con hielo."),
            esPropia = false,
            esFavorito = false,
            likesCount = 12,
            leDiLike = true,
        ),
        Receta(
            id = "m4",
            titulo = "Ñoquis de Calabaza al Romero",
            categoria = "comida",
            tiempoMinutos = 35,
            porciones = 3,
            ingredientes = listOf(Ingrediente("Ñoquis", "400", "g")),
            pasos = listOf("Saltear en mantequilla dorada con romero fresco."),
            esPropia = true,
            esFavorito = false,
            likesCount = 27,
            leDiLike = false,
        ),
    )

    // ==========================================
    // 01 · Bienvenida · Editorial
    // ==========================================
    @Test
    fun captura01_bienvenida() = mostrarYCapturar("view_01_bienvenida.png", "redesign_01_bienvenida.png") {
        BienvenidaScreen(
            onExplorarVisitante = {},
            onIniciarSesion = {},
            onCrearCuenta = {},
        )
    }

    // ==========================================
    // 02 · Iniciar sesión
    // ==========================================
    @Test
    fun captura02_iniciarSesion() = mostrarYCapturar("view_02_iniciar_sesion.png", "redesign_02_login.png") {
        LoginContent(
            state = LoginUiState(correo = "sofia@cocina.mx"),
            onCorreoChange = {},
            onPasswordChange = {},
            onIniciarSesion = {},
            onIrARegistro = {},
        )
    }

    // ==========================================
    // 03 · Crear cuenta
    // ==========================================
    @Test
    fun captura03_crearCuenta() = mostrarYCapturar("view_03_crear_cuenta.png", "redesign_03_registro.png") {
        RegisterContent(
            state = RegisterUiState(nombre = "Sofía", correo = "sofia@cocina.mx"),
            onNombreChange = {},
            onCorreoChange = {},
            onPasswordChange = {},
            onConfirmacionChange = {},
            onRegistrar = {},
            onVolver = {},
        )
    }

    // ==========================================
    // 04 · Login · Errores de validación
    // ==========================================
    @Test
    fun captura04_loginErroresValidacion() = mostrarYCapturar("view_04_login_errores_validacion.png") {
        LoginContent(
            state = LoginUiState(
                correo = "correo-invalido",
                correoError = "Escribe un correo válido",
                password = "",
                passwordError = "La contraseña es obligatoria",
                error = "Credenciales incorrectas o sin conexión con el servidor.",
            ),
            onCorreoChange = {},
            onPasswordChange = {},
            onIniciarSesion = {},
            onIrARegistro = {},
        )
    }

    // ==========================================
    // 05 · Explorar · Visitante (Likes + Receta sin foto)
    // ==========================================
    @Test
    fun captura05_explorarVisitante() = mostrarYCapturar("view_05_explorar_visitante.png") {
        HomeContent(
            state = HomeUiState(
                modo = ModoHome.EXPLORAR,
                estaAutenticado = false,
                nombreUsuario = "",
                lista = ListaUi.Contenido(recetasMuestra()),
            ),
            likesPorReceta = mapOf(
                "m1" to (34 to true),
                "m2" to (19 to false),
                "m3" to (12 to false),
                "m4" to (27 to false),
            ),
            favoritosPorReceta = emptySet(),
            onNuevaReceta = {},
            onVerDetalle = {},
            onEliminarClick = {},
            onReintentar = {},
            onActualizar = {},
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onIniciarSesion = {},
            onCerrarSesion = {},
        )
    }

    // ==========================================
    // 06 · Explorar · Registrado (Likes + Favoritos on/off)
    // ==========================================
    @Test
    fun captura06_explorarRegistrado() = mostrarYCapturar("view_06_explorar_registrado.png", "redesign_04_explorar_registrado.png") {
        HomeContent(
            state = HomeUiState(
                modo = ModoHome.EXPLORAR,
                estaAutenticado = true,
                nombreUsuario = "Ana Sofía",
                lista = ListaUi.Contenido(recetasMuestra()),
            ),
            likesPorReceta = mapOf(
                "m1" to (34 to true),
                "m2" to (19 to false),
                "m3" to (12 to true),
                "m4" to (27 to false),
            ),
            favoritosPorReceta = setOf("m1", "m2"),
            onNuevaReceta = {},
            onVerDetalle = {},
            onEliminarClick = {},
            onReintentar = {},
            onActualizar = {},
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onIniciarSesion = {},
            onCerrarSesion = {},
        )
    }

    // ==========================================
    // 07 · Mis recetas · Propias (Autoría y gestión)
    // ==========================================
    @Test
    fun captura07_misRecetasPropias() = mostrarYCapturar("view_07_mis_recetas_propias.png") {
        val propias = recetasMuestra().filter { it.esPropia }
        HomeContent(
            state = HomeUiState(
                modo = ModoHome.MIS_RECETAS,
                estaAutenticado = true,
                nombreUsuario = "Carlos",
                lista = ListaUi.Contenido(propias),
            ),
            likesPorReceta = mapOf("m1" to (34 to true), "m4" to (27 to false)),
            favoritosPorReceta = setOf("m1"),
            onNuevaReceta = {},
            onVerDetalle = {},
            onEliminarClick = {},
            onReintentar = {},
            onActualizar = {},
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onIniciarSesion = {},
            onCerrarSesion = {},
        )
    }

    // ==========================================
    // 08 · Favoritos · Colección privada
    // ==========================================
    @Test
    fun captura08_favoritosColeccionPrivada() = mostrarYCapturar("view_08_favoritos_coleccion_privada.png", "redesign_05_favoritos_con_recetas.png") {
        val muestras = recetasMuestra()
        FavoritosContent(
            nombreUsuario = "Ana Sofía",
            lista = FavoritosListaUi.Contenido(
                listOf(
                    muestras[0].copy(likesCount = 34, leDiLike = true, esFavorito = true),
                    muestras[1].copy(likesCount = 19, leDiLike = false, esFavorito = true),
                ),
            ),
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onVerDetalle = {},
            onToggleLike = {},
            onQuitarFavorito = {},
            onCerrarSesion = {},
        )
    }

    // ==========================================
    // 09 · Catálogo · Cargando
    // ==========================================
    @Test
    fun captura09_catalogoCargando() = mostrarYCapturar("view_09_catalogo_cargando.png") {
        HomeContent(
            state = HomeUiState(
                modo = ModoHome.EXPLORAR,
                estaAutenticado = true,
                nombreUsuario = "Ana Sofía",
                lista = ListaUi.Cargando,
            ),
            onNuevaReceta = {},
            onVerDetalle = {},
            onEliminarClick = {},
            onReintentar = {},
            onActualizar = {},
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onIniciarSesion = {},
            onCerrarSesion = {},
        )
    }

    // ==========================================
    // 10 · Catálogo · Error de conexión y Reintento
    // ==========================================
    @Test
    fun captura10_catalogoErrorReintento() = mostrarYCapturar("view_10_catalogo_error_reintento.png") {
        HomeContent(
            state = HomeUiState(
                modo = ModoHome.EXPLORAR,
                estaAutenticado = false,
                lista = ListaUi.Error("No se pudieron cargar las recetas. Verifica tu internet e intenta nuevamente."),
            ),
            onNuevaReceta = {},
            onVerDetalle = {},
            onEliminarClick = {},
            onReintentar = {},
            onActualizar = {},
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onIniciarSesion = {},
            onCerrarSesion = {},
        )
    }

    // ==========================================
    // 11 · Explorar · Catálogo vacío
    // ==========================================
    @Test
    fun captura11_explorarCatalogoVacio() = mostrarYCapturar("view_11_explorar_catalogo_vacio.png") {
        HomeContent(
            state = HomeUiState(
                modo = ModoHome.EXPLORAR,
                estaAutenticado = false,
                lista = ListaUi.Vacia,
            ),
            onNuevaReceta = {},
            onVerDetalle = {},
            onEliminarClick = {},
            onReintentar = {},
            onActualizar = {},
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onIniciarSesion = {},
            onCerrarSesion = {},
        )
    }

    // ==========================================
    // 12 · Mis recetas · Vacío
    // ==========================================
    @Test
    fun captura12_misRecetasVacio() = mostrarYCapturar("view_12_mis_recetas_vacio.png") {
        HomeContent(
            state = HomeUiState(
                modo = ModoHome.MIS_RECETAS,
                estaAutenticado = true,
                nombreUsuario = "Carlos",
                lista = ListaUi.Vacia,
            ),
            onNuevaReceta = {},
            onVerDetalle = {},
            onEliminarClick = {},
            onReintentar = {},
            onActualizar = {},
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onIniciarSesion = {},
            onCerrarSesion = {},
        )
    }

    // ==========================================
    // 13 · Favoritos · Estado vacío
    // ==========================================
    @Test
    fun captura13_favoritosEstadoVacio() = mostrarYCapturar("view_13_favoritos_estado_vacio.png", "redesign_06_favoritos_vacio.png") {
        FavoritosContent(
            nombreUsuario = "Ana Sofía",
            lista = FavoritosListaUi.Vacia,
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onVerDetalle = {},
            onToggleLike = {},
            onQuitarFavorito = {},
            onCerrarSesion = {},
        )
    }

    // ==========================================
    // 14 · Confirmar eliminación (Diálogo)
    // ==========================================
    @Test
    fun captura14_confirmarEliminacionDialog() = mostrarYCapturar("view_14_confirmar_eliminacion_dialog.png") {
        Box(modifier = Modifier.fillMaxSize()) {
            ConfirmarEliminarDialog(
                titulo = "Tarta de Fresa y Vainilla",
                cargando = false,
                onConfirmar = {},
                onCancelar = {},
            )
        }
    }

    // ==========================================
    // 15 · Detalle · Visitante (Sin foto, Likes, TTS)
    // ==========================================
    @Test
    fun captura15_detalleVisitante() = mostrarYCapturar("view_15_detalle_visitante.png") {
        val muestraSinFoto = recetasMuestra()[2] // Limonada sin foto
        DetalleContent(
            state = DetalleUiState(
                esPropietarioVerificado = false,
                estaAutenticado = false,
                contenido = DetalleContenido.Listo(muestraSinFoto),
                ttsState = EstadoLectura(etapa = EtapaLectura.INACTIVO),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            estaAutenticado = false,
            likesCount = 8,
            leDiLike = false,
            esFavorito = false,
            onVolver = {},
            onEditar = {},
            onEliminarClick = {},
            onReintentar = {},
            onReproducirTts = {},
            onContinuarTts = {},
            onRepetirTts = {},
        )
    }

    // ==========================================
    // 16 · Detalle · Registrado no propietario (Likes + Favorito)
    // ==========================================
    @Test
    fun captura16_detalleRegistradoNoPropietario() = mostrarYCapturar("view_16_detalle_registrado_no_propietario.png") {
        val chilaquiles = recetasMuestra()[1]
        DetalleContent(
            state = DetalleUiState(
                esPropietarioVerificado = false,
                estaAutenticado = true,
                contenido = DetalleContenido.Listo(chilaquiles),
                ttsState = EstadoLectura(etapa = EtapaLectura.INACTIVO),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            estaAutenticado = true,
            likesCount = 19,
            leDiLike = false,
            esFavorito = true,
            onVolver = {},
            onEditar = {},
            onEliminarClick = {},
            onReintentar = {},
            onReproducirTts = {},
            onContinuarTts = {},
            onRepetirTts = {},
        )
    }

    // ==========================================
    // 17 · Detalle · Propietario · TTS Inactivo (Lista)
    // ==========================================
    @Test
    fun captura17_detallePropietarioTtsInactivo() = mostrarYCapturar("view_17_detalle_propietario_tts_inactivo.png") {
        val tarta = recetasMuestra()[0]
        DetalleContent(
            state = DetalleUiState(
                esPropietarioVerificado = true,
                estaAutenticado = true,
                contenido = DetalleContenido.Listo(tarta),
                ttsState = EstadoLectura(etapa = EtapaLectura.INACTIVO),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            estaAutenticado = true,
            likesCount = 34,
            leDiLike = true,
            esFavorito = true,
            onVolver = {},
            onEditar = {},
            onEliminarClick = {},
            onReintentar = {},
            onReproducirTts = {},
            onContinuarTts = {},
            onRepetirTts = {},
        )
    }

    // ==========================================
    // 18 · Detalle · TTS Leyendo introducción
    // ==========================================
    @Test
    fun captura18_detalleTtsLeyendoIntroduccion() = mostrarYCapturar("view_18_detalle_tts_leyendo_introduccion.png") {
        val tarta = recetasMuestra()[0]
        DetalleContent(
            state = DetalleUiState(
                esPropietarioVerificado = true,
                estaAutenticado = true,
                contenido = DetalleContenido.Listo(tarta),
                ttsState = EstadoLectura(
                    etapa = EtapaLectura.INTRODUCCION,
                    pasoActual = 0,
                    totalPasos = 3,
                    estaReproduciendo = true,
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            estaAutenticado = true,
            likesCount = 34,
            leDiLike = true,
            esFavorito = true,
            onVolver = {},
            onEditar = {},
            onEliminarClick = {},
            onReintentar = {},
            onReproducirTts = {},
            onContinuarTts = {},
            onRepetirTts = {},
        )
    }

    // ==========================================
    // 19 · Detalle · TTS Leyendo paso actual
    // ==========================================
    @Test
    fun captura19_detalleTtsLeyendoPasoActual() = mostrarYCapturar("view_19_detalle_tts_leyendo_paso_actual.png", "redesign_07_detalle_likes_favorito_tts.png") {
        val tarta = recetasMuestra()[0]
        DetalleContent(
            state = DetalleUiState(
                esPropietarioVerificado = true,
                estaAutenticado = true,
                contenido = DetalleContenido.Listo(tarta),
                ttsState = EstadoLectura(
                    etapa = EtapaLectura.PASO,
                    pasoActual = 2,
                    totalPasos = 3,
                    estaReproduciendo = true,
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            estaAutenticado = true,
            likesCount = 34,
            leDiLike = true,
            esFavorito = true,
            onVolver = {},
            onEditar = {},
            onEliminarClick = {},
            onReintentar = {},
            onReproducirTts = {},
            onContinuarTts = {},
            onRepetirTts = {},
        )
    }

    // ==========================================
    // 20 · Detalle · TTS Finalizado
    // ==========================================
    @Test
    fun captura20_detalleTtsFinalizado() = mostrarYCapturar("view_20_detalle_tts_finalizado.png") {
        val chilaquiles = recetasMuestra()[1]
        DetalleContent(
            state = DetalleUiState(
                esPropietarioVerificado = false,
                estaAutenticado = true,
                contenido = DetalleContenido.Listo(chilaquiles),
                ttsState = EstadoLectura(
                    etapa = EtapaLectura.FINALIZADO,
                    pasoActual = 1,
                    totalPasos = 1,
                    estaReproduciendo = false,
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            estaAutenticado = true,
            likesCount = 19,
            leDiLike = false,
            esFavorito = true,
            onVolver = {},
            onEditar = {},
            onEliminarClick = {},
            onReintentar = {},
            onReproducirTts = {},
            onContinuarTts = {},
            onRepetirTts = {},
        )
    }

    // ==========================================
    // 21 · Crear receta · Portada e info general
    // ==========================================
    @Test
    fun captura21_crearRecetaPortadaInfo() = mostrarYCapturar("view_21_crear_receta_portada_info.png", "redesign_08_form_receta.png") {
        FormContent(
            state = FormUiState(
                esEdicion = false,
                titulo = "Panqué de limón y romero",
                descripcion = "Esponjoso con glaseado cítrico de mesa dominical.",
                categoria = "postre",
                tiempoMinutos = "45",
                porciones = "8",
                ingredientes = listOf(IngredienteForm("Harina de trigo", "250", "g")),
                pasos = listOf("Precalentar el horno a 180 °C y engrasar el molde."),
            ),
            onVolver = {},
            onTituloChange = {},
            onDescripcionChange = {},
            onCategoriaChange = {},
            onTiempoChange = {},
            onPorcionesChange = {},
            onIngredienteChange = { _, _ -> },
            onAgregarIngrediente = {},
            onQuitarIngrediente = {},
            onPasoChange = { _, _ -> },
            onAgregarPaso = {},
            onQuitarPaso = {},
            onGuardar = {},
        )
    }

    // ==========================================
    // 22 · Ingredientes, pasos y validaciones
    // ==========================================
    @Test
    fun captura22_ingredientesPasosValidaciones() = mostrarYCapturar("view_22_ingredientes_pasos_validaciones.png") {
        FormContent(
            state = FormUiState(
                esEdicion = false,
                titulo = "Atole de Guayaba y Canela",
                categoria = "bebida",
                tiempoMinutos = "20",
                porciones = "4",
                ingredientes = listOf(
                    IngredienteForm("Guayabas maduras", "500", "g"),
                    IngredienteForm("Piloncillo rallado", "100", "g"),
                    IngredienteForm("Canela en rama", "1", "vara"),
                ),
                pasos = listOf(
                    "Cocer las guayabas en agua hirviendo y licuar.",
                    "Colar y agregar canela con piloncillo a fuego lento.",
                ),
                errores = FormErrores(
                    tiempo = null,
                    ingredientes = null,
                ),
            ),
            onVolver = {},
            onTituloChange = {},
            onDescripcionChange = {},
            onCategoriaChange = {},
            onTiempoChange = {},
            onPorcionesChange = {},
            onIngredienteChange = { _, _ -> },
            onAgregarIngrediente = {},
            onQuitarIngrediente = {},
            onPasoChange = { _, _ -> },
            onAgregarPaso = {},
            onQuitarPaso = {},
            onGuardar = {},
        )
    }

    // ==========================================
    // 23 · Editar receta · Previsualización foto
    // ==========================================
    @Test
    fun captura23_editarRecetaPrevisualizacion() = mostrarYCapturar("view_23_editar_receta_previsualizacion.png") {
        FormContent(
            state = FormUiState(
                esEdicion = true,
                titulo = "Tarta de Fresa y Vainilla",
                descripcion = "Base crujiente de mantequilla con crema suave y fresas frescas.",
                categoria = "postre",
                tiempoMinutos = "50",
                porciones = "8",
                imagenUrlActual = "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                ingredientes = listOf(
                    IngredienteForm("Fresas frescas", "300", "g"),
                    IngredienteForm("Crema para batir", "250", "ml"),
                ),
                pasos = listOf(
                    "Hornear la base de galleta durante 15 minutos a 180 °C.",
                    "Batir la crema con vainilla hasta lograr picos suaves.",
                ),
            ),
            onVolver = {},
            onTituloChange = {},
            onDescripcionChange = {},
            onCategoriaChange = {},
            onTiempoChange = {},
            onPorcionesChange = {},
            onIngredienteChange = { _, _ -> },
            onAgregarIngrediente = {},
            onQuitarIngrediente = {},
            onPasoChange = { _, _ -> },
            onAgregarPaso = {},
            onQuitarPaso = {},
            onGuardar = {},
        )
    }

    // ==========================================
    // 24 · Cierre de sesión · Retorno seguro
    // ==========================================
    @Test
    fun captura24_cierreSesionRetornoSeguro() = mostrarYCapturar("view_24_cierre_sesion_retorno_seguro.png") {
        BienvenidaScreen(
            onExplorarVisitante = {},
            onIniciarSesion = {},
            onCrearCuenta = {},
            mensajeAviso = "Sesión cerrada. Se limpió la pila de navegación privada.",
        )
    }
}
