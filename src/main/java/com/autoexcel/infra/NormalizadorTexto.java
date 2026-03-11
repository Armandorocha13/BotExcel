package com.autoexcel.infra;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class NormalizadorTexto {

    /**
     * Remove acentos, caracteres especiais e espaços extras para comparação de nomes de bases.
     * @param texto Texto original (ex: "144458 - Teresópolis")
     * @return Texto limpo (ex: "TERESOPOLIS")
     */
    public static String normalizar(String texto) {
        if (texto == null) return "";
        
        // Remove acentos
        String deacentuado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        deacentuado = pattern.matcher(deacentuado).replaceAll("");

        // Converte para maiúsculo, remove tudo que não é letra ou número e tira espaços
        return deacentuado.toUpperCase()
                .replaceAll("[^A-Z0-9]", "")
                .trim();
    }
    
    /**
     * Verifica se duas bases são compatíveis (uma contém a outra após normalização).
     */
    public static boolean saoCompativeis(String baseA, String baseB) {
        String normA = normalizar(baseA);
        String normB = normalizar(baseB);
        if (normA.isEmpty() || normB.isEmpty()) return false;
        
        return normA.contains(normB) || normB.contains(normA);
    }
}
