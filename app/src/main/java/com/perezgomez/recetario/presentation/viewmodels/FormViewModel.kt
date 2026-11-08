package com.perezgomez.recetario.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perezgomez.recetario.domain.model.Ingrediente
import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.repository.RecetaRepository
import com.perezgomez.recetario.domain.validation.Validadores
import com.perezgomez.recetario.presentation.navigation.Rutas
import com.perezgomez.recetario.presentation.screens.FormErrores
import com.perezgomez.recetario.presentation.screens.FormUiState
import com.perezgomez.recetario.presentation.screens.IngredienteForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Un solo ViewModel para crear y editar: si llega un id por navegación, precarga la receta. */
class FormViewModel(
    private val recetaRepository: RecetaRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recetaId: String? = savedStateHandle[Rutas.ARG_ID]
    private var creadaEn: String = ""

    private val _uiState = MutableStateFlow(
        FormUiState(esEdicion = recetaId != null, cargando = recetaId != null),
    )
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    init {
        if (recetaId != null) cargarReceta(recetaId)
    }

    fun cargarReceta(id: String = recetaId.orEmpty()) {
        if (id.isEmpty()) return
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            recetaRepository.obtener(id)
                .onSuccess { receta ->
                    creadaEn = receta.creadaEn
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            titulo = receta.titulo,
                            descripcion = receta.descripcion,
                            categoria = receta.categoria,
                            tiempoMinutos = receta.tiempoMinutos.toString(),
                            porciones = receta.porciones.toString(),
                            ingredientes = receta.ingredientes
                                .map { i -> IngredienteForm(i.nombre, i.cantidad, i.unidad) }
                                .ifEmpty { listOf(IngredienteForm()) },
                            pasos = receta.pasos.ifEmpty { listOf("") },
                            favorita = receta.favorita,
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(cargando = false, error = e.message) } }
        }
    }

    // ---- eventos de la UI -------------------------------------------------

    fun onTituloChange(valor: String) =
        _uiState.update { it.copy(titulo = valor, errores = it.errores.copy(titulo = null)) }

    fun onDescripcionChange(valor: String) =
        _uiState.update { it.copy(descripcion = valor, errores = it.errores.copy(descripcion = null)) }

    fun onCategoriaChange(valor: String) =
        _uiState.update { it.copy(categoria = valor, errores = it.errores.copy(categoria = null)) }

    fun onTiempoChange(valor: String) =
        _uiState.update {
            it.copy(tiempoMinutos = valor.filter(Char::isDigit), errores = it.errores.copy(tiempo = null))
        }

    fun onPorcionesChange(valor: String) =
        _uiState.update {
            it.copy(porciones = valor.filter(Char::isDigit), errores = it.errores.copy(porciones = null))
        }

    fun onFavoritaChange(valor: Boolean) = _uiState.update { it.copy(favorita = valor) }

    fun onIngredienteChange(indice: Int, ingrediente: IngredienteForm) =
        _uiState.update {
            it.copy(
                ingredientes = it.ingredientes.mapIndexed { i, actual -> if (i == indice) ingrediente else actual },
                errores = it.errores.copy(ingredientes = null),
            )
        }

    fun onAgregarIngrediente() =
        _uiState.update { it.copy(ingredientes = it.ingredientes + IngredienteForm()) }

    fun onQuitarIngrediente(indice: Int) =
        _uiState.update {
            if (it.ingredientes.size <= 1) it
            else it.copy(ingredientes = it.ingredientes.filterIndexed { i, _ -> i != indice })
        }

    fun onPasoChange(indice: Int, texto: String) =
        _uiState.update {
            it.copy(
                pasos = it.pasos.mapIndexed { i, actual -> if (i == indice) texto else actual },
                errores = it.errores.copy(pasos = null),
            )
        }

    fun onAgregarPaso() = _uiState.update { it.copy(pasos = it.pasos + "") }

    fun onQuitarPaso(indice: Int) =
        _uiState.update {
            if (it.pasos.size <= 1) it else it.copy(pasos = it.pasos.filterIndexed { i, _ -> i != indice })
        }

    fun onGuardar() {
        val actual = _uiState.value
        if (actual.guardando || actual.cargando) return

        val errores = validar(actual)
        if (errores.hayErrores()) {
            _uiState.update { it.copy(errores = errores, error = null) }
            return
        }

        val receta = Receta(
            id = recetaId.orEmpty(),
            titulo = actual.titulo.trim(),
            descripcion = actual.descripcion.trim(),
            categoria = actual.categoria,
            tiempoMinutos = actual.tiempoMinutos.trim().toInt(),
            porciones = actual.porciones.trim().toInt(),
            ingredientes = actual.ingredientes
                .filter { it.nombre.isNotBlank() }
                .map { Ingrediente(it.nombre.trim(), it.cantidad.trim(), it.unidad.trim()) },
            pasos = actual.pasos.filter { it.isNotBlank() },
            favorita = actual.favorita,
            creadaEn = creadaEn,
        )

        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            val resultado = if (recetaId == null) {
                recetaRepository.crear(receta)
            } else {
                recetaRepository.actualizar(receta)
            }
            resultado
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            guardado = true,
                            mensajeExito = if (recetaId == null) "Receta creada" else "Receta actualizada",
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(guardando = false, error = e.message) } }
        }
    }

    // ---- validación -------------------------------------------------------

    private fun validar(s: FormUiState) = FormErrores(
        titulo = Validadores.obligatorio(s.titulo, "El título")
            ?: Validadores.longitudMaxima(s.titulo.trim(), "El título", 120),
        descripcion = Validadores.longitudMaxima(s.descripcion.trim(), "La descripción", 1000),
        categoria = if (s.categoria.isBlank()) "Elige una categoría" else null,
        tiempo = Validadores.enteroEnRango(s.tiempoMinutos, "El tiempo", 0, 100000),
        porciones = Validadores.enteroEnRango(s.porciones, "Las porciones", 1, 1000),
        ingredientes = if (s.ingredientes.none { it.nombre.isNotBlank() }) "Agrega al menos un ingrediente" else null,
        pasos = if (s.pasos.none { it.isNotBlank() }) "Agrega al menos un paso" else null,
    )
}
