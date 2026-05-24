# Actividad JADE con informe LaTeX y ejercicio adicional

Repositorio de trabajo para la actividad de la asignatura **Sistemas Multiagente y Percepción Computacional**.

La entrega incluye:

- un proyecto Java 8 con Maven y JADE;
- un ejercicio adicional con dos agentes: uno con interfaz Swing y otro respondedor;
- una memoria en LaTeX en `docs/` lista para compilar;
- marcadores para añadir las capturas de pantalla finales.

## Estructura

- `java/`: implementación Maven del ejercicio en Java.
- `docs/`: memoria en LaTeX, bibliografía y utilidades de compilación.

## Requisitos

- Java 8 o superior.
- Maven 3.8 o superior.
- `pdflatex` y `bibtex` para generar la memoria.

## Ejecución del ejercicio

```bash
cd java
mvn clean test
mvn exec:java -Dexec.mainClass=com.unir.jade.chat.Main
```

## Memoria

```bash
cd docs
make
```

## Nota sobre JADE

La documentación oficial actual de JADE permite integrarlo directamente mediante Maven a través de JitPack. La memoria describe esa vía, aunque el enunciado original de la práctica mencione el procedimiento clásico con ficheros descargados.

