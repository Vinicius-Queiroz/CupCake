package com.example.cupcake.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.cupcake.R;
import com.example.cupcake.adapter.AdapterPedido;
import com.example.cupcake.helper.ConfiguracaoFirebase;
import com.example.cupcake.helper.UsuarioFirebase;
import com.example.cupcake.listener.RecyclerItemClickListener;
import com.example.cupcake.model.Pedido;
import com.example.cupcake.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PedidosActivity extends AppCompatActivity {
    private RecyclerView recyclerPedidos;
    private AdapterPedido adapterPedido;
    private List<Pedido> pedidos = new ArrayList<>();
    private AlertDialog dialog;
    private DatabaseReference firebaseRef;
    private String idEmpresa;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);
        //inicializar componentes
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idEmpresa = UsuarioFirebase.getIdUsuario();

        //configuracao da toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Pedidos");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //configurar RecyclerView
        recyclerPedidos.setLayoutManager(new LinearLayoutManager(this));
        recyclerPedidos.setHasFixedSize(true);
        adapterPedido = new AdapterPedido(pedidos);
        recyclerPedidos.setAdapter(adapterPedido);
        //recuperar pedidos
        recuperarPedidos();

        //configurar evento de clique para finalizar pedido no recyclerview
        recyclerPedidos.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerPedidos, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onLongItemClick(View view, int position) {
                Pedido pedido = pedidos.get(position);
                pedido.setStatus("finalizado");
                pedido.atualizarStatus();
                finish();
                Toast.makeText(PedidosActivity.this,"Pedido Finalizado!",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        }));


    }

    private void recuperarPedidos() {
        DatabaseReference pedidoRef = firebaseRef.child("pedidos").child(idEmpresa);
        Query pedidoPesquisa = pedidoRef.orderByChild("status").equalTo("confirmado");
        pedidoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pedidos.clear();
                if (snapshot.getValue() != null) {
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Pedido pedido = ds.getValue(Pedido.class);
                        pedidos.add(pedido);
                    }
                    adapterPedido.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializarComponentes() {
        recyclerPedidos = findViewById(R.id.recyclerPedidos);
    }

}