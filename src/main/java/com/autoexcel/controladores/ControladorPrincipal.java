package com.autoexcel.controladores;

import com.autoexcel.modelos.Credencial;
import com.autoexcel.modelos.Equipamento;
import com.autoexcel.servicos.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ControladorPrincipal {

    @FXML private TextField campoCaminhoArquivo;
    @FXML private Button botaoSelecionarArquivo;
    @FXML private TextArea areaLogs;
    @FXML private FlowPane containerCards;

    private File arquivoExcelSelecionado;
    private Map<String, List<Equipamento>> dadosAgrupados;
    private Map<String, Credencial> acessosDisponiveis;

    // Dependências (SOLID - DIP)
    private final ILeitorExcel leitorExcel = new LeitorExcelImpl();
    private final IGerenciadorAcessos gerenciadorAcessos = new GerenciadorAcessosImpl();
    private final IAutomacao servicoAutomacao = new AutomacaoImpl();

    @FXML
    public void initialize() {
        escreverLog(">>> Sistema de Automação RNC carregado.");
    }

    @FXML
    private void acaoSelecionarArquivo() {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Selecionar Base Excel");
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx", "*.xls"));

        Stage palco = (Stage) botaoSelecionarArquivo.getScene().getWindow();
        File arquivo = seletor.showOpenDialog(palco);

        if (arquivo != null) {
            arquivoExcelSelecionado = arquivo;
            campoCaminhoArquivo.setText(arquivo.getAbsolutePath());
            areaLogs.clear();
            escreverLog(">>> Arquivo selecionado: " + arquivo.getName());
            processarBases();
        }
    }

    private void processarBases() {
        try {
            // Carregar dados Excel
            List<Equipamento> todosOsItens = leitorExcel.lerArquivo(arquivoExcelSelecionado.getAbsolutePath());
            dadosAgrupados = leitorExcel.agruparPorBase(todosOsItens);

            // Carregar acessos CSV (Tenta na raiz e na pasta do executável)
            String[] caminhosPossiveis = {
                "acessos.csv",
                "../acessos.csv",
                "../../acessos.csv",
                new File(System.getProperty("user.dir"), "acessos.csv").getAbsolutePath()
            };
            
            for (String caminho : caminhosPossiveis) {
                File arq = new File(caminho);
                if (arq.exists()) {
                    acessosDisponiveis = gerenciadorAcessos.carregarAcessos(arq.getAbsolutePath());
                    if (!acessosDisponiveis.isEmpty()) {
                        escreverLog(">>> Arquivo de acessos carregado: " + arq.getName());
                        break;
                    }
                }
            }

            if (acessosDisponiveis == null || acessosDisponiveis.isEmpty()) {
                escreverLog(">>> AVISO: Nenhum acesso encontrado no arquivo acessos.csv!");
            }

            escreverLog(">>> Bases encontradas: " + dadosAgrupados.size());
            criarInterfaceDeCards();
        } catch (Exception e) {
            escreverLog(">>> Erro no processamento: " + e.getMessage());
        }
    }

    private void criarInterfaceDeCards() {
        containerCards.getChildren().clear();

        for (Map.Entry<String, List<Equipamento>> entrada : dadosAgrupados.entrySet()) {
            String nomeBase = entrada.getKey();
            List<Equipamento> itens = entrada.getValue();

            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
            card.setPrefWidth(220);

            Label lblTitulo = new Label(nomeBase);
            lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            Label lblQtd = new Label(itens.size() + " itens");

            Button btnIniciar = new Button("Iniciar Automação");
            
            // Buscar credencial usando a lógica SOLID isolada
            Credencial cred = gerenciadorAcessos.buscarPorBase(nomeBase, acessosDisponiveis);

            if (cred == null) {
                btnIniciar.setText("Sem Acesso");
                btnIniciar.setDisable(true);
                btnIniciar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            } else {
                btnIniciar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                btnIniciar.setOnAction(e -> dispararRobo(nomeBase, itens, cred));
            }

            card.getChildren().addAll(lblTitulo, lblQtd, btnIniciar);
            containerCards.getChildren().add(card);
        }
    }

    private void dispararRobo(String base, List<Equipamento> itens, Credencial cred) {
        areaLogs.clear();
        escreverLog(">>> Iniciando robô para " + base);
        
        bloquearInterface(true);

        Thread threadTrabalho = new Thread(() -> {
            try {
                servicoAutomacao.executar(itens, cred.getEmail(), cred.getSenha(), this::escreverLog, () -> {
                    Platform.runLater(() -> {
                        escreverLog(">>> Automação finalizada para a base " + base);
                        bloquearInterface(false);
                        exibirAlerta("Concluído", "A base " + base + " foi processada.");
                    });
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    escreverLog(">>> ERRO: " + ex.getMessage());
                    bloquearInterface(false);
                });
            }
        });

        threadTrabalho.setDaemon(true);
        threadTrabalho.start();
    }

    private void escreverLog(String msg) {
        Platform.runLater(() -> areaLogs.appendText(msg + "\n"));
    }

    private void bloquearInterface(boolean bloquear) {
        Platform.runLater(() -> {
            botaoSelecionarArquivo.setDisable(bloquear);
            containerCards.setDisable(bloquear);
        });
    }

    private void exibirAlerta(String titulo, String msg) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(msg);
        alerta.show();
    }
}
