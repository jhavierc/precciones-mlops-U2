# Pipeline de ML para Predicción de Enfermedades (Comunes y Huérfanas)

## Descripción del Problema

Se requiere un sistema capaz de predecir, a partir de síntomas de un paciente, si este podría padecer una enfermedad determinada. El desafío central es dual: existen enfermedades comunes con abundantes datos etiquetados, pero también enfermedades huérfanas (raras) con muy pocos registros disponibles. El pipeline debe manejar ambos escenarios de forma robusta.

---

## 1. Diseño

### 1.1 Restricciones y Limitaciones

| Dimensión | Restricción |
|---|---|
| **Datos desbalanceados** | Las enfermedades huérfanas tienen pocos casos; las comunes pueden dominar el entrenamiento |
| **Privacidad y regulación** | Los datos clínicos están sujetos a normativas (HIPAA, GDPR, Ley 1581 en Colombia). Requieren anonimización y consentimiento |
| **Calidad de datos** | Registros incompletos, errores de digitación, terminología no estandarizada (ej. síntomas descritos con sinónimos) |
| **Sesgo clínico** | Los datos históricos pueden reflejar sesgos de diagnóstico previos (subdiagnóstico de enfermedades huérfanas) |
| **Interpretabilidad** | En contexto médico, los modelos deben ser explicables para los profesionales de salud |
| **Latencia** | Si se usa en consulta, la predicción debe ser en tiempo real (< 2 segundos) |

### 1.2 Tipos de Datos

- **Síntomas estructurados**: variables categóricas o binarias (presencia/ausencia de síntoma)
- **Texto clínico libre**: notas de consulta, anamnesis (requiere NLP)
- **Datos demográficos**: edad, sexo, historial familiar
- **Resultados de laboratorio**: valores numéricos continuos
- **Imágenes médicas** (opcional): radiografías, ecografías (requiere CV)
- **Ontologías médicas**: HPO (Human Phenotype Ontology), ICD-10/11, OMIM para enfermedades huérfanas

---

## 2. Desarrollo

### 2.1 Fuentes de Datos y Manejo

```
Fuentes externas:
  - Orphanet / OMIM          → enfermedades huérfanas y fenotipos asociados
  - HPO (Human Phenotype Ontology) → síntomas estandarizados
  - PubMed / bases clínicas  → literatura para síntesis de datos
  - Registros hospitalarios  → casos reales (previa anonimización)
  - Kaggle / UCI Medical     → datasets públicos de enfermedades comunes
```

**Estrategia de manejo de datos escasos (enfermedades huérfanas):**
- **Data augmentation**: generar instancias sintéticas con SMOTE, ADASYN o GANs médicas
- **Transfer learning**: pre-entrenar con enfermedades comunes, afinar con datos de enfermedades huérfanas (few-shot learning)
- **Knowledge graphs**: representar relaciones síntoma-enfermedad usando grafos (Neo4j + embeddings)
- **Weak supervision**: uso de ontologías médicas como fuente de etiquetado débil (Snorkel)

### 2.2 Preprocesamiento

```
Ingesta cruda
    → Validación de esquema (Great Expectations)
    → Anonimización (reemplazar PII)
    → Normalización de síntomas (mapeo a HPO terms)
    → Imputación de valores faltantes (KNN imputer o MICE)
    → Codificación (One-Hot para síntomas categóricos)
    → Escalado de variables numéricas (StandardScaler / MinMaxScaler)
    → Partición: train / validation / test (estratificada por clase)
```

### 2.3 Modelos de ML Propuestos

Se propone una arquitectura por capas según disponibilidad de datos:

**Capa 1 — Enfermedades comunes (datos abundantes):**
- Random Forest / XGBoost: alto rendimiento, buena interpretabilidad con SHAP
- Red neuronal densa (MLP): captura relaciones no lineales complejas
- LightGBM: eficiente en grandes volúmenes

**Capa 2 — Enfermedades huérfanas (datos escasos):**
- Few-shot learning con Prototypical Networks o Siamese Networks
- Fine-tuning de modelos pre-entrenados en datos comunes
- Clasificadores bayesianos: cuantifican incertidumbre, útil con pocos datos

**Capa 3 — Ensemble / Router:**
- Un meta-modelo determina si el caso es candidato a enfermedad común u huérfana, y enruta al sub-modelo adecuado
- Calibración de probabilidades (Platt scaling / isotonic regression)

### 2.4 Validación y Testing

| Estrategia | Descripción |
|---|---|
| **Cross-validation estratificada** | K-Fold preservando proporciones de clases raras |
| **Métricas principales** | F1-macro, AUC-ROC, precision@K, recall para clases minoritarias |
| **Pruebas de equidad** | Verificar que el modelo no discrimine por demografía |
| **Pruebas de robustez** | Ruido en síntomas de entrada, síntomas faltantes |
| **Validación clínica** | Revisión con médicos especialistas antes de producción |
| **Backtesting** | Evaluar sobre casos históricos con diagnóstico confirmado |

---

## 3. Producción

### 3.1 Despliegue

```
Flujo de despliegue:

  [Código + Modelo entrenado]
         |
         v
  [Dockerfile → imagen Docker]
         |
         v
  [DockerHub — registro de imágenes versionadas]
    (ej. usuario/diagnostico-ml:v1.2.0)
         |
         v
  [Servicio Cloud (AWS ECS / GCP Cloud Run / Azure Container Apps)]
    → Pull automático desde DockerHub
    → Contenedor con API REST (FastAPI)
    → Escalado automático según demanda
         |
         v
  [API Gateway — HTTPS endpoint público]
         |
         +-----------+-----------+
         |                       |
  [App Web (React)]     [App Móvil (Flutter/React Native)]
```

**Flujo detallado de CI/CD:**
1. El modelo entrenado y el servidor FastAPI se empaquetan en una imagen Docker
2. La imagen se etiqueta con la versión del modelo y se publica en DockerHub (`usuario/diagnostico-ml:v1.x.x`)
3. El servicio cloud detecta la nueva imagen (webhook o pipeline CI/CD) y realiza el despliegue sin tiempo de inactividad (rolling update)
4. La app web y la app móvil consumen el mismo endpoint REST expuesto por el API Gateway

**Características del servicio cloud:**
- Escalado automático horizontal según carga de solicitudes
- Variables de entorno para configuración (umbral de confianza, versión de modelo activa)
- Health check endpoint para reinicio automático ante fallos
- Se implementa versionado de modelos: producción, staging, shadow
- **Shadow mode**: el nuevo modelo recibe tráfico real pero no sirve la respuesta al usuario; se comparan sus predicciones con el modelo en producción para validación silenciosa

### 3.2 Monitoreo

El monitoreo es crítico dado el impacto clínico del sistema:

**Monitoreo de datos (data drift):**
- Detectar cambios en la distribución de síntomas de entrada (Kolmogorov-Smirnov, PSI)
- Alertas si la distribución de clases predichas cambia significativamente
- Herramienta: Evidently AI / Whylogs

**Monitoreo de modelo (model drift):**
- Seguimiento de métricas en producción contra un conjunto de evaluación continuo
- Alertas si el F1-score cae por debajo de un umbral definido
- Latencia de predicción (P95, P99)

**Monitoreo de negocio / clínico:**
- Tasa de confirmación diagnóstica (feedback de médicos cuando está disponible)
- Proporción de predicciones de alta confianza vs. baja confianza
- Casos donde el modelo predice "sin diagnóstico claro" (señal de posible enfermedad huérfana no catalogada)

**Stack de observabilidad:**
```
Prometheus + Grafana  → métricas de infraestructura y modelo
ELK Stack             → logs de predicciones y errores
PagerDuty / Opsgenie  → alertas y on-call
MLflow                → tracking de experimentos y versiones
```

### 3.3 Re-entrenamiento y Mejora Continua

**Disparadores de re-entrenamiento:**
- Detección de data drift (umbral de PSI > 0.2)
- Degradación de métricas en producción (caída > 5% en F1-macro)
- Nuevas enfermedades catalogadas en Orphanet o CIE-11
- Acumulación de nuevos casos etiquetados (activo/programado cada trimestre)

**Flujo de re-entrenamiento:**
```
Nuevos datos etiquetados
    → Validación de calidad (Great Expectations)
    → Versionado en Feature Store
    → Pipeline de entrenamiento automatizado (Apache Airflow / Prefect)
    → Evaluación automatizada contra baseline
    → Aprobación humana (medical review gate)
    → Despliegue canario (10% → 50% → 100% del tráfico)
    → Rollback automático si métricas degradan
```

**Aprendizaje activo:**
- El sistema identifica predicciones con baja confianza y las prioriza para etiquetado manual por especialistas
- Esto es especialmente valioso para enfermedades huérfanas donde cada caso nuevo es muy informativo

---

## 4. Diagrama Resumen del Pipeline

```
[Datos Crudos]
  Registros clínicos, Orphanet, HPO, OMIM
        |
        v
[Ingesta y Validación]
  Great Expectations, anonimización, esquema
        |
        v
[Preprocesamiento]
  Normalización HPO, imputación, codificación
        |
        v
[Feature Store]
  Feast / Hopsworks — versionado de features
        |
        v
[Entrenamiento]
  Enfermedades comunes → XGBoost / MLP
  Enfermedades huérfanas → Few-shot / Transfer Learning
  Meta-modelo → Router / Ensemble
        |
        v
[Validación y Evaluación]
  CV estratificado, F1-macro, revisión clínica
        |
        v
[Registro de Modelos]
  MLflow Model Registry — versioning
        |
        v
[Despliegue]
  Docker + Kubernetes + FastAPI
  Shadow mode → Canary → Producción
        |
        v
[Monitoreo]
  Data drift, model drift, métricas clínicas
        |
        v
[Re-entrenamiento]
  Aprendizaje activo → pipeline automatizado → deploy
```

---

## 5. Consideraciones Éticas y de Seguridad

- **Explicabilidad**: toda predicción debe acompañarse de los síntomas que más influyeron (SHAP values) para que el médico pueda validarla
- **No reemplaza al médico**: el sistema es una herramienta de apoyo a la decisión, no un diagnóstico definitivo
- **Auditoría**: todas las predicciones se almacenan con timestamp, versión de modelo y features usadas para trazabilidad
- **Equidad algorítmica**: validación periódica de que el modelo no penaliza grupos demográficos subrepresentados
- **Seguridad**: cifrado en tránsito (TLS) y en reposo, autenticación OAuth2, RBAC para acceso al modelo
