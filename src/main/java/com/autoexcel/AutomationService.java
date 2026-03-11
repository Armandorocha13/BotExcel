package com.autoexcel;

import com.autoexcel.model.DadosExcel;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import java.util.List;
import java.util.function.Consumer;

public class AutomationService {

    public void startAutomation(List<DadosExcel> linhas, String email, String senha, Consumer<String> logCallback) {
        if (linhas == null || linhas.isEmpty()) {
            logCallback.accept("[AVISO] A lista de registros está vazia. Automação abortada.");
            return;
        }
        logCallback.accept("[SUCESSO] Iniciando automação para " + linhas.size() + " registros desta Base.");
        logCallback.accept("[INFO] Inicializando a arquitetura Web do Playwright...");

        try {
            try (Playwright playwright = Playwright.create()) {
                // Modificado para usar o chromium do usuário ou criar uma janela maximizada com delays seguros
                // slowmo de 100ms ajuda a ver a interacao real
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100)); 
                BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1366, 768));
                Page page = context.newPage();

                logCallback.accept("    > Acessando a página de login...");
                page.navigate("https://rnc.tcia.com.br/");

                // ---------------------------------------------------------------------------------
                // PARTE 1 - LOGIN E ACESSO
                // ---------------------------------------------------------------------------------
                logCallback.accept("    > Realizando Login com: " + email);
                
                // Entrando usando os acessos injetados por parâmetro
                // Verificamos se a página de login está ativa ou se precisamos preencher
                if (page.url().contains("/login") || page.locator("#email").isVisible()) {
                    page.locator("#email").fill(email);
                    page.locator("#password").fill(senha);
                    page.locator("button:has-text('Entrar')").click();
                    logCallback.accept("    > Formulário de login enviado...");
                    
                    // Espera carregar o dashboard ou mudar a URL antes de forçar a ida para /rnc/nova
                    try {
                        page.waitForURL("**/dashboard", new Page.WaitForURLOptions().setTimeout(10000));
                        logCallback.accept("    > Sessão iniciada com sucesso.");
                    } catch (Exception e) {
                        logCallback.accept("    > Aguardando redirecionamento...");
                    }
                } else {
                    logCallback.accept("    > Sessão já ativa ou página direta detectada.");
                }

                logCallback.accept("    > (Web) Navegando para o formulário de Nova RNC...");
                page.navigate("https://rnc.tcia.com.br/rnc/nova");
                
                // Espera o painel inteiro carregar na tela (Aumentado para 30s para evitar quedas de rede)
                page.waitForSelector("text=Registro de Falhas", new Page.WaitForSelectorOptions().setTimeout(30000));

                // ---------------------------------------------------------------------------------
                // PARTE 2 - ADICIONANDO EQUIPAMENTOS NA RNC
                // ---------------------------------------------------------------------------------
                for (int i = 0; i < linhas.size(); i++) {
                    DadosExcel dados = linhas.get(i);
                    String codigoMac = dados.getEnderecavelPrincipal(); // Este é o código da coluna J q testamos
                    
                    if (codigoMac == null || codigoMac.trim().isEmpty()) {
                        logCallback.accept(String.format("[SKIPPED] Linha %d não contém MAC válido.", i + 1));
                        continue;
                    }

                    logCallback.accept("\n-------------------------------------------------------------");
                    logCallback.accept(String.format("[PROCESSO] %d/%d - Inserindo Equipamento no Sistema...", i + 1, linhas.size()));
                    
                    try {
                        // 1. Clicar em Adicionar Novo Item (Usando Role BUTTON para não confundir com o título do modal)
                        logCallback.accept("    > Acionando Modal de Novo Item...");
                        
                        Locator addBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Adicionar Novo Item"));
                        addBtn.scrollIntoViewIfNeeded();
                        addBtn.click();

                        // 2. Preencher a CA ID/CM MAC usando o seletor de ID encontrado na inspeção técnica
                        logCallback.accept("    > Escrevendo MAC: " + codigoMac);
                        page.locator("#ca-id").fill(codigoMac);

                        // 3. Clicar em Consultar Item
                        logCallback.accept("    > Botão Consultar Item enviado.");
                        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Consultar Item")).click();

                        // 4. Verificar se apareceu o erro de 'Código informado não localizado'
                        logCallback.accept("    > Esperando retorno do servidor (até 8s)...");
                        
                        // Espera técnica: se o campo SAP continuar vazio após 5-7s, ou se o alerta aparecer...
                        boolean carregou = false;
                        for (int sec = 0; sec < 8; sec++) {
                            // Se o alerta de erro aparecer
                            if (page.locator("text=Código informado não localizado").isVisible()) {
                                logCallback.accept("    [AVISO] Sistema reportou: Código não localizado.");
                                break;
                            }
                            // Se o campo SAP for preenchido automaticamente, significa que achou
                            String valSap = page.locator("#codSap").inputValue();
                            if (valSap != null && !valSap.trim().isEmpty()) {
                                logCallback.accept("    > Equipamento reconhecido automaticamente pelo sistema.");
                                carregou = true;
                                break;
                            }
                            Thread.sleep(1000);
                        }

                        if (!carregou) {
                            logCallback.accept("    > Preenchendo dados auxiliares do Excel (SAP, Modelo, Tecnologia)...");
                            
                            // Preenche os campos habilitados pelo sistema para permitir o botão 'Adicionar'
                            page.locator("#codSap").fill(dados.getCodigoMaterialSap());
                            page.locator("#modelo").fill(dados.getModelo());
                            page.locator("#tecnologia").fill(dados.getFamilia());
                            
                            logCallback.accept("    > Dados preenchidos manualmente para habilitar cadastro.");
                        }

                        // Procurar o menu de sintomas
                        logCallback.accept("    > Abrindo Selecionador de Sintomas...");
                        // Clica no dropdown de sintomas (usando seletor de texto exato pra não errar o click)
                        page.getByText("Selecione", new Page.GetByTextOptions().setExact(true)).first().click();

                        // 5. Clicar na opção "CONFIGURAÇÃO - NÃO HABILITA"
                        logCallback.accept("    > Escolhendo opção: CONFIGURAÇÃO - NÃO HABILITA...");
                        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("CONFIGURAÇÃO - NÃO HABILITA")).first().click();

                        // 6. Clicar em Adicionar (Aguardando ficar habilitado)
                        logCallback.accept("    > Aguardando botão Adicionar ficar pronto...");
                        Locator btnAddFinal = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Adicionar"));
                        
                        // Espera até o botão estar habilitado (Playwright faz isso no click, mas forçamos log se demorar)
                        btnAddFinal.click();

                        logCallback.accept("[SUCESSO] Item incluído no checklist!");

                    } catch (PlaywrightException ex) {
                        logCallback.accept("[ERRO] Falha ao processar código " + codigoMac + " -> " + ex.getMessage());
                        // Tentar fechar o modal ou seguir a vida
                        try {
                            if (page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancelar")).isVisible()) {
                                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancelar")).click();
                            }
                        } catch (Exception ignored) {}
                    }
                }
                
                logCallback.accept("\n[INFO] Robô Finalizou as inclusões.");
                logCallback.accept("[AVISO] A lista está pronta. Por favor, revise os itens e clique em 'Salvar RNC' manualmente.");
                logCallback.accept("[INFO] Aguardando você fechar o navegador para encerrar o processo...");
                
                // Mantém o navegador aberto enquanto o usuário não fechar a página
                while (!page.isClosed()) {
                    Thread.sleep(1000);
                }
                
                logCallback.accept("[INFO] Navegador fechado pelo usuário. Finalizando automação.");

            }
        } catch (Exception e) {
            logCallback.accept("[ERRO FATAL] O Processo desarmou: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
