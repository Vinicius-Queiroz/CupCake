package com.example.cupcake.model;

import com.example.cupcake.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

public class Usuario {
    private String idUsuario;
    private String nomeUsuario;
    private String enderecoUsuario;

    public Usuario() {
    }

    public void salvar() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(getIdUsuario());
        usuarioRef.setValue(this);
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getEnderecoUsuario() {
        return enderecoUsuario;
    }

    public void setEnderecoUsuario(String enderecoUsuario) {
        this.enderecoUsuario = enderecoUsuario;
    }
}
