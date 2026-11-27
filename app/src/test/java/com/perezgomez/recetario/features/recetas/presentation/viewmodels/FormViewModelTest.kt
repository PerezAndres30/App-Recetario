package com.perezgomez.recetario.features.recetas.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.perezgomez.recetario.core.navigation.Rutas
import com.perezgomez.recetario.features.auth.domain.entities.Usuario
import com.perezgomez.recetario.features.recetas.domain.entities.CambioImagen
import com.perezgomez.recetario.features.recetas.domain.entities.Ingrediente
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.presentation.screens.IngredienteForm
import com.perezgomez.recetario.util.FakeAuthRepository
import com.perezgomez.recetario.util.FakeImagenRepository
import com.perezgomez.recetario.util.FakeProcesadorImagen
import com.perezgomez.recetario.util.FakeRecetaRepository
import com.perezgomez.recetario.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var recetaRepository: FakeRecetaRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var imagenRepository: FakeImagenRepository
    private lateinit var procesadorImagen: FakeProcesadorImagen

    private val recetaExistente = Receta(
        id = "receta-42",
        titulo = "Tacos al Pastor",
        descripcion = "Tradicionales y marinados",
        categoria = "comida",
        tiempoMinutos = 45,
        porciones = 4,
        ingredientes = listOf(Ingrediente("Carne de cerdo", "1", "kg")),
        pasos = listOf("Marinar la carne", "Asar al trompo"),
        favorita = true,
        creadaEn = "2026-11-20T10:00:00Z",
    )

    @Before
    fun setUp() {
        recetaRepository = FakeRecetaRepository()
        imagenRepository = FakeImagenRepository()
        procesadorImagen = FakeProcesadorImagen()
        authRepository = FakeAuthRepository(Usuario("user-1", "Chef Ivan", "ivan@test.com"))
    }

    @Test
    fun modoCreacion_inicializaEstadoVacioYNoEdicion() {
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            imagenRepository = imagenRepository,
            procesadorImagen = procesadorImagen,
            savedStateHandle = SavedStateHandle(),
        )

        val state = viewModel.uiState.value
        assertFalse(state.esEdicion)
        assertFalse(state.cargando)
        assertEquals("", state.titulo)
        assertEquals("", state.descripcion)
        assertEquals("1", state.porciones)
        assertEquals(1, state.ingredientes.size)
        assertEquals(1, state.pasos.size)
    }

    @Test
    fun modoEdicion_precargaDatosDeRecetaExistente() = runTest {
        recetaRepository.recetas.add(recetaExistente)

        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            imagenRepository = imagenRepository,
            procesadorImagen = procesadorImagen,
            savedStateHandle = SavedStateHandle(mapOf(Rutas.ARG_ID to "receta-42")),
        )

        assertTrue(viewModel.uiState.value.esEdicion)

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.cargando)
        assertEquals("Tacos al Pastor", state.titulo)
        assertEquals("Tradicionales y marinados", state.descripcion)
        assertEquals("comida", state.categoria)
        assertEquals("45", state.tiempoMinutos)
        assertEquals("4", state.porciones)
        assertEquals(1, state.ingredientes.size)
        assertEquals("Carne de cerdo", state.ingredientes[0].nombre)
        assertEquals(2, state.pasos.size)
        assertTrue(state.favorita)
    }

    @Test
    fun onGuardar_conCamposVacios_muestraErroresYNoLlamaRepositorio() = runTest {
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            imagenRepository = imagenRepository,
            procesadorImagen = procesadorImagen,
            savedStateHandle = SavedStateHandle(),
        )

        viewModel.onGuardar()

        val state = viewModel.uiState.value
        assertFalse(state.guardando)
        assertFalse(state.guardado)
        assertTrue(state.errores.hayErrores())
        assertNotNull(state.errores.titulo)
        assertNotNull(state.errores.categoria)
        assertEquals(0, recetaRepository.llamadasCrear)
    }

    @Test
    fun onGuardar_sinAutenticacion_muestraErrorYNoLlamaRepositorio() = runTest {
        val authSinSesion = FakeAuthRepository(null)
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            authRepository = authSinSesion,
            imagenRepository = imagenRepository,
            procesadorImagen = procesadorImagen,
            savedStateHandle = SavedStateHandle(),
        )

        viewModel.onTituloChange("Platillo")
        viewModel.onCategoriaChange("comida")
        viewModel.onTiempoChange("10")
        viewModel.onPorcionesChange("1")
        viewModel.onPasoChange(0, "Cocinar")
        val ing = viewModel.uiState.value.ingredientes[0]
        viewModel.onIngredienteChange(0, ing.copy(nombre = "Ingrediente"))

        viewModel.onGuardar()

        val state = viewModel.uiState.value
        assertFalse(state.guardando)
        assertFalse(state.guardado)
        assertEquals("Debes iniciar sesión para crear o editar recetas.", state.error)
        assertEquals(0, recetaRepository.llamadasCrear)
    }

    @Test
    fun onGuardar_bloqueaGuardadosDuplicadosMientrasGuarda() = runTest {
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            imagenRepository = imagenRepository,
            procesadorImagen = procesadorImagen,
            savedStateHandle = SavedStateHandle(),
        )

        viewModel.onTituloChange("Ensalada César")
        viewModel.onCategoriaChange("comida")
        viewModel.onTiempoChange("20")
        viewModel.onPorcionesChange("2")
        viewModel.onPasoChange(0, "Mezclar lechuga con aderezo")
        val ing = viewModel.uiState.value.ingredientes[0]
        viewModel.onIngredienteChange(0, ing.copy(nombre = "Lechuga romana"))

        viewModel.onGuardar()
        assertTrue(viewModel.uiState.value.guardando)

        viewModel.onGuardar()

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, recetaRepository.llamadasCrear)
        assertFalse(viewModel.uiState.value.guardando)
        assertTrue(viewModel.uiState.value.guardado)
        assertEquals("Receta creada", viewModel.uiState.value.mensajeExito)
    }

    @Test
    fun onGuardar_enModoEdicion_llamaActualizarYMensajeEsRecetaActualizada() = runTest {
        recetaRepository.recetas.add(recetaExistente)

        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            imagenRepository = imagenRepository,
            procesadorImagen = procesadorImagen,
            savedStateHandle = SavedStateHandle(mapOf(Rutas.ARG_ID to "receta-42")),
        )
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTituloChange("Tacos al Pastor Especiales")
        viewModel.onGuardar()

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.guardando)
        assertTrue(state.guardado)
        assertEquals("Receta actualizada", state.mensajeExito)
        assertEquals(1, recetaRepository.llamadasActualizar)
        assertEquals(0, recetaRepository.llamadasCrear)
    }

    @Test
    fun gestionDeIngredientesYPasos_agregarYQuitarModificaListas() {
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            imagenRepository = imagenRepository,
            procesadorImagen = procesadorImagen,
            savedStateHandle = SavedStateHandle(),
        )

        viewModel.onAgregarIngrediente()
        assertEquals(2, viewModel.uiState.value.ingredientes.size)

        viewModel.onIngredienteChange(1, IngredienteForm("Sal", "1", "cdta"))
        assertEquals("Sal", viewModel.uiState.value.ingredientes[1].nombre)

        viewModel.onQuitarIngrediente(0)
        assertEquals(1, viewModel.uiState.value.ingredientes.size)
        assertEquals("Sal", viewModel.uiState.value.ingredientes[0].nombre)

        viewModel.onQuitarIngrediente(0)
        assertEquals(1, viewModel.uiState.value.ingredientes.size)

        viewModel.onAgregarPaso()
        assertEquals(2, viewModel.uiState.value.pasos.size)

        viewModel.onPasoChange(1, "Servir caliente")
        assertEquals("Servir caliente", viewModel.uiState.value.pasos[1])

        viewModel.onQuitarPaso(0)
        assertEquals(1, viewModel.uiState.value.pasos.size)
        assertEquals("Servir caliente", viewModel.uiState.value.pasos[0])

        viewModel.onQuitarPaso(0)
        assertEquals(1, viewModel.uiState.value.pasos.size)
    }

    @Test
    fun cambioDeFavorita_actualizaEstado() {
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            imagenRepository = imagenRepository,
            procesadorImagen = procesadorImagen,
            savedStateHandle = SavedStateHandle(),
        )

        assertFalse(viewModel.uiState.value.favorita)
        viewModel.onFavoritaChange(true)
        assertTrue(viewModel.uiState.value.favorita)
    }

    // ---- fotos ------------------------------------------------------------

    private fun nuevaConDatosValidos(): FormViewModel {
        val vm = FormViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            imagenRepository = imagenRepository,
            procesadorImagen = procesadorImagen,
            savedStateHandle = SavedStateHandle(),
        )
        vm.onTituloChange("Sopa")
        vm.onCategoriaChange("comida")
        vm.onTiempoChange("10")
        vm.onIngredienteChange(0, IngredienteForm("Agua", "1", "l"))
        vm.onPasoChange(0, "Hervir")
        return vm
    }

    @Test
    fun fotoElegida_quedaComoVistaPrevia() = runTest {
        val vm = nuevaConDatosValidos()
        vm.onFotoElegida("content://foto")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(vm.uiState.value.imagenLocal)
        assertFalse(vm.uiState.value.procesandoImagen)
    }

    @Test
    fun fotoConError_muestraMensajeYNoGuardaVistaPrevia() = runTest {
        procesadorImagen.deberiaFallar = true
        val vm = nuevaConDatosValidos()
        vm.onFotoElegida("content://foto")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(null, vm.uiState.value.imagenLocal)
        assertEquals("No se pudo preparar la foto", vm.uiState.value.error)
    }

    @Test
    fun crearConFoto_guardaTextoPrimeroYDespuesSubeYAsociaLaFoto() = runTest {
        val vm = nuevaConDatosValidos()
        vm.onFotoElegida("content://foto")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        vm.onGuardar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, recetaRepository.llamadasCrear)
        assertEquals(1, imagenRepository.subidas.size)
        assertEquals("id-1", imagenRepository.subidas[0].first)
        assertTrue(recetaRepository.ultimoCambioImagen is CambioImagen.Establecer)
        assertNotNull(recetaRepository.recetas[0].imagenUrl)
        assertTrue(vm.uiState.value.guardado)
        assertEquals("Receta creada", vm.uiState.value.mensajeExito)
    }

    @Test
    fun crearConFoto_siLaSubidaFalla_laRecetaQuedaCreadaSinFoto() = runTest {
        imagenRepository.deberiaFallar = true
        val vm = nuevaConDatosValidos()
        vm.onFotoElegida("content://foto")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        vm.onGuardar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, recetaRepository.recetas.size)
        assertEquals(null, recetaRepository.recetas[0].imagenUrl)
        assertTrue(vm.uiState.value.guardado)
        assertTrue(vm.uiState.value.mensajeExito.contains("foto no se pudo subir"))
    }

    @Test
    fun crearSinFoto_noSubeNada() = runTest {
        val vm = nuevaConDatosValidos()
        vm.onGuardar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(imagenRepository.subidas.isEmpty())
        assertTrue(vm.uiState.value.guardado)
    }

    private fun edicion(conFoto: Boolean): FormViewModel {
        recetaRepository.recetas.add(
            recetaExistente.copy(
                esPropia = true,
                imagenUrl = if (conFoto) "https://res.cloudinary.com/demo/image/upload/recetario/recetas/receta-42.jpg" else null,
                imagenPublicId = if (conFoto) "recetario/recetas/receta-42" else null,
            ),
        )
        return FormViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            imagenRepository = imagenRepository,
            procesadorImagen = procesadorImagen,
            savedStateHandle = SavedStateHandle(mapOf(Rutas.ARG_ID to "receta-42")),
        )
    }

    @Test
    fun editarSinTocarLaFoto_laMantiene() = runTest {
        val vm = edicion(conFoto = true)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        vm.onGuardar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CambioImagen.Mantener, recetaRepository.ultimoCambioImagen)
        assertNotNull(recetaRepository.recetas[0].imagenUrl)
    }

    @Test
    fun editarQuitandoLaFoto_enviaQuitar() = runTest {
        val vm = edicion(conFoto = true)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        vm.onQuitarFoto()
        vm.onGuardar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CambioImagen.Quitar, recetaRepository.ultimoCambioImagen)
        assertEquals(null, recetaRepository.recetas[0].imagenUrl)
    }

    @Test
    fun editarConFotoNueva_subeYReemplaza() = runTest {
        val vm = edicion(conFoto = false)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        vm.onFotoElegida("content://nueva")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        vm.onGuardar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("receta-42", imagenRepository.subidas[0].first)
        assertTrue(recetaRepository.ultimoCambioImagen is CambioImagen.Establecer)
        assertTrue(vm.uiState.value.guardado)
    }

    @Test
    fun editarConFotoNueva_siFallaLaSubida_noGuardaNadaYPermiteReintentar() = runTest {
        imagenRepository.deberiaFallar = true
        val vm = edicion(conFoto = false)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        vm.onFotoElegida("content://nueva")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        vm.onGuardar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, recetaRepository.llamadasActualizar)
        assertFalse(vm.uiState.value.guardado)
        assertFalse(vm.uiState.value.guardando)
        assertNotNull(vm.uiState.value.error)
        assertNotNull(vm.uiState.value.imagenLocal)
    }
}
