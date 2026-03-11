package com.autoexcel.servicos;

import com.autoexcel.modelos.Equipamento;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeitorExcelImpl implements ILeitorExcel {

    @Override
    public List<Equipamento> lerArquivo(String caminhoArquivo) {
        List<Equipamento> lista = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(caminhoArquivo);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isHeader = true;

            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (row == null || row.getCell(0) == null || row.getCell(0).getCellType() == CellType.BLANK) {
                    continue;
                }

                Equipamento eq = new Equipamento();
                eq.setFornecedorSap(formatter.formatCellValue(row.getCell(0)));
                eq.setFornecedor(formatter.formatCellValue(row.getCell(1)));
                eq.setOperacao(formatter.formatCellValue(row.getCell(2)));
                eq.setLocal(formatter.formatCellValue(row.getCell(3)));
                eq.setCodigoMaterialSap(formatter.formatCellValue(row.getCell(4)));
                eq.setTipo(formatter.formatCellValue(row.getCell(5)));
                eq.setModelo(formatter.formatCellValue(row.getCell(6)));
                eq.setFamilia(formatter.formatCellValue(row.getCell(7)));
                eq.setValorUn(formatter.formatCellValue(row.getCell(8)));
                eq.setEnderecavelPrincipal(formatter.formatCellValue(row.getCell(9)));
                eq.setEstado(formatter.formatCellValue(row.getCell(10)));
                eq.setDataUltimaAlteracao(formatter.formatCellValue(row.getCell(11)));
                eq.setNoConnect(formatter.formatCellValue(row.getCell(12)));
                eq.setStatusConnect2(formatter.formatCellValue(row.getCell(13)));
                eq.setLoginTecnico(formatter.formatCellValue(row.getCell(14)));
                eq.setNomeTecnico(formatter.formatCellValue(row.getCell(15)));
                eq.setSupervisor(formatter.formatCellValue(row.getCell(16)));
                eq.setCoordenador(formatter.formatCellValue(row.getCell(17)));
                eq.setSegmento(formatter.formatCellValue(row.getCell(18)));
                eq.setCidade(formatter.formatCellValue(row.getCell(19)));
                eq.setDataConnect(formatter.formatCellValue(row.getCell(20)));
                eq.setAlteracaoConnect(formatter.formatCellValue(row.getCell(21)));
                eq.setBsod(formatter.formatCellValue(row.getCell(22)));
                eq.setNotaFiscal(formatter.formatCellValue(row.getCell(23)));
                eq.setStatusNf(formatter.formatCellValue(row.getCell(24)));
                eq.setProjecao(formatter.formatCellValue(row.getCell(25)));
                eq.setAutoEstoque(formatter.formatCellValue(row.getCell(26)));
                eq.setAutoVolante(formatter.formatCellValue(row.getCell(27)));
                eq.setAutoTerminais(formatter.formatCellValue(row.getCell(28)));
                eq.setAutoReserva(formatter.formatCellValue(row.getCell(29)));
                eq.setAutoExtra(formatter.formatCellValue(row.getCell(30)));
                eq.setAgingConnect(formatter.formatCellValue(row.getCell(31)));
                eq.setAgingAtlas(formatter.formatCellValue(row.getCell(32)));
                eq.setAgingNf(formatter.formatCellValue(row.getCell(33)));
                eq.setInventario60Dias(formatter.formatCellValue(row.getCell(34)));

                lista.add(eq);
            }

        } catch (IOException e) {
            throw new RuntimeException("Falha ao ler Excel: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public Map<String, List<Equipamento>> agruparPorBase(List<Equipamento> dados) {
        Map<String, List<Equipamento>> mapa = new HashMap<>();
        for (Equipamento eq : dados) {
            String base = eq.getFornecedor();
            if (base == null || base.trim().isEmpty()) base = "Base Desconhecida";
            mapa.computeIfAbsent(base, k -> new ArrayList<>()).add(eq);
        }
        return mapa;
    }
}
