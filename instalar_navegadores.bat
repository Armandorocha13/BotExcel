@echo off
echo >>> Instalando navegadores necessarios (isso pode demorar alguns minutos)...
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRO] Falha ao instalar navegadores. Verifique sua conexao com a internet.
    pause
) else (
    echo.
    echo [SUCESSO] Navegadores instalados! Agora voce pode usar o BotExcel.
    pause
)
