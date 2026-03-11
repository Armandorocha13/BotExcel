package com.autoexcel.servicos;

import com.autoexcel.modelos.Equipamento;
import java.util.List;
import java.util.Map;

public interface ILeitorExcel {
    List<Equipamento> lerArquivo(String caminhoArquivo);
    Map<String, List<Equipamento>> agruparPorBase(List<Equipamento> dados);
}
