package com.perezgomez.recetario.data.mapper

import com.perezgomez.recetario.data.dto.IngredienteDto
import com.perezgomez.recetario.data.dto.RecetaDto
import com.perezgomez.recetario.data.dto.RecetaRequestDto
import com.perezgomez.recetario.domain.model.Ingrediente
import com.perezgomez.recetario.domain.model.Receta

// Único lugar donde se convierte entre DTO y modelo de dominio. Solo lo usan los repositorios.

fun IngredienteDto.toDomain() = Ingrediente(nombre = nombre, cantidad = cantidad, unidad = unidad)

fun Ingrediente.toDto() = IngredienteDto(nombre = nombre, cantidad = cantidad, unidad = unidad)

fun RecetaDto.toDomain() = Receta(
    id = recetaId,
    titulo = titulo,
    descripcion = descripcion,
    categoria = categoria,
    tiempoMinutos = tiempoMinutos,
    porciones = porciones,
    ingredientes = ingredientes.map { it.toDomain() },
    pasos = pasos,
    favorita = favorita,
    creadaEn = creadaEn,
)

fun List<RecetaDto>.toDomainList(): List<Receta> = map { it.toDomain() }

fun Receta.toRequestDto() = RecetaRequestDto(
    titulo = titulo.trim(),
    descripcion = descripcion.trim(),
    categoria = categoria,
    tiempoMinutos = tiempoMinutos,
    porciones = porciones,
    ingredientes = ingredientes.map { it.toDto() },
    pasos = pasos.map { it.trim() },
    favorita = favorita,
)
