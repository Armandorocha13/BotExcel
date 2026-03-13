package com.autoexcel.servicos;

import com.autoexcel.modelos.Credencial;
import com.autoexcel.infra.NormalizadorTexto;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class GerenciadorAcessosImpl implements IGerenciadorAcessos {

    @Override
    public Map<String, Credencial> carregarAcessos(String caminhoCsv) {
        Map<String, Credencial> mapa = new HashMap<>();
        java.io.File arquivo = new java.io.File(caminhoCsv);
        
        if (!arquivo.exists()) {
            System.err.println("Arquivo de acessos não encontrado em: " + arquivo.getAbsolutePath());
            return mapa;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo, java.nio.charset.StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                
                // Suporta vírgula ou ponto-e-vírgula (comum em Excel brasileiro)
                String[] partes = linha.contains(";") ? linha.split(";") : linha.split(",");
                
                if (partes.length >= 3) {
                    Credencial c = new Credencial(partes[0].trim(), partes[1].trim(), partes[2].trim());
                    mapa.put(c.getBase(), c);
                }
            }
            System.out.println(">>> Credenciais carregadas: " + mapa.size());
        } catch (Exception e) {
            System.err.println("Erro ao ler CSV de acessos: " + e.getMessage());
        }
        return mapa;
    }

    @Override
    public Credencial buscarPorBase(String nomeBase, Map<String, Credencial> todosAcessos) {
        if (nomeBase == null || todosAcessos == null) return null;

        for (Map.Entry<String, Credencial> entry : todosAcessos.entrySet()) {
            if (NormalizadorTexto.saoCompativeis(entry.getKey(), nomeBase)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
