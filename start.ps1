$MAVEN_DIR = "C:\maven\apache-maven-3.9.6"

if (-not (Test-Path "$MAVEN_DIR\bin\mvn.cmd")) {
    Write-Host "[INFO] Preparando utilitarios Maven (Instalacao Limpa)..." -ForegroundColor Cyan
    if (-not (Test-Path "C:\maven")) {
        New-Item -ItemType Directory -Force -Path "C:\maven" | Out-Null
    }
    
    Write-Host "[INFO] Baixando pacote do Maven. Aguarde..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri "https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" -OutFile "maven.zip" -UseBasicParsing
    
    Write-Host "[INFO] Extraindo pacote para o disco..." -ForegroundColor Yellow
    Expand-Archive -Path "maven.zip" -DestinationPath "C:\maven" -Force
    Remove-Item "maven.zip" -Force
}

Write-Host "[INFO] Iniciando o JavaFX e o Playwright! (A primeira vez pode demorar para baixar a internet do chromium...)" -ForegroundColor Green

$env:Path = "$MAVEN_DIR\bin;" + $env:Path
mvn clean compile javafx:run
