package com.example.cupcake.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.example.cupcake.R;
import com.example.cupcake.helper.ConfiguracaoFirebase;
import com.example.cupcake.helper.UsuarioFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class AutenticacaoActivity extends AppCompatActivity {

    private EditText campoEmail,campoSenha;
    private Switch switchTipo,switchAcesso;
    private Button buttonAcesso;
    private LinearLayout linearLayoutTipo;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacao);

        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        //verificar usuario logado
        verificarUsuarioLogado();

        //verifica se o switch do tipo de usuario foi checado para exibir o layout abaixo
        switchAcesso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (switchAcesso.isChecked()) {//empresa
                    linearLayoutTipo.setVisibility(View.VISIBLE);
                }else {//usuario
                    linearLayoutTipo.setVisibility(View.GONE);
                }
            }
        });
        //evento do botao para acessar
        buttonAcesso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if (!email.isEmpty()) {
                    if (!senha.isEmpty()) {
                        //verifica se o switch está checado e cria a funcao de cadastro ou login
                        if (switchAcesso.isChecked()) {//é cadastro
                            autenticacao.createUserWithEmailAndPassword(email,senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    //caso tenha sucesso ao criar usuario
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AutenticacaoActivity.this,"Cadastro realizado com Sucesso!",Toast.LENGTH_SHORT).show();
                                        String tipoUsuario = getTipoUsuario();
                                        UsuarioFirebase.atualizarTipoUsuario(tipoUsuario);
                                        abrirTelaHome(tipoUsuario);
                                    }else {
                                        String erroExcecao = "";
                                        try {
                                            throw task.getException();
                                        }catch (FirebaseAuthWeakPasswordException e) {
                                            erroExcecao = "Digite uma senha forte!";
                                        }catch (FirebaseAuthInvalidCredentialsException e) {
                                            erroExcecao = "Digite um e-mail válido!";
                                        }catch (FirebaseAuthUserCollisionException e) {
                                            erroExcecao = "Esta conta já está cadastrada!";
                                        }catch (Exception e) {
                                            erroExcecao = "ao cadastrar Usuário: "+ e.getMessage();
                                            e.printStackTrace();
                                        }
                                        Toast.makeText(AutenticacaoActivity.this,"Erro: " + erroExcecao,Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }else {//é login
                            autenticacao.signInWithEmailAndPassword(email,senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AutenticacaoActivity.this,"Bem Vindo(a)",Toast.LENGTH_SHORT).show();
                                        String tipoUsuario = task.getResult().getUser().getDisplayName();
                                        abrirTelaHome(tipoUsuario);
                                    }else {
                                        Toast.makeText(AutenticacaoActivity.this,"Erro ao fazer Login: " + task.getException(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }

                    }else {
                        Toast.makeText(AutenticacaoActivity.this,"Informe a senha",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(AutenticacaoActivity.this,"Informe o e-mail",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    //evento para verificar se o usuario está logado
    public void verificarUsuarioLogado() {
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if (usuarioAtual != null) {
            String tipoUsuario = usuarioAtual.getDisplayName();
            abrirTelaHome(tipoUsuario);
        }
    }
    //evento de retorno do tipo de usuario cadastrado
    private String getTipoUsuario() {
        return switchTipo.isChecked() ? "E" : "U";
    }
    public void abrirTelaHome(String switchTipo) {
        if (switchTipo.equals("E")) {//Empresa
            startActivity(new Intent(getApplicationContext(),EmpresaActivity.class));
        }else {//Usuario
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
        }
    }
    //inicializador de componentes da tela
    public void inicializarComponentes() {
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        switchAcesso = findViewById(R.id.switchAcesso);
        switchTipo = findViewById(R.id.switchtipo);
        buttonAcesso = findViewById(R.id.buttonAcessar);
        linearLayoutTipo = findViewById(R.id.linearLayoutTipo);
    }

}