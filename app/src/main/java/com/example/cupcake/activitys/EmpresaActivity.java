package com.example.cupcake.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.cupcake.R;
import com.example.cupcake.adapter.AdapterProduto;
import com.example.cupcake.helper.ConfiguracaoFirebase;
import com.example.cupcake.helper.UsuarioFirebase;
import com.example.cupcake.listener.RecyclerItemClickListener;
import com.example.cupcake.model.Produto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmpresaActivity extends AppCompatActivity {
    private FirebaseAuth autenticacao;
    private RecyclerView recyclerProdutos;
    private AdapterProduto adapterProduto;
    private List<Produto> produtos = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private String idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empresa);
        //configuracao da toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("CupCake - Empresa");
        setSupportActionBar(toolbar);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();
        //inicializar componentes
        inicializarComponentes();

        //configurar RecyclerView
        recyclerProdutos.setLayoutManager(new LinearLayoutManager(this));
        recyclerProdutos.setHasFixedSize(true);
        adapterProduto = new AdapterProduto(produtos,this);
        recyclerProdutos.setAdapter(adapterProduto);

        //recupera produtos para empresa
        recuperarProdutos();

        //adiciona evento de clique no RecyclerView
        recyclerProdutos.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerProdutos, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onLongItemClick(View view, int position) {
                Produto produtoSelecionado = produtos.get(position);
                produtoSelecionado.remover();
                Toast.makeText(EmpresaActivity.this,"Produto excluido com sucesso!",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        }));

    }

    //recuperar produtos para empresa
    private void recuperarProdutos() {
        DatabaseReference produtosRef = firebaseRef.child("produtos").child(idUsuarioLogado);
        produtosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                produtos.clear();
                for (DataSnapshot ds:snapshot.getChildren()) {
                    produtos.add(ds.getValue(Produto.class));
                }
                adapterProduto.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializarComponentes() {
        recyclerProdutos = findViewById(R.id.recyclerNovoProduto);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_emp,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuPesquisar) {
            abrirNovoProduto();
        }else if (item.getItemId() == R.id.menuConfiguracoes) {
            abrirConfiguracoes();
        }else if (item.getItemId() == R.id.menuSair) {
            deslogarUsuario();
        }else if (item.getItemId() == R.id.menuPedidoRecebido) {
            abrirPedidoRecebido();
        }
        return super.onOptionsItemSelected(item);
    }

    private void abrirPedidoRecebido() {
        startActivity(new Intent(getApplicationContext(),PedidosActivity.class));
    }

    private void abrirNovoProduto() {
        startActivity(new Intent(getApplicationContext(),NovoProdutoEmpresaActivity.class));
    }
    private void abrirConfiguracoes() {
        startActivity(new Intent(getApplicationContext(),ConfiguracoesEmpresaActivity.class));
    }

    private void deslogarUsuario() {
        try {
            autenticacao.signOut();
            finish();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}