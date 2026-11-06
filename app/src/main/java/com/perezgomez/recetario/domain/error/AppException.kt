package com.perezgomez.recetario.domain.error

/** Error con un mensaje ya listo para mostrar al usuario. */
class AppException(message: String) : Exception(message)
