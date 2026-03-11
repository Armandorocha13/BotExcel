package com.autoexcel.servicos;

import com.autoexcel.modelos.Credencial;
import java.util.Map;

public interface IGerenciadorAcessos {
    Map<String, Credencial> carregarAcessos(String caminhoCsv);
    Credencial buscarPorBase(String nomeBase, Map<String, Credencial> todosAcessos);
}
