package com.autoexcel.modelos;

public class Credencial {
    private String base;
    private String email;
    private String senha;

    public Credencial() {
    }

    public Credencial(String base, String email, String senha) {
        this.base = base;
        this.email = email;
        this.senha = senha;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
