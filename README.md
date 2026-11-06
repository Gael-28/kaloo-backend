# Kaloo Backend (Ktor + SQL Server)

Backend Ktor (CIO) separado del frontend Android. Carpetas:

- `C:\Users\Kevin GM\Documents\kaloo-backend` ← este proyecto (backend)
- `C:\Users\Kevin GM\Documents\kaloo-frontend` ← frontend Android Studio / KMP (`androidApp/`, `shared/`)

## Estructura de paquetes

```
src/main/kotlin/com/kaloo/
├── Application.kt          # module() + main() — punto de entrada
├── plugins/                # plugins Ktor (Http = CORS, Serialization = JSON)
│   ├── Http.kt
│   └── Serialization.kt
├── config/                 # configuración (SQL Server vía JDBC)
│   ├── AppConfig.kt        # env: HOST, PORT, DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
│   └── Database.kt         # DriverManager + mssql-jdbc, healthCheck()
├── models/                 # DTOs serializables
│   ├── User.kt
│   ├── Auth.kt             # LoginRequest / AuthResponse
│   ├── Nutrition.kt        # NutritionRequest / NutritionResponse (IMC)
│   ├── Exercise.kt         # ExerciseRequest / ExerciseResponse
│   ├── Coach.kt            # CoachMessageRequest / ApiResponse
│   └── Establishment.kt    # Establishment / AdminPanelResponse
├── routes/                 # endpoints HTTP
│   ├── Routing.kt          # GET / + agrega módulos
│   ├── AuthRoutes.kt       # POST /login
│   ├── NutritionRoutes.kt  # POST /api/nutrition
│   ├── ExerciseRoutes.kt   # POST /api/exercises
│   ├── CoachRoutes.kt      # POST /api/coaches/message
│   └── AdminRoutes.kt      # /api/admin/panel, /api/admin/establishment
├── repositories/           # acceso a datos (memoria / users.json → SQL Server)
│   ├── UserRepository.kt
│   └── EstablishmentRepository.kt
└── services/               # lógica de negocio
    ├── AuthService.kt
    ├── NutritionService.kt # cálculo IMC
    ├── ExerciseService.kt  # recomendación por objetivo
    └── CoachService.kt
```

## Flujo de una petición

`Route (routes/)` → `Service (services/)` → `Repository (repositories/)` → `Database (config/)` → SQL Server.

Los modelos (`models/`) son los DTOs JSON que usa el frontend Android (Ktor Client).

## SQL Server

Driver JDBC: `com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11` (ver `build.gradle.kts`).
Configuración por variables de entorno (ver `config/AppConfig.kt`) o `database/` para scripts `.sql`.

## Arranque

```powershell
cd C:\Users\Kevin GM\Documents\kaloo-backend
.\gradlew.bat run     # http://0.0.0.0:8080
.\gradlew.bat test    # tests de ServerTest y AdminRoutesTest
```
