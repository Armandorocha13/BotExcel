package com.autoexcel;

import com.autoexcel.model.Acesso;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AcessosReader {

    /**
     * Lê o acessos.csv e retorna um Map onde a chave é o nome da Base (ex: 136710 - Rio de Janeiro)
     * e o valor é o objeto Acesso contendo email e senha correspondentes.
     */
    public Map<String, Acesso> readAcessos(String csvFilePath) {
        Map<String, Acesso> acessosMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isHeader = true;
            
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String base = values[0].trim();
                    String email = values[1].trim();
                    String senha = values[2].trim();
                    acessosMap.put(base, new Acesso(base, email, senha));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo acessos.csv: " + e.getMessage());
        }

        return acessosMap;
    }
}
