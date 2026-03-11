package com.autoexcel.servicos;

import com.autoexcel.modelos.Equipamento;
import java.util.List;
import java.util.function.Consumer;

public interface IAutomacao {
    void executar(List<Equipamento> itens, String email, String senha, Consumer<String> logCallback);
}
