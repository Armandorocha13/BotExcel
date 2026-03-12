# Script de Empacotamento Portátil para BotExcel

Write-Host ">>> Iniciando compilação do projeto..." -ForegroundColor Cyan
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "!!! Erro na compilação. Abortando." -ForegroundColor Red
    exit 1
}

$APP_NAME = "BotExcel"
$JAR_NAME = "AutoExcelProject-1.0-SNAPSHOT.jar"
$MAIN_CLASS = "com.autoexcel.principal.Lancador"
$JAVA_HOME_PATH = $env:JAVA_HOME

if (-not $JAVA_HOME_PATH) {
    $JAVA_HOME_PATH = "C:\Program Files\Java\jdk-25"
}

Write-Host ">>> Criando pasta do aplicativo portátil (app-image)..." -ForegroundColor Cyan
Remove-Item -Path "output" -Recurse -ErrorAction SilentlyContinue

& "$JAVA_HOME_PATH\bin\jpackage.exe" `
    --type app-image `
    --name $APP_NAME `
    --input target `
    --main-jar $JAR_NAME `
    --main-class $MAIN_CLASS `
    --dest output `
    --vendor "AutoExcel" `
    --description "Bot de Automação Excel"

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n>>> SUCESSO! O aplicativo portátil foi gerado na pasta 'output\$APP_NAME'." -ForegroundColor Green
    Write-Host ">>> Você pode copiar essa pasta para qualquer máquina Windows e rodar o '$APP_NAME.exe'." -ForegroundColor Yellow
} else {
    Write-Host "!!! Erro ao gerar o app-image." -ForegroundColor Red
}
