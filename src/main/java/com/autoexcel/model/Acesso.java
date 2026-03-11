package com.autoexcel.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Acesso {
    private String base;
    private String email;
    private String senha;
}
