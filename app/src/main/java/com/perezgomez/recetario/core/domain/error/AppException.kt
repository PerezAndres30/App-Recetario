package com.perezgomez.recetario.core.domain.error

/** Error con un mensaje ya listo para mostrar al usuario. */
open class AppException(message: String) : Exception(message)

/** Error HTTP 401: token ausente, inválido o expirado en el servidor. */
class UnauthorizedException(message: String) : AppException(message)
