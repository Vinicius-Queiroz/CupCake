package com.example.cupcake.activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cupcake.R;
import com.example.cupcake.helper.UsuarioFirebase;
import com.example.cupcake.model.Produto;

public class NovoProdutoEmpresaActivity extends AppCompatActivity {
    private EditText editProdutoNome,editProdutoDescricao,editProdutopreco;
    private String idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_produto_empresa);
        //configuracao da toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Empresa - Novo Produto");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //configuracoes iniciais
        inicializarComponentes();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

    }

    public void validarDadosProduto(View view) {
        //valida se os campos foram preenchidos
        String nome = editProdutoNome.getText().toString();
        String descricao = editProdutoDescricao.getText().toString();
        String preco = editProdutopreco.getText().toString();
        if (!nome.isEmpty()) {
            if (!descricao.isEmpty()) {
                if (!preco.isEmpty()) {
                    Produto produto = new Produto();
                    produto.setIdUsuario(idUsuarioLogado);
                    produto.setNome(nome);
                    produto.setDescricao(descricao);
                    produto.setPreco(Double.parseDouble(preco));
                    produto.salvar();
                    finish();
                    exibirMensagem("Produto salvo com Sucesso");
                }else {
                    exibirMensagem("Digite o valor");
                }
            }else {
                exibirMensagem("Digite a descrição");
            }
        }else {
            exibirMensagem("Digite o nome do produto");
        }
    }

    private void exibirMensagem(String texto) {
        Toast.makeText(this,texto,Toast.LENGTH_SHORT).show();
    }

    private void inicializarComponentes() {
        editProdutoNome = findViewById(R.id.editTextNomeProduto);
        editProdutoDescricao = findViewById(R.id.editTextDescricaoProduto);
        editProdutopreco = findViewById(R.id.editTextPrecoProduto);
    }
}