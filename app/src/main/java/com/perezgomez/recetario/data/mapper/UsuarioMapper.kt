package com.perezgomez.recetario.data.mapper

import com.perezgomez.recetario.data.dto.UsuarioDto
import com.perezgomez.recetario.domain.model.Usuario

fun UsuarioDto.toDomain() = Usuario(id = id, nombre = nombre, correo = correo)
