@echo off
set "MAVEN_DIR=C:\maven\apache-maven-3.9.6"

echo [INFO] Iniciando o JavaFX e o Playwright!
set "PATH=%MAVEN_DIR%\bin;%PATH%"
mvn clean compile javafx:run
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] O Maven falhou ao iniciar a aplicacao.
    pause
)
