# examforge-backend

# ExamForge: Generador Inteligente de Evaluaciones Académicas mediante RAG

## Portada
* **Título del Proyecto:** ExamForge - Plataforma Backend para la Generación, Gestión y Resolución de Evaluaciones Académicas Contextualizadas mediante Arquitectura RAG
* **Curso:** CS 2031 Desarrollo Basado en Plataforma
* **Periodo Académico:** 2026-2
* **Integrantes del Equipo:**
  * Lucia Rodriguez — Código: 202310459
  * Samira Rincon — Código: 202220436
  * Valeria Briceño — Código: 202310513
* **Enlace de Despliegue en Producción:** [https://examforge-api.onrender.com](https://examforge-api.onrender.com)
* **Colección Postman:** Ubicada en la raíz del repositorio (`postman_collection.json`)

---

## Índice
1. [Introducción](#introducción)
   - [Contexto](#contexto)
   - [Objetivos del Proyecto](#objetivos-del-proyecto)
2. [Identificación del Problema o Necesidad](#identificación-del-problema-o-necesidad)
   - [Descripción del Problema](#descripción-del-problema)
   - [Justificación](#justificación)
3. [Descripción de la Solución](#descripción-de-la-solución)
   - [Funcionalidades Implementadas](#funcionalidades-implementadas)
   - [Tecnologías Utilizadas](#tecnologías-utilizadas)
4. [Modelo de Entidades](#modelo-de-entidades)
   - [Diagrama Entidad-Relación](#diagrama-entidad-relación)
   - [Descripción de Entidades, Atributos y Relaciones JPA](#descripción-de-entidades-atributos-y-relaciones-jpa)
5. [Manejo de Errores](#manejo-de-errores)
   - [Estrategia de Excepciones Globales](#estrategia-de-excepciones-globales)
   - [Catálogo de Excepciones Personalizadas](#catálogo-de-excepciones-personalizadas)
6. [Medidas de Seguridad Implementadas](#medidas-de-seguridad-implementadas)
   - [Seguridad de Datos y Arquitectura JWT](#seguridad-de-datos-y-arquitectura-jwt)
   - [Control de Acceso Basado en Roles (RBAC)](#control-de-acceso-basado-en-roles-rbac)
   - [Prevención de Vulnerabilidades Comunes](#prevención-de-vulnerabilidades-comunes)
7. [Eventos y Asincronía](#eventos-y-asincronía)
   - [Pipeline Asíncrono de Procesamiento de Documentos](#pipeline-asíncrono-de-procesamiento-de-documentos)
   - [Servicio de Notificación por Correo Electrónico](#servicio-de-notificación-por-correo-electrónico)
8. [GitHub & Management](#github--management)
   - [Gestión de Tareas y Flujo Git](#gestión-de-tareas-y-flujo-git)
   - [Integración Continua con GitHub Actions](#integración-continua-con-github-actions)
9. [Instrucciones de Instalación y Ejecución Local](#instrucciones-de-instalación-y-ejecución-local)
10. [Conclusión](#conclusión)
    - [Logros del Proyecto](#logros-del-proyecto)
    - [Aprendizajes Clave](#aprendizajes-clave)
    - [Trabajo Futuro](#trabajo-futuro)
11. [Apéndices y Licencia](#apéndices-y-licencia)
12. [Referencias](#referencias)

---

## Introducción

### Contexto
En el ecosistema universitario contemporáneo, el aprendizaje autónomo y la preparación para evaluaciones constituyen actividades críticas que demandan gran cantidad de tiempo tanto de estudiantes como de profesores. Los materiales didácticos —compuestos principalmente por diapositivas, sílabos, lecturas complementarias y guías de laboratorio distribuidas en formato PDF— suelen dispersarse en plataformas heterogéneas. Aunque los modelos de lenguaje masivos actuales permiten la formulación automática de preguntas, carecen de un marco estructurado que asocie el conocimiento generado con una jerarquía académica formal (universidad, carrera y curso), imposibilitando la verificación documental y el seguimiento longitudinal del rendimiento estudiantil. ExamForge surge en este contexto como una plataforma backend construida con estándares de ingeniería de software que centraliza, procesa y evalúa el conocimiento institucional mediante arquitecturas de recuperación de información.

### Objetivos del Proyecto
* Diseñar e implementar una API RESTful escalable y modular con Spring Boot que gestione la estructura académica y el flujo completo de creación, edición y resolución de exámenes.
* Desarrollar un pipeline desacoplado y asíncrono para el procesamiento de documentos PDF, garantizando tiempos de respuesta mínimos en el servidor mediante fragmentación semántica de texto (chunking).
* Implementar un sistema de seguridad sin estado basado en JSON Web Tokens (JWT) y autorización basada en roles (RBAC) que proteja la integridad de los recursos académicos.
* Asegurar una trazabilidad estricta entre las preguntas de evaluación generadas y los fragmentos textuales que les sirven de sustento, mitigando inconsistencias conceptuales.

---

## Identificación del Problema o Necesidad

### Descripción del Problema
Los estudiantes universitarios enfrentan serias dificultades para medir su nivel de dominio conceptual antes de sus evaluaciones oficiales. Por un lado, la confección manual de bancos de preguntas por parte de los docentes consume decenas de horas por ciclo lectivo. Por otro lado, cuando los alumnos recurren a herramientas comerciales de inteligencia artificial generalista, se enfrentan a problemas sistemáticos:
1. **Falta de Trazabilidad:** Las respuestas generadas no citan de forma precisa el documento o la página exacta de la bibliografía del curso.
2. **Desorganización Curricular:** No existe un modelo relacional que agrupe evaluaciones según la malla curricular universitaria.
3. **Ausencia de un Motor de Práctica:** Las respuestas de los LLMs convencionales se entregan en bloques de texto plano, sin permitir una experiencia interactiva de evaluación, cálculo automatizado de notas ni retroalimentación formativa por áreas de mejora.

### Justificación
La implementación de ExamForge resuelve estas deficiencias al proveer un repositorio estructurado y validado. Al anclar la generación de reactivos evaluativos a documentos específicos del curso mediante fragmentos indexados, se garantiza que la práctica sea confiable y representativa. Esto optimiza la productividad del docente en la elaboración de material y dota al estudiante de un entorno formal donde poner a prueba sus competencias con retroalimentación inmediata.

---

## Descripción de la Solución

### Funcionalidades Implementadas
* **Gestión de Identidad y Autenticación:** Registro de usuarios, encriptación segura de credenciales, autenticación mediante JWT y renovación de sesiones a través de refresh tokens.
* **Organización Curricular:** Mantenimiento de cursos, carreras e inscripciones bajo un esquema de permisos diferenciados entre alumnos y profesores.
* **Ingesta y Fragmentación de Documentos:** Subida de material académico en PDF y particionamiento en fragmentos textuales identificados por página y orden relativo.
* **Generación y Edición Paramétrica de Exámenes:** Creación de evaluaciones definiendo cantidad de preguntas, nivel de dificultad y fuentes documentales asociadas, con capacidad de edición manual o regeneración individual de reactivos.
* **Motor de Resolución e Intentos:** Registro de respuestas de los estudiantes, calificación algorítmica instantánea y emisión de métricas de desempeño cuantitativo y cualitativo.
* **Servicio Automatizado de Notificaciones:** Despacho asíncrono de correos electrónicos transaccionales con el informe de resultados del examen resuelto.

### Tecnologías Utilizadas
* **Lenguaje:** Java 17 LTS
* **Framework Principal:** Spring Boot 3.3.x (Spring Web, Spring Data JPA, Spring Security, Spring Async)
* **Base de Datos Relacional:** PostgreSQL 15
* **Seguridad y Criptografía:** Spring Security 6, JJWT (Java JWT) y BCrypt
* **Procesamiento de Documentos:** Apache PDFBox
* **Mapeo de Objetos:** MapStruct y Lombok
* **Servicio de Correo:** JavaMailSender con plantillas HTML procesadas por Thymeleaf
* **Gestión de Dependencias y Construcción:** Apache Maven
* **Control de Versiones y CI/CD:** Git, GitHub Projects y GitHub Actions

---

## Modelo de Entidades

### Diagrama Entidad-Relación
+---------------+        1:N        +------------------+
|     User      |-------------------|    UserCourse    |
+---------------+                   +------------------+
| 1           | 1                          | N
|             |                            |
| 1:N         | 1:N                        | M:1
v             v                            v
+---------+   +------------+ 1:N    +------------------+
| Attempt |   |  Document  |<-------|      Course      |
+---------+   +------------+        +------------------+
| 1                      | 1
| 1:N                    | 1:N
v                        v
+---------------+ 1:M +------------------+
| DocumentChunk |<----|    Assessment    |
+---------------+     +------------------+
^                        | 1
|                        | 1:N
| M:N (FuentePregunta)   v
+-----------------+ +--------------+
| |   Question   |
| +--------------+
|      | 1
|      | 1:N
|      v
| +--------------+
+-|    Option    |
+--------------+


### Descripción de Entidades, Atributos y Relaciones JPA
El modelo de datos fue normalizado para garantizar integridad referencial y alto rendimiento en operaciones transaccionales complejas:

1. **`User` (`users`):** Representa a los usuarios del sistema. Contiene identificador único (`id`), nombre completo (`fullName`), correo electrónico institucional único (`email`), contraseña cifrada (`password`), rol del sistema (`role`) y marcas temporales de auditoría (`createdAt`).
   * *Relaciones:* `@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)` hacia `UserCourse` y `Attempt`.
2. **`Course` (`courses`):** Modela la asignatura académica. Posee `id`, nombre (`name`), código curricular (`code`), descripción temática (`description`) y universidad de pertenencia (`university`).
   * *Relaciones:* `@OneToMany(fetch = FetchType.LAZY)` hacia `UserCourse`, `Document` y `Assessment`.
3. **`UserCourse` (`user_courses`):** Entidad de enlace que registra la matrícula de los usuarios en asignaturas específicas, almacenando el identificador, la fecha de inscripción y el rol particular en la materia (`roleInCourse`).
   * *Relaciones:* `@ManyToOne(fetch = FetchType.LAZY)` hacia `User` y `Course`.
4. **`Document` (`documents`):** Registra los archivos bibliográficos cargados. Dispone de `id`, título descriptivo (`title`), ruta de almacenamiento (`fileUrl`), tamaño del archivo, estado de procesamiento (`status`: PENDING, PROCESSED, FAILED) y usuario que efectuó la carga.
   * *Relaciones:* `@ManyToOne(fetch = FetchType.LAZY)` hacia `Course` y `User`; `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` hacia `DocumentChunk`.
5. **`DocumentChunk` (`document_chunks`):** Almacena las unidades mínimas de texto extraídas del PDF para el contexto evaluativo. Contiene `id`, contenido textual (`content` con tipo `@Lob`/`TEXT`), número de página (`pageNumber`) y número de secuencia (`chunkOrder`).
   * *Relaciones:* `@ManyToOne(fetch = FetchType.LAZY)` hacia `Document`.
6. **`Assessment` (`assessments`):** Modela el examen o evaluación generada. Contiene `id`, título (`title`), instrucciones (`description`), nivel de dificultad (`difficulty`), indicador de visibilidad comunitaria (`isPublic`) y usuario creador.
   * *Relaciones:* `@ManyToOne(fetch = FetchType.LAZY)` hacia `Course` y `User`; `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` hacia `Question` e `Attempt`.
7. **`Question` (`questions`):** Representa cada reactivo dentro del examen. Atributos: `id`, enunciado textual (`text`), modalidad (`type`: MULTIPLE_CHOICE, TRUE_FALSE) y justificación conceptual (`explanation`).
   * *Relaciones:* `@ManyToOne(fetch = FetchType.LAZY)` hacia `Assessment`; `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` hacia `Option`; relación `@ManyToMany` hacia `DocumentChunk` como tabla intermedia de fuentes de sustento.
8. **`Option` (`options`):** Opciones posibles para una pregunta determinada. Atributos: `id`, texto explicativo de la alternativa (`text`) e indicador booleano de corrección (`isCorrect`).
   * *Relaciones:* `@ManyToOne(fetch = FetchType.LAZY)` hacia `Question`.
9. **`Attempt` (`attempts`):** Sesión de evaluación completada por un estudiante. Atributos: `id`, puntaje obtenido (`score`), retroalimentación cualitativa global (`feedback`) y fecha/hora de envío (`completedAt`).
   * *Relaciones:* `@ManyToOne(fetch = FetchType.LAZY)` hacia `Assessment` y `User`.

---

## Manejo de Errores

### Estrategia de Excepciones Globales
La robustez del backend se sustenta en un manejador global de excepciones instrumentado mediante la anotación `@RestControllerAdvice`. Este componente intercepta todas las fallas producidas a lo largo de las capas de la aplicación y las transforma en una respuesta uniforme modelada por `ErrorResponseDTO`. Dicha estructura incorpora:
* `timestamp`: Momento exacto en formato ISO-8601 del incidente.
* `status`: Código numérico HTTP.
* `error`: Denominación canónica del error (ej. Not Found, Bad Request).
* `message`: Detalle técnico o de negocio comprensible para el consumidor del API.
* `path`: Ruta del recurso invocada.

### Catálogo de Excepciones Personalizadas
Se implementaron más de 7 excepciones personalizadas organizadas jerárquicamente extendiendo de `RuntimeException`:
1. `ResourceNotFoundException` (HTTP 404): Disparada cuando un ID de curso, documento o examen no existe.
2. `DuplicateResourceException` (HTTP 409): Activada ante intentos de duplicar correos o claves únicas.
3. `UnauthorizedException` (HTTP 401): Lanzada por credenciales inválidas o firmas JWT corruptas.
4. `ForbiddenActionException` (HTTP 403): Ocurre cuando un usuario intenta modificar recursos ajenos sin privilegios de administrador.
5. `DocumentProcessingException` (HTTP 422): Señala fallas de parsing o lectura durante la descomposición de archivos PDF.
6. `AiGenerationException` (HTTP 502): Notifica interrupciones, respuestas malformadas o timeouts con el servicio generativo.
7. `InvalidAssessmentConfigException` (HTTP 400): Disparada cuando la cantidad de preguntas requeridas sobrepasa la densidad informativa de los fragmentos provistos.
8. `MethodArgumentNotValidException`: Captura validaciones de entrada (`@Valid`), recopilando mensajes de error específicos por campo en un mapa estructurado.

---

## Medidas de Seguridad Implementadas

### Seguridad de Datos y Arquitectura JWT
La seguridad de la plataforma opera bajo un modelo sin estado (*stateless*). Durante el inicio de sesión, el backend genera un token de acceso firmado algorítmicamente mediante HMAC-SHA256 (`HS256`) utilizando una clave simétrica robusta almacenada exclusivamente en variables de entorno. 
* El componente `JwtAuthenticationFilter` intercepta cada petición entrante, extrae el token del header `Authorization: Bearer <token>`, valida su firma y tiempo de vigencia, y reconstruye la identidad del usuario en el `SecurityContextHolder`.
* La persistencia de credenciales emplea el algoritmo de derivación de claves `BCryptPasswordEncoder` con factor de coste 12, previniendo ataques de diccionario y colisiones mediante salting dinámico.

### Control de Acceso Basado en Roles (RBAC)
Se estructuraron tres niveles de autoridad: `ROLE_STUDENT`, `ROLE_TEACHER` y `ROLE_ADMIN`. La autorización se aplica de forma granular en la capa de servicios y controladores empleando `@PreAuthorize` y anotaciones de seguridad por método (`@EnableMethodSecurity`), impidiendo que estudiantes realicen tareas docentes como la carga de material oficial o la modificación de evaluaciones curriculares.

### Prevención de Vulnerabilidades Comunes
* **Inyección SQL:** Mitigada totalmente mediante el uso exclusivo de repositorios Spring Data JPA con ligadura estricta de parámetros en consultas derivadas y consultas JPQL parametrizadas.
* **Cross-Site Scripting (XSS):** Garantizada a través del escape de caracteres en el motor de serialización Jackson y la validación de formato mediante expresiones regulares con `@Pattern` y `@NotBlank`.
* **Cross-Site Request Forgery (CSRF):** Al operar con una arquitectura API REST basada en Bearer Tokens en headers HTTP y no depender de cookies de sesión tradicionales del navegador, el soporte CSRF se encuentra deshabilitado de forma controlada y justificada.
* **CORS:** Configuración estricta en `CorsConfigurationSource` declarando orígenes explícitos, métodos HTTP autorizados (GET, POST, PUT, PATCH, DELETE) y cabeceras habilitadas.

---

## Eventos y Asincronía

### Pipeline Asíncrono de Procesamiento de Documentos
La lectura de archivos PDF y su partición en fragmentos textuales representa una carga computacional intensiva que degradaría la latencia del API si se ejecutara en el hilo principal de la petición web. Para mitigar esto:
1. La subida del archivo concluye inmediatamente persistiendo el documento con estado `PENDING` y devolviendo un código `202 Accepted`.
2. El servicio emite un `DocumentUploadedEvent` mediante `ApplicationEventPublisher`.
3. Un listener asíncrono decorado con `@Async` y gestionado por un `ThreadPoolTaskExecutor` personalizado asume el procesamiento en segundo plano: extrae el texto por páginas, construye los registros `DocumentChunk` y actualiza el estado a `PROCESSED`.

### Servicio de Notificación por Correo Electrónico
Al momento en que un estudiante remite su examen resuelto:
1. Se publica un `AttemptCompletedEvent`.
2. Un componente de mensajería intercepta el evento de forma desacoplada y utiliza `JavaMailSender` para componer un correo electrónico en formato HTML.
3. El motor de plantillas Thymeleaf inyecta dinámicamente el puntaje final, las preguntas acertadas y las oportunidades de refuerzo bibliográfico antes del despacho telemático.

---

## GitHub & Management

### Gestión de Tareas y Flujo Git
El desarrollo colaborativo de ExamForge se estructuró bajo la metodología GitFlow simplificada:
* La rama `main` contiene exclusivamente versiones estables y desplegables.
* Se prohibieron los commits directos sobre `main`, exigiendo ramas especializadas por funcionalidad (`feature/entities-jpa`, `feature/jwt-auth`, `feature/async-pipeline`).
* Se utilizó **GitHub Projects** mediante un tablero Kanban con columnas *Todo*, *In Progress* y *Done*, asociando cada *Pull Request* al respectivo *Issue* de trabajo con etiquetas descriptivas.

### Integración Continua con GitHub Actions
Se configuró un flujo de CI automatizado (`.github/workflows/backend-ci.yml`) que se dispara ante cada evento de `push` o `pull_request` a la rama principal:
* Entorno de ejecución sobre contenedor Ubuntu Latest con JDK 17 (Eclipse Temurin).
* Cacheo automático del repositorio local de dependencias Maven para optimizar tiempos de ejecución.
* Compilación completa del proyecto y verificación estricta de compilabilidad previa a la fusión de código.

---

## Instrucciones de Instalación y Ejecución Local

### Prerrequisitos
* Java Development Kit (JDK) 17 o superior
* Apache Maven 3.8+
* PostgreSQL 15 en ejecución local

### Pasos de Configuración
1. Clonar el repositorio:
   ```bash
   git clone [https://github.com/tu-usuario/examforge-backend.git](https://github.com/tu-usuario/examforge-backend.git)
   cd examforge-backend
