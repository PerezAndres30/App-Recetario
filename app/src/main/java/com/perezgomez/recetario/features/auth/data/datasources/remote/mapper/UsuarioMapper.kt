package com.perezgomez.recetario.features.auth.data.datasources.remote.mapper

import com.perezgomez.recetario.features.auth.data.datasources.remote.models.UsuarioDto
import com.perezgomez.recetario.features.auth.domain.entities.Usuario

fun UsuarioDto.toDomain() = Usuario(id = id, nombre = nombre, correo = correo)
