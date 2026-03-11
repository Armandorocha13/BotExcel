# Guia de Distribuição e Uso - BotExcel

Este documento detalha como preparar e compartilhar o sistema para que outras pessoas possam utilizá-lo em suas máquinas.

## 1. Pré-requisitos
Para rodar este programa, o computador do usuário final precisa ter:
- **Java Runtime Environment (JRE) 17** ou superior instalado.
- Acesso à internet (para o primeiro carregamento dos drivers do Playwright).

## 2. Preparação do Pacote (Build)
Como criador, você deve gerar o arquivo executável unificado (.jar). Para isso, no terminal do projeto, execute:
```powershell
mvn clean compile assembly:single
```
Isso criará um arquivo dentro da pasta `target` chamado `AutoExcelProject-1.0-SNAPSHOT-jar-with-dependencies.jar`.

## 3. O que Compartilhar
Para que outra pessoa use o robô, você deve enviar a ela uma pasta contendo:
1. **O Executável:** O arquivo `.jar` gerado no passo anterior.
2. **As Configurações:** O arquivo `acessos.csv` (contendo as credenciais das bases).
3. **A Base de Dados:** O arquivo `base.xlsx` original.
4. **Script de Inicialização:** Um arquivo `executar.bat` (instruções abaixo).

## 4. Script de Inicialização (executar.bat)
Crie um arquivo chamado `executar.bat` na mesma pasta do `.jar` com o seguinte conteúdo:
```batch
@echo off
echo Iniciando Robô de Automação RNC...
java -jar AutoExcelProject-1.0-SNAPSHOT-jar-with-dependencies.jar
pause
```

## 5. Primeira Execução
Na primeira vez que o colega rodar o programa, o **Playwright** baixará automaticamente os navegadores necessários para o perfil dele. Isso pode demorar alguns minutos dependendo da conexão. Nas próximas vezes, a abertura será instantânea.

## 6. Segurança
> [!WARNING]
> Certifique-se de que o arquivo `acessos.csv` contém apenas as credenciais necessárias para as bases que o usuário deve operar. Nunca compartilhe senhas administrativas caso não seja necessário.
