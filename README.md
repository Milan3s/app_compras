
# 🎮 Raids and Clips

Aplicación de escritorio desarrollada en **Java + JavaFX**, con **MySQL** como base de datos y estilos en **CSS**.
Este README está pensado para que cualquier usuario, incluso sin experiencia, pueda instalar y ejecutar el proyecto correctamente.

---

## 📦 Requisitos previos

Importante , como el github no me deja subir archivos mas pesados de 200MB comparto el drive de los recursos para hacer funcionar las aplicaciones Java FX :  

👉 [📂 Enlace a recursos en Google Drive](https://drive.google.com/drive/u/0/folders/1Ohg2pPctoHHeIcJg0N4FlschER5sc1bO)

👉 [🌍 Página de proyectos y descargas](https://dmilanes.es/proyectos)


1. **Instalar XAMPP**

   * Descarga desde: [https://www.apachefriends.org/es/index.html](https://www.apachefriends.org/es/index.html)
   * Instálalo en la ruta por defecto:

     ```
     C:\xampp\
     ```
   * Para abrir el panel de control usa:

     ```
     C:\xampp\xampp-control.exe
     ```

2. **Instalar NetBeans**

   * Descarga desde: [https://netbeans.apache.org/](https://netbeans.apache.org/)
   * Recomendado **NetBeans 17 o superior**.

3. **Instalar JDK y JavaFX SDK**

   * Instala un **JDK** (Java Development Kit). Ejemplo: `jdk-24` o `jdk-11`.
   * Instala **JavaFX SDK** (necesario para la interfaz gráfica).
   * Instálalos en la ruta:

     ```
     C:\Program Files\Java\
     ```
   * Ejemplo de carpetas:

     ```
     C:\Program Files\Java\jdk-24
     C:\Program Files\Java\javafx-sdk-24.0.2
     ```

4. **Instalar Scene Builder**

   * Descarga desde: [https://gluonhq.com/products/scene-builder/](https://gluonhq.com/products/scene-builder/)
   * En NetBeans, ve a:
     **Tools > Options > Java > JavaFX** y selecciona la ruta de Scene Builder.

---

## 🧬 Clonar el proyecto

1. Abre la consola de Windows (CMD o PowerShell).
2. Escribe:

   ```bash
   git clone https://github.com/Milan3s/raids-and-clips.git
   ```
3. Abre NetBeans → **File > Open Project** → selecciona la carpeta clonada.

---

## 🗄️ Configuración de la base de datos

1. Abre **XAMPP Control Panel** desde:

   ```
   C:\xampp\xampp-control.exe
   ```
2. Inicia **Apache** y **MySQL**.
3. Entra en [http://localhost/phpmyadmin](http://localhost/phpmyadmin).
4. Crea la base de datos:

   ```
   raids_and_clips
   ```
5. Menú **Importar** → selecciona el archivo SQL del proyecto:

   ```
   ./database/raids_and_clips.sql
   ```
6. Pulsa **Continuar**.

✔️ Ya tendrás las tablas listas en MySQL.

---

## ⚙️ Configuración en NetBeans

1. **Agregar JDK**

   * En NetBeans: **Tools > Java Platforms > Add Platform**.
   * Selecciona:

     ```
     C:\Program Files\Java\jdk-24
     ```

2. **Añadir librerías JavaFX**

   * Clic derecho sobre el proyecto → **Properties**.
   * Ve a **Libraries > Compile > Add JAR/Folder**.
   * Añade todos los `.jar` de:

     ```
     C:\Program Files\Java\javafx-sdk-24.0.2\lib
     ```

3. **Opciones de ejecución (VM Options)**

   * En **Properties > Run > VM Options**, pega:

     ```
     --module-path "C:\Program Files\Java\javafx-sdk-24.0.2\lib" --add-modules javafx.controls,javafx.fxml
     ```

---

## 🛠️ Compilar el proyecto y mover archivos

1. En NetBeans: clic derecho en el proyecto → **Build with Dependencies**.

2. El compilado se genera en:

   ```
   C:\Users\Milanes\Documents\NetBeansProjects\raids_and_clips\target
   ```

3. Crea una carpeta en Archivos de Programa para la aplicación:

   ```
   C:\Program Files\Jars JavaFX\raids_and_clips\
   ```

4. Copia **todo el contenido de `target/`** dentro de `raids_and_clips\`.
   Esto incluye:

   * El archivo `.jar` generado.
   * Las carpetas `classes`, `generated-sources`, `maven-archiver`, `maven-status`, etc.

5. Renombra el `.jar` principal a:

   ```
   raids_and_clips.jar
   ```

---

## ▶️ Crear y configurar el archivo .BAT

1. Dentro de `C:\Program Files\Jars JavaFX\raids_and_clips\`, crea un archivo llamado:

   ```
   raids_and_clips.bat
   ```

2. Pega este contenido:

```bat
@echo off
title Raids and Clips - Iniciando
echo ================================
echo    Iniciando Raids and Clips...
echo ================================
echo.

REM --- Lanzar la aplicación ----
start "" "C:\Program Files\Java\jdk-24\bin\javaw.exe" ^
--module-path "C:\Program Files\Java\javafx-sdk-24.0.2\lib" ^
--add-modules javafx.controls,javafx.fxml ^
-jar "raids_and_clips.jar"

exit
```

3. Para ejecutar la aplicación, haz **doble clic en `raids_and_clips.bat`**.

---

## 📝 Cómo modificar el .BAT si cambian las versiones

* **Si cambias de JDK**, actualiza la ruta:

  ```
  "C:\Program Files\Java\jdk-24\bin\javaw.exe"
  ```

  Ejemplo para JDK 11:

  ```
  "C:\Program Files\Java\jdk-11.0.26\bin\javaw.exe"
  ```

* **Si cambias de JavaFX**, actualiza la ruta:

  ```
  --module-path "C:\Program Files\Java\javafx-sdk-24.0.2\lib"
  ```

  Ejemplo para JavaFX 21:

  ```
  --module-path "C:\Program Files\Java\javafx-sdk-21.0.7\lib"
  ```

* **Si renombraste el JAR**, asegúrate que coincida aquí:

  ```
  -jar "raids_and_clips.jar"
  ```

---

## 📂 Estructura final de la carpeta

Al terminar la instalación, tu carpeta debe quedar así:

```
C:\Program Files\Jars JavaFX\raids_and_clips\
│
├── raids_and_clips.jar      (JAR principal renombrado)
├── raids_and_clips.bat      (archivo para ejecutar la app)
│
├── classes\                 (copiado desde target/)
├── generated-sources\       (copiado desde target/)
├── maven-archiver\          (copiado desde target/)
├── maven-status\            (copiado desde target/)
│
└── (otros archivos generados en target/)
```

* El `.jar` **siempre debe llamarse** `raids_and_clips.jar`.
* El `.bat` **siempre debe estar en la misma carpeta que el `.jar`**.
* Los demás directorios (`classes`, etc.) también son necesarios.

---

✅ Con esta guía, cualquier usuario puede:

* Instalar XAMPP y la base de datos.
* Configurar NetBeans, JDK, JavaFX y Scene Builder.
* Compilar el proyecto.
* Copiar los archivos a Archivos de Programa.
* Ejecutar la aplicación con un simple **doble clic en el .bat**.
