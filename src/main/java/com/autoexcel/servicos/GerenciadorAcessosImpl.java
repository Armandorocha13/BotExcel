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
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoCsv))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",");
                if (partes.length >= 3) {
                    Credencial c = new Credencial(partes[0].trim(), partes[1].trim(), partes[2].trim());
                    mapa.put(c.getBase(), c);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar credenciais CSV: " + e.getMessage());
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
