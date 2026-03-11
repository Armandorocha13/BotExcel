package com.autoexcel;

import com.autoexcel.model.DadosExcel;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String excelFilePath = "c:\\Users\\user\\Desktop\\AutoExcel\\base.xlsx";
        ExcelReader excelReader = new ExcelReader();
        
        System.out.println("Lendo o arquivo Excel...");
        List<DadosExcel> linhas = excelReader.readExcel(excelFilePath);
        
        System.out.println("Total de registros lidos: " + linhas.size());
        
        if (!linhas.isEmpty()) {
            System.out.println("Exemplo do primeiro registro: " + linhas.get(0));
        }
    }
}
