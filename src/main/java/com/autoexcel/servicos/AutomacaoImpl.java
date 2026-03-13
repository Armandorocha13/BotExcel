package com.autoexcel.servicos;

import com.autoexcel.modelos.Equipamento;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class AutomacaoImpl implements IAutomacao {

    @Override
    public void executar(List<Equipamento> itens, String email, String senha, Consumer<String> logCallback, Runnable onReadyToUnlock) {
        if (itens == null || itens.isEmpty()) {
            logCallback.accept("[AVISO] Lista vazia. Cancelando.");
            if (onReadyToUnlock != null) onReadyToUnlock.run();
            return;
        }

        logCallback.accept("[SUCESSO] Iniciando automação para " + itens.size() + " registros.");

        try {
            logCallback.accept("    > Inicializando motor Playwright (aguarde)...");
            try (Playwright playwright = Playwright.create()) {
                logCallback.accept("    > Lançando navegador Chromium...");
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setArgs(Arrays.asList("--start-maximized"))
                    .setSlowMo(100));
                
                logCallback.accept("    > Criando contexto de navegação...");
                BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
                Page page = context.newPage();

                logCallback.accept("    > Acessando portal: https://rnc.tcia.com.br/");
                page.navigate("https://rnc.tcia.com.br/");

            // Login
            if (page.url().contains("/login") || page.locator("#email").isVisible()) {
                logCallback.accept("    > Realizando Login: " + email);
                page.locator("#email").fill(email);
                page.locator("#password").fill(senha);
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Entrar")).click();
            }

            page.waitForURL("**/dashboard", new Page.WaitForURLOptions().setTimeout(20000));
            logCallback.accept("    > Sessão iniciada.");
            page.navigate("https://rnc.tcia.com.br/rnc/nova");
            page.waitForSelector("text=Registro de Falhas", new Page.WaitForSelectorOptions().setTimeout(30000));

            for (int i = 0; i < itens.size(); i++) {
                Equipamento eq = itens.get(i);
                String mac = eq.getEnderecavelPrincipal();

                if (mac == null || mac.trim().isEmpty()) continue;

                logCallback.accept("\n-------------------------------------------------------------");
                logCallback.accept(String.format("[PROCESSO] %d/%d - Inserindo Equipamento...", i + 1, itens.size()));

                try {
                    // Abrir Modal
                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Adicionar Novo Item")).click();
                    
                    // Preencher MAC e Consultar
                    logCallback.accept("    > Consultando MAC: " + mac);
                    page.locator("#ca-id").fill(mac);
                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Consultar Item")).click();

                    // Espera Resposta
                    boolean reconhecido = false;
                    for (int s = 0; s < 8; s++) {
                        if (page.locator("text=Código informado não localizado").isVisible()) break;
                        String valSap = page.locator("#codSap").inputValue();
                        if (valSap != null && !valSap.trim().isEmpty()) {
                            reconhecido = true;
                            break;
                        }
                        Thread.sleep(1000);
                    }

                    if (!reconhecido) {
                        logCallback.accept("    [AVISO] MAC não localizado. Preenchendo dados do Excel...");
                        page.locator("#codSap").fill(eq.getCodigoMaterialSap());
                        page.locator("#modelo").fill(eq.getModelo());
                        page.locator("#tecnologia").fill(eq.getFamilia());
                    } else {
                        logCallback.accept("    > Reconhecido automaticamente.");
                    }

                    // Sintoma
                    page.getByText("Selecione", new Page.GetByTextOptions().setExact(true)).first().click();
                    Thread.sleep(1000);
                    page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("CONFIGURAÇÃO - NÃO HABILITA")).first().click();

                    // Adicionar
                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Adicionar")).click();
                    logCallback.accept("[SUCESSO] Item adicionado!");

                } catch (Exception ex) {
                    logCallback.accept("[ERRO] Falha no item " + mac + ": " + ex.getMessage());
                    try { page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancelar")).click(); } catch (Exception ignored) {}
                }
            }

            logCallback.accept("\n[INFO] Fim das inserções. Rolando para o final da página...");
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
            logCallback.accept("[SUCESSO] Página rolada. Por favor, revise e salve manualmente.");
            
            // LIBERA A UI AQUI
            if (onReadyToUnlock != null) onReadyToUnlock.run();
            
            while (!page.isClosed()) { Thread.sleep(1000); }
        }
    } catch (com.microsoft.playwright.PlaywrightException e) {
            logCallback.accept("[ERRO] Erro no Playwright: " + e.getMessage());
            if (e.getMessage().contains("Executable doesn't exist")) {
                logCallback.accept(">>> AVISO: Os navegadores nao estao instalados.");
                logCallback.accept(">>> Por favor, feche o programa e execute o arquivo 'instalar_navegadores.bat'.");
            }
        } catch (Exception e) {
            logCallback.accept("[ERRO FATAL] Lógica de automação parou: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (onReadyToUnlock != null) onReadyToUnlock.run();
        }
    }
}
