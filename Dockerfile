# ============================================================
#  Dockerfile — Sistema de Tareas Domésticas (Spring Boot 3)
#  Usa construcción en dos etapas (multi-stage build):
#    Etapa 1 "build"  → compila el proyecto con Maven
#    Etapa 2 "run"    → solo copia el .jar y lo ejecuta
#  Resultado: imagen final liviana (~200 MB en lugar de ~600 MB)
# ============================================================

# ---- ETAPA 1: COMPILACIÓN ----
# Usamos la imagen oficial de Maven con Java 21
FROM maven:3.9-eclipse-temurin-21 AS build

# Directorio de trabajo dentro del contenedor de compilación
WORKDIR /app

# Copiamos primero solo el pom.xml y descargamos dependencias.
# Esto es un truco de caché de Docker: si el pom.xml no cambia,
# Docker reutiliza esta capa y no vuelve a descargar internet.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Ahora sí copiamos el código fuente completo
COPY src ./src

# Compilamos y empaquetamos, saltando los tests
# (los tests corren en el pipeline de CI, no aquí)
RUN mvn package -DskipTests -q

# ---- ETAPA 2: EJECUCIÓN ----
# Imagen base solo con el JRE (no necesitamos Maven para correr)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiamos únicamente el .jar generado en la etapa anterior
# El * en el nombre cubre el número de versión (ej: tareas-0.0.1-SNAPSHOT.jar)
COPY --from=build /app/target/*.jar app.jar

# Puerto en el que escucha Spring Boot
EXPOSE 8080

# Comando de arranque
# -Djava.security.egd acelera el inicio en Linux evitando bloqueos de entropía
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
