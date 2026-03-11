package com.autoexcel;

import com.autoexcel.model.DadosExcel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

    /**
     * Lê o arquivo XLSX e converte as linhas para uma lista de objetos DadosExcel.
     * @param filePath Caminho do arquivo Excel (.xlsx)
     * @return Lista de POJOs populada
     */
    public List<DadosExcel> readExcel(String filePath) {
        List<DadosExcel> dadosList = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Lê a primeira aba (sheet) do Excel
            Sheet sheet = workbook.getSheetAt(0);

            boolean isHeader = true;

            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Ignora o cabeçalho
                }

                // Verifica se a linha principal possui dados (se não possuir, encerramos a leitura da linha)
                if (row == null || row.getCell(0) == null || row.getCell(0).getCellType() == CellType.BLANK) {
                    continue;
                }

                DadosExcel dados = new DadosExcel();
                dados.setFornecedorSap(dataFormatter.formatCellValue(row.getCell(0)));
                dados.setFornecedor(dataFormatter.formatCellValue(row.getCell(1)));
                dados.setOperacao(dataFormatter.formatCellValue(row.getCell(2)));
                dados.setLocal(dataFormatter.formatCellValue(row.getCell(3)));
                dados.setCodigoMaterialSap(dataFormatter.formatCellValue(row.getCell(4)));
                dados.setTipo(dataFormatter.formatCellValue(row.getCell(5)));
                dados.setModelo(dataFormatter.formatCellValue(row.getCell(6)));
                dados.setFamilia(dataFormatter.formatCellValue(row.getCell(7)));
                dados.setValorUn(dataFormatter.formatCellValue(row.getCell(8)));
                dados.setEnderecavelPrincipal(dataFormatter.formatCellValue(row.getCell(9)));
                dados.setEstado(dataFormatter.formatCellValue(row.getCell(10)));
                dados.setDataUltimaAlteracao(dataFormatter.formatCellValue(row.getCell(11)));
                dados.setNoConnect(dataFormatter.formatCellValue(row.getCell(12)));
                dados.setStatusConnect2(dataFormatter.formatCellValue(row.getCell(13)));
                dados.setLoginTecnico(dataFormatter.formatCellValue(row.getCell(14)));
                dados.setNomeTecnico(dataFormatter.formatCellValue(row.getCell(15)));
                dados.setSupervisor(dataFormatter.formatCellValue(row.getCell(16)));
                dados.setCoordenador(dataFormatter.formatCellValue(row.getCell(17)));
                dados.setSegmento(dataFormatter.formatCellValue(row.getCell(18)));
                dados.setCidade(dataFormatter.formatCellValue(row.getCell(19)));
                dados.setDataConnect(dataFormatter.formatCellValue(row.getCell(20)));
                dados.setAlteracaoConnect(dataFormatter.formatCellValue(row.getCell(21)));
                dados.setBsod(dataFormatter.formatCellValue(row.getCell(22)));
                dados.setNotaFiscal(dataFormatter.formatCellValue(row.getCell(23)));
                dados.setStatusNf(dataFormatter.formatCellValue(row.getCell(24)));
                dados.setProjecao(dataFormatter.formatCellValue(row.getCell(25)));
                dados.setAutoEstoque(dataFormatter.formatCellValue(row.getCell(26)));
                dados.setAutoVolante(dataFormatter.formatCellValue(row.getCell(27)));
                dados.setAutoTerminais(dataFormatter.formatCellValue(row.getCell(28)));
                dados.setAutoReserva(dataFormatter.formatCellValue(row.getCell(29)));
                dados.setAutoExtra(dataFormatter.formatCellValue(row.getCell(30)));
                dados.setAgingConnect(dataFormatter.formatCellValue(row.getCell(31)));
                dados.setAgingAtlas(dataFormatter.formatCellValue(row.getCell(32)));
                dados.setAgingNf(dataFormatter.formatCellValue(row.getCell(33)));
                dados.setInventario60Dias(dataFormatter.formatCellValue(row.getCell(34)));

                dadosList.add(dados);
            }

        } catch (IOException e) {
            System.err.println("Erro ao iterar arquivo Excel. Caminho: " + filePath);
            e.printStackTrace();
        }

        return dadosList;
    }

    /**
     * Agrupa a lista de DadosExcel pelo campo fornecedor (Base).
     * @param allData Lista com todos os registros do Excel.
     * @return Map cuja chave é o fornecedor (Base) e valor é a lista de DadosExcel daquela base.
     */
    public java.util.Map<String, List<DadosExcel>> groupDataByBase(List<DadosExcel> allData) {
        java.util.Map<String, List<DadosExcel>> agrupado = new java.util.HashMap<>();
        
        for (DadosExcel obj : allData) {
            String base = obj.getFornecedor();
            if (base == null || base.trim().isEmpty()) {
                base = "Base Desconhecida";
            }
            agrupado.computeIfAbsent(base, k -> new ArrayList<>()).add(obj);
        }
        
        return agrupado;
    }
}
