package com.autoexcel.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Credencial {
    private String base;
    private String email;
    private String senha;
}
