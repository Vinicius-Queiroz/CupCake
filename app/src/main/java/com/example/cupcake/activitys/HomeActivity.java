package com.example.cupcake.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

import com.example.cupcake.R;
import com.example.cupcake.adapter.AdapterEmpresa;
import com.example.cupcake.helper.ConfiguracaoFirebase;
import com.example.cupcake.listener.RecyclerItemClickListener;
import com.example.cupcake.model.Empresa;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth autenticacao;
    private SearchView searchView;
    private RecyclerView recyclerEmpresa;
    private List<Empresa> empresas = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private AdapterEmpresa adapterEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //configuracao da toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        firebaseRef = ConfiguracaoFirebase.getFirebase();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        //inicializar componentes
        inicializarComponentes();
        //configurar RecyclerView
        recyclerEmpresa.setLayoutManager(new LinearLayoutManager(this));
        recyclerEmpresa.setHasFixedSize(true);
        adapterEmpresa = new AdapterEmpresa(empresas,this);
        recyclerEmpresa.setAdapter(adapterEmpresa);

        //recupera empresas para cliente
        recuperarEmpresas();

        //configuracao do SearchView
        searchView.setQueryHint("Pesquisar");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                pesquisarEmpresas(newText);
                return true;
            }
        });

        //configurar evento de clique nos produtos para levar para  a tela cardapio
        recyclerEmpresa.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerEmpresa, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Empresa empresaSelecionada = empresas.get(position);
                Intent i = new Intent(HomeActivity.this,CardapioActivity.class);
                i.putExtra("empresa",empresaSelecionada);
                startActivity(i);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        }));

    }
    private void pesquisarEmpresas(String pesquisa) {
        DatabaseReference empresasRef = firebaseRef.child("empresas");
        Query query = empresasRef.orderByChild("nome").startAt(pesquisa).endAt(pesquisa + "\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                empresas.clear();
                for (DataSnapshot ds:snapshot.getChildren()) {
                    empresas.add(ds.getValue(Empresa.class));
                }
                adapterEmpresa.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void recuperarEmpresas() {
        DatabaseReference empresaRef = firebaseRef.child("empresas");
        empresaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                empresas.clear();
                for (DataSnapshot ds:snapshot.getChildren()) {
                    empresas.add(ds.getValue(Empresa.class));
                }
                adapterEmpresa.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_usuario,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuPesquisar) {

        }else if (item.getItemId() == R.id.menuConfiguracoes) {
            abrirConfiguracoesUsuario();
        }else if (item.getItemId() == R.id.menuSair) {
            deslogaraUsuario();
        }
        return super.onOptionsItemSelected(item);
    }
    private void abrirConfiguracoesUsuario() {
        startActivity(new Intent(getApplicationContext(),ConfiguracoesUsuarioActivity.class));
    }
    private void deslogaraUsuario() {
        try {
            autenticacao.signOut();
            finish();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void inicializarComponentes() {
        searchView = findViewById(R.id.materialSearchView);
        recyclerEmpresa = findViewById(R.id.recyclerEmpresa);
    }

}