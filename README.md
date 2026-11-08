# Recetario · App CRUD con autenticación (Unidad 3)

App Android nativa en **Kotlin + Jetpack Compose (Material 3)** que consume la API REST en AWS
(`recetario-api`). Registro, login, sesión persistente y CRUD completo de **Recetas**.

## Cómo abrirla

1. Android Studio → **Open** → selecciona esta carpeta (`recetario-android`).
2. Espera el *Gradle Sync* (la primera vez descarga dependencias y Gradle 8.9).
3. Elige un emulador y pulsa **Run**.

Si despliegas de nuevo la API con `sam deploy` sobre el mismo stack, la URL no cambia. El registro y el login
usan `POST /register` y `POST /login` de la API, que guarda los usuarios en su propia base de datos (DynamoDB).

La URL de la API está en `app/build.gradle.kts` (`API_BASE_URL`). Si vuelves a desplegar y cambia, edítala ahí
(debe terminar en `/`). La API usa HTTPS, así que no hace falta habilitar tráfico en texto plano.

## Arquitectura

```
presentation/  screens, viewmodels, components     ← Compose + MVVM, solo conoce domain
domain/        modelos, interfaces, validaciones   ← Kotlin puro, sin Android ni Retrofit
data/          dto, datasource, mapper, repository ← implementa domain
di/            AppContainer (inyección manual)
```

```
Composable ─eventos→ ViewModel ─→ Repository (interfaz, domain)
     ▲                  │                 │
     └──── UiState ─────┘          RepositoryImpl → ApiCaller → ApiService (Retrofit) → API
                                         │
                                  Mapper (DTO ⇄ dominio)
```

| Requisito de la rúbrica | Dónde está |
|---|---|
| Registro y login contra la API | `data/datasource/AuthApiService`, `data/repository/AuthRepositoryImpl`, `presentation/screens/Login*`, `Register*` |
| Validaciones (vacíos, correo, 8 caracteres, confirmación) | `domain/validation/Validadores` usadas desde los ViewModels |
| Token tras una abstracción + interceptor | `domain/session/SessionStorage`, `data/local/SharedPrefsSessionStorage`, `data/remote/AuthInterceptor` |
| Sesión persiste / cerrar sesión sin volver con Atrás | `MainActivity` (destino inicial), `HomeViewModel.onCerrarSesion`, `presentation/navigation/AppNavHost` (vacía la pila) |
| Listar con `LazyColumn`, estados de carga / vacío / error + reintentar | `presentation/screens/HomeScreen`, `HomeUiState` (`ListaUi`) |
| Detalle con todos los atributos | `presentation/screens/DetalleScreen`, `DetalleViewModel` |
| Crear y editar con un solo formulario validado | `presentation/screens/FormScreen`, `FormViewModel` (recibe el id opcional) |
| Eliminar con diálogo de confirmación | `presentation/components/ConfirmarEliminarDialog`, usado en `HomeScreen` y `DetalleScreen` |
| Retroalimentación (Snackbar / navegación) | `HomeScreen`, `DetalleScreen`, `volverConMensaje` en `AppNavHost` |
| MVVM: `UiState` inmutable, `MutableStateFlow` privado, `collectAsStateWithLifecycle` | `presentation/viewmodels/*`, `presentation/screens/*` (`…UiState` en su propio archivo) |
| Repository + DTO + Mapper | `domain/repository`, `data/repository`, `data/dto`, `data/mapper/*Mapper.kt` |
| DI manual | `di/AppContainer` (creado una vez en `RecetarioApp`), `presentation/viewmodels/AppViewModelFactory` (`viewModelFactory { initializer { … } }`) |
| Version Catalog | `gradle/libs.versions.toml` |

Sin Hilt, Dagger ni Koin.

## Recurso del dominio: Receta

`titulo` (texto), `descripcion`, `categoria`, `tiempoMinutos` (número), `porciones` (número),
`favorita` (booleano), `ingredientes` (lista) y `pasos` (lista).
Validaciones: título obligatorio (máx. 120), categoría obligatoria, tiempo 0–100000, porciones 1–1000,
al menos un ingrediente y un paso.

## Pruebas rápidas

1. Regístrate con un correo nuevo y una contraseña de 8 caracteres o más (o inicia sesión si ya tienes cuenta).
2. Crea una receta con el botón **+**; aparece en la lista con un mensaje de confirmación.
3. Toca una receta para ver el detalle; desde ahí puedes editarla o eliminarla.
4. Cierra la app por completo y ábrela: entra directo a Home (la sesión persiste).
5. Usa el ícono de salida: regresa a Login y el botón Atrás ya no vuelve a Home.

## API

La app consume la API de este proyecto (`recetario-api`: API Gateway + Lambda + DynamoDB), con las rutas
`POST /register`, `POST /login` y el CRUD de `/recetas` (requiere `Authorization: Bearer <token>`).
Las instrucciones de despliegue están en el README de esa API.
