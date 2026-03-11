package com.autoexcel;

import com.autoexcel.model.Acesso;
import com.autoexcel.model.DadosExcel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MainController {

    @FXML
    private TextField filePathField;

    @FXML
    private Button selectFileButton;

    @FXML
    private TextArea logArea;

    @FXML
    private FlowPane cardsContainer;

    private File selectedExcelFile;
    private Map<String, List<DadosExcel>> dadosAgrupados;
    private Map<String, Acesso> acessosBase;

    @FXML
    public void initialize() {
        logToInterface(">>> Módulo de automação multi-base carregado.");
        logToInterface(">>> Por favor, insira o XLSX para separarmos as bases.");
    }

    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar Arquivo .Xlsx");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Arquivos Padrão do Excel", "*.xlsx", "*.xls")
        );

        Stage stage = (Stage) selectFileButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedExcelFile = file;
            filePathField.setText(file.getAbsolutePath());
            logArea.clear();
            logToInterface(">>> Documento Válido Identificado: [" + file.getName() + "]");
            processarArquivo();
        }
    }

    private void processarArquivo() {
        try {
            // Ler e agrupar excel
            ExcelReader excelReader = new ExcelReader();
            List<DadosExcel> todosDados = excelReader.readExcel(selectedExcelFile.getAbsolutePath());
            dadosAgrupados = excelReader.groupDataByBase(todosDados);
            
            // Ler CSV
            AcessosReader acessosReader = new AcessosReader();
            String csvPath = new File("acessos.csv").getAbsolutePath();
            acessosBase = acessosReader.readAcessos(csvPath);

            logToInterface(">>> Bases identificadas no Excel: " + dadosAgrupados.size());
            logToInterface(">>> Credenciais carregadas do CSV: " + acessosBase.size());

            gerarCardsUi();
        } catch (Exception e) {
            logToInterface(">>> Erro ao processar: " + e.getMessage());
        }
    }

    private void gerarCardsUi() {
        cardsContainer.getChildren().clear();

        for (Map.Entry<String, List<DadosExcel>> entry : dadosAgrupados.entrySet()) {
            String nomeBase = entry.getKey();
            List<DadosExcel> itens = entry.getValue();
            
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
            card.setPrefWidth(220);

            Label titleLabel = new Label(nomeBase);
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            
            Label countLabel = new Label(itens.size() + " Registros");
            countLabel.setStyle("-fx-text-fill: #7f8c8d;");

            Button runBtn = new Button("Rodar Base");
            runBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            
            // Verificar se temos acesso. 
            // Agora usamos uma busca flexível para bater "TERESOPOLIS" com "144458 - Teresópolis"
            Acesso credenciais = getAcessoNormalizado(nomeBase);
            
            if (credenciais == null) {
                runBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
                runBtn.setText("Sem Credencial no CSV");
                runBtn.setDisable(true);
            }

            final Acesso credFinal = credenciais;
            runBtn.setOnAction(e -> {
                if (credFinal != null) {
                    iniciarAutomacaoParaBase(nomeBase, itens, credFinal.getEmail(), credFinal.getSenha());
                }
            });

            card.getChildren().addAll(titleLabel, countLabel, runBtn);
            cardsContainer.getChildren().add(card);
        }
    }

    private void iniciarAutomacaoParaBase(String base, List<DadosExcel> itens, String email, String senha) {
        logArea.clear();
        logToInterface(">>> Iniciando robô para a base: " + base);
        // Ocultar a edicao para evitar multiplos clicks
        selectFileButton.setDisable(true);
        cardsContainer.setDisable(true);

        Thread workerThread = new Thread(() -> {
            try {
                AutomationService servicoBack = new AutomationService();
                servicoBack.startAutomation(itens, email, senha, this::logToInterface);
                
                Platform.runLater(() -> {
                    logToInterface(">>> Automação da base " + base + " finalizada!");
                    selectFileButton.setDisable(false);
                    cardsContainer.setDisable(false);
                    showAlert(Alert.AlertType.INFORMATION, "Sucesso", "A fila da base " + base + " foi despachada.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    logToInterface(">>> ERRO: " + e.getMessage());
                    selectFileButton.setDisable(false);
                    cardsContainer.setDisable(false);
                });
            }
        });

        workerThread.setDaemon(true);
        workerThread.start();
    }

    private void logToInterface(String mensagem) {
        Platform.runLater(() -> {
            logArea.appendText(mensagem + "\n");
        });
    }

    private void showAlert(Alert.AlertType type, String titulo, String subtexto) {
        Alert notification = new Alert(type);
        notification.setTitle(titulo);
        notification.setHeaderText(null);
        notification.setContentText(subtexto);
        notification.show();
    }

    /**
     * Tenta encontrar o acesso na lista do CSV mesmo que o nome no Excel seja parcial.
     * Ex: "TERESOPOLIS" bate com "144458 - Teresópolis"
     */
    private Acesso getAcessoNormalizado(String nomeBaseExcel) {
        if (nomeBaseExcel == null || acessosBase == null) return null;
        
        String busca = normalize(nomeBaseExcel);
        if (busca.isEmpty()) return null;

        for (Map.Entry<String, Acesso> entry : acessosBase.entrySet()) {
            String baseCsv = normalize(entry.getKey());
            if (baseCsv.contains(busca) || busca.contains(baseCsv)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.toUpperCase()
                .replace("Á", "A").replace("À", "A").replace("Â", "A").replace("Ã", "A")
                .replace("É", "E").replace("È", "E").replace("Ê", "E")
                .replace("Í", "I").replace("Ì", "I").replace("Î", "I")
                .replace("Ó", "O").replace("Ò", "O").replace("Ô", "O").replace("Õ", "O")
                .replace("Ú", "U").replace("Ù", "U").replace("Û", "U")
                .replace("Ç", "C")
                .replaceAll("[^A-Z0-9]", "")
                .trim();
    }
}
