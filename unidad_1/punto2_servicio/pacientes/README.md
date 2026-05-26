# Pacientes API

Servicio Spring Boot para consultar pacientes, sus habitos y una prediccion simple de nivel de enfermedad basada en la cantidad de habitos no saludables asociados a cada paciente.

El proyecto usa Spring Boot, Spring Data JPA, H2 en memoria y Springdoc OpenAPI para exponer la documentacion Swagger.

## Contexto

Al iniciar la aplicacion se autocarga informacion de prueba:

- 563 pacientes.
- 10 habitos, entre saludables y no saludables.
- Relaciones entre pacientes y habitos.

La prediccion se calcula contando solo los habitos no saludables:

- `0` habitos malos: `NO ENFERMO`
- `1` habito malo: `ENFERMEDAD LEVE`
- `2` habitos malos: `ENFERMEDAD AGUDA`
- `3` habitos malos: `ENFERMEDAD CRÓNICA`
- `3+` habitos malos y edad mayor o igual a 70: `ENFERMEDAD TERMINAL`

## Servicios

Base URL local con Docker:

```text
http://localhost:8081
```

Swagger UI:

```text
http://localhost:8081/swagger-ui/index.html
```

### 1. Resumen de predicciones

Retorna la cantidad de pacientes por nivel de enfermedad.

```http
POST /api/predecir
```

Ejemplo de consumo:

```bash
curl -X POST "http://localhost:8081/api/predecir" \
  -H "Content-Type: application/json" \
  -d '{
    "edad": [18, 90],
    "habitos": []
  }'
```

Respuesta esperada:

```json
[
  { "NO ENFERMO": 113 },
  { "ENFERMEDAD LEVE": 113 },
  { "ENFERMEDAD AGUDA": 113 },
  { "ENFERMEDAD CRÓNICA": 160 },
  { "ENFERMEDAD TERMINAL": 64 }
]
```

### 2. Detalle de prediccion por paciente

Retorna los pacientes filtrados con sus datos, habitos y prediccion individual.

```http
POST /api/predecir/detalle
```

Ejemplo de consumo:

```bash
curl -X POST "http://localhost:8081/api/predecir/detalle" \
  -H "Content-Type: application/json" \
  -d '{
    "edad": [25, 60],
    "habitos": ["Actividad fisica regular"]
  }'
```

Respuesta esperada:

```json
[
  {
    "primerNombre": "Maria",
    "segundoNombre": "Fernanda",
    "primerApellido": "Garcia",
    "segundoApellido": "Jimenez",
    "genero": "Femenino",
    "edad": 34,
    "habitos": [
      "Actividad fisica regular",
      "Dormir entre 7 y 8 horas",
      "Chequeos medicos preventivos"
    ],
    "prediccion": "NO ENFERMO"
  }
]
```

### 3. Reporte historico de predicciones

Retorna las estadisticas acumuladas de las predicciones realizadas desde que el archivo de reporte existe.

```http
GET /api/predecir/reporte
```

Ejemplo de consumo:

```bash
curl -X GET "http://localhost:8081/api/predecir/reporte"
```

Respuesta esperada:

```json
{
  "totalPorCategoria": {
    "NO ENFERMO": 113,
    "ENFERMEDAD LEVE": 113,
    "ENFERMEDAD AGUDA": 113,
    "ENFERMEDAD CRÓNICA": 160,
    "ENFERMEDAD TERMINAL": 64
  },
  "ultimasPredicciones": [
    {
      "fecha": "2026-05-16T10:30:15.123",
      "paciente": "Carlos Felipe Martinez Reyes",
      "genero": "Masculino",
      "edad": 82,
      "prediccion": "ENFERMEDAD TERMINAL"
    }
  ],
  "fechaUltimaPrediccion": "2026-05-16T10:30:15.123"
}
```

El historial se guarda en un archivo de texto configurable con la variable `PACIENTES_REPORTE_PATH`. En Docker se usa por defecto:

```text
/tmp/pacientes/predicciones.log
```

## Filtros disponibles

El cuerpo de la solicitud acepta:

```json
{
  "edad": [18, 90],
  "habitos": ["Actividad fisica regular", "Consumo de tabaco"]
}
```

- `edad`: lista con edad minima y edad maxima.
- `habitos`: lista de nombres de habitos para filtrar pacientes relacionados.

Para consultar todos los pacientes, se puede enviar:

```json
{
  "edad": [],
  "habitos": []
}
```

## Levantar con Docker

Construir la imagen:

```bash
docker compose build
```

Levantar el servicio:

```bash
docker compose up -d
```

Ver estado:

```bash
docker compose ps
```

Ver logs:

```bash
docker compose logs -f pacientes-api
```

Detener el servicio:

```bash
docker compose down
```

## Puertos

El contenedor expone internamente el puerto `8080`, pero el compose publica el servicio en el puerto local `8081`:

```text
localhost:8081 -> container:8080
```

## Base de datos

La aplicacion usa H2 en memoria. Los datos se crean automaticamente al iniciar el servicio, por lo que al detener y recrear el contenedor se vuelve a cargar la informacion inicial.
