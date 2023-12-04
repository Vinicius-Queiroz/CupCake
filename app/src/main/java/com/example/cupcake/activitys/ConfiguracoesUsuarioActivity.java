package com.example.cupcake.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cupcake.R;
import com.example.cupcake.helper.ConfiguracaoFirebase;
import com.example.cupcake.helper.UsuarioFirebase;
import com.example.cupcake.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ConfiguracoesUsuarioActivity extends AppCompatActivity {
    private EditText nomeUsuario,enderecoUsuario;
    private String idUsuario;
    private DatabaseReference firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_usuario);
        //inicializar componentes
        inicializarComponentes();
        idUsuario = UsuarioFirebase.getIdUsuario();
        firebaseRef = ConfiguracaoFirebase.getFirebase();

        //configuracao da toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Cliente - Configurações");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recuperar dados do usuario
        recuperarDadosUsuario();

    }

    private void recuperarDadosUsuario() {
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    nomeUsuario.setText(usuario.getNomeUsuario());
                    enderecoUsuario.setText(usuario.getEnderecoUsuario());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void validarDadosUsuario(View view) {
        //validar se os campos foram preenchidos
        String nome = nomeUsuario.getText().toString();
        String endereco = enderecoUsuario.getText().toString();

        if (!nome.isEmpty()) {
            if (!endereco.isEmpty()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(idUsuario);
                usuario.setNomeUsuario(nome);
                usuario.setEnderecoUsuario(endereco);
                usuario.salvar();
                exibirMensagem("Suas Informações foram salvas!");
                finish();
            }else {
                exibirMensagem("Digite seu Endereço completo");
            }
        }else {
            exibirMensagem("Digite seu Nome");
        }

    }

    private void exibirMensagem(String texto) {
        Toast.makeText(this,texto,Toast.LENGTH_SHORT).show();
    }

    private void inicializarComponentes() {
        nomeUsuario = findViewById(R.id.editUsuarioNome);
        enderecoUsuario = findViewById(R.id.editUsuarioEndereco);
    }
}