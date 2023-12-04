package com.example.cupcake.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cupcake.R;
import com.example.cupcake.adapter.AdapterProduto;
import com.example.cupcake.helper.ConfiguracaoFirebase;
import com.example.cupcake.helper.UsuarioFirebase;
import com.example.cupcake.listener.RecyclerItemClickListener;
import com.example.cupcake.model.Empresa;
import com.example.cupcake.model.ItemPedido;
import com.example.cupcake.model.Pedido;
import com.example.cupcake.model.Produto;
import com.example.cupcake.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CardapioActivity extends AppCompatActivity {

    private RecyclerView recyclerProdutosCardapio;
    private ImageView imageEmpresaCardapio;
    private TextView textNomeEmpresaCardapio;
    private Empresa empresaSelecionada;
    private AdapterProduto adapterProduto;
    private List<Produto> produtos = new ArrayList<>();
    private List<ItemPedido> itensCarrinho = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private String idEmpresa;
    private String idUsuarioLogado;
    private Usuario usuario;
    private Pedido pedidoRecuperado;
    private int qtdItensCarrinho;
    private Double totalCarrinho;
    private TextView textCarrinhoQtd,textCarrinhoTotal;
    private int metodoPagamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);
        //inicializar componentes
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //recuperar empresa selecionada
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            empresaSelecionada = (Empresa) bundle.getSerializable("empresa");
            textNomeEmpresaCardapio.setText(empresaSelecionada.getNome());
            idEmpresa = empresaSelecionada.getIdUsuario();
            String url = empresaSelecionada.getUrlImagem();
            Picasso.get().load(url).into(imageEmpresaCardapio);
        }

        //configuracao da toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Cardapio");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //configurar RecyclerView
         recyclerProdutosCardapio.setLayoutManager(new LinearLayoutManager(this));
         recyclerProdutosCardapio.setHasFixedSize(true);
         adapterProduto = new AdapterProduto(produtos,this);
         recyclerProdutosCardapio.setAdapter(adapterProduto);

         //configurar evento de clique para selecionar os produtos no recyclerview
        recyclerProdutosCardapio.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerProdutosCardapio, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                confirmarQuantidade(position);

            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        }));

        //recupera produtos da empresa
        recuperarProdutos();
        //recuperar dados do usuario
        recuperarDadosUsuario();

    }

    //recuperar produtos para empresa
    private void recuperarProdutos() {
        DatabaseReference produtosRef = firebaseRef.child("produtos").child(idEmpresa);
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

    private void confirmarQuantidade(int posicao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quantidade");
        builder.setMessage("Digite a quantidade");

        EditText editQauntidade = new EditText(this);
        editQauntidade.setText("1");
        builder.setView(editQauntidade);

        builder.setPositiveButton("confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String quantidade = editQauntidade.getText().toString();
                Produto produtoSelecionado = produtos.get(posicao);
                ItemPedido itempedido = new ItemPedido();
                itempedido.setIdProduto(produtoSelecionado.getIdProduto());
                itempedido.setNomeProduto(produtoSelecionado.getNome());
                itempedido.setPreco(produtoSelecionado.getPreco());
                itempedido.setQuantidade(Integer.parseInt(quantidade));
                itensCarrinho.add(itempedido);

                if (pedidoRecuperado == null) {
                    pedidoRecuperado = new Pedido(idUsuarioLogado,idEmpresa);
                }
                pedidoRecuperado.setNome(usuario.getNomeUsuario());
                pedidoRecuperado.setEndereco(usuario.getEnderecoUsuario());
                pedidoRecuperado.setItens(itensCarrinho);
                pedidoRecuperado.salvar();

            }
        });

        builder.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialogo = builder.create();
        dialogo.show();

    }

    //recuperar dados do usuario
    private void recuperarDadosUsuario() {
        DatabaseReference usuariosRef = firebaseRef.child("usuarios").child(idUsuarioLogado);
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    usuario = snapshot.getValue(Usuario.class);
                }
                recuperarPedido();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cardapio,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuPedido) {
            confirmarPedido();
        }
        return super.onOptionsItemSelected(item);
    }
    //funcao confirmar pedido onde o usuario escolhe metodo de pagamento
    private void confirmarPedido() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione um método de pagamento");
        CharSequence[] itens = new CharSequence[] {
            "Dinheiro","Máquina de Cartão"
        };
        //implementacao do metodo click de pagamento
        builder.setSingleChoiceItems(itens, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                metodoPagamento = i;
            }
        });
        EditText editObservacao = new EditText(this);
        editObservacao.setHint("Informe uma observação");
        builder.setView(editObservacao);
        //implementacao do metodo positivo do botao confirmar
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String observacao = editObservacao.getText().toString();
                pedidoRecuperado.setMetodoPagamento(metodoPagamento);
                pedidoRecuperado.setObservacao(observacao);
                pedidoRecuperado.setStatus("confirmado");
                pedidoRecuperado.confirmar();
                pedidoRecuperado.remover();
                pedidoRecuperado = null;
                Toast.makeText(CardapioActivity.this,"Pedido Confirmado com Sucesso!",Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pedidoRecuperado.remover();
                Toast.makeText(CardapioActivity.this,"Você Cancelou o Pedido!",Toast.LENGTH_LONG).show();

            }
        });

        AlertDialog dialogMetodo = builder.create();
        dialogMetodo.show();

    }

    //recuperar pedido
    private void recuperarPedido() {
        DatabaseReference pedidoRef = firebaseRef.child("pedidosUsuario").child(idEmpresa).child(idUsuarioLogado);
        pedidoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                qtdItensCarrinho = 0;
                totalCarrinho = 0.0;
                //esa variavel zera a lista de pedidos quando o usuario sai
                itensCarrinho = new ArrayList<>();
                //condicao para verificar se tem pedido
                if (snapshot.getValue() != null) {
                    pedidoRecuperado = snapshot.getValue(Pedido.class);
                    itensCarrinho = pedidoRecuperado.getItens();

                    for (ItemPedido itemPedido:itensCarrinho) {
                        int qtde = itemPedido.getQuantidade();
                        Double preco = itemPedido.getPreco();
                        totalCarrinho += (qtde * preco);
                        qtdItensCarrinho += qtde;

                    }

                }
                DecimalFormat df = new DecimalFormat("0.00");
                textCarrinhoQtd.setText("Qtd: " + String.valueOf(qtdItensCarrinho));
                textCarrinhoTotal.setText("R$ " + df.format(totalCarrinho));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void inicializarComponentes() {
        recyclerProdutosCardapio = findViewById(R.id.recyclerProdutosCardapio);
        imageEmpresaCardapio = findViewById(R.id.imageEmpresaCardapio);
        textNomeEmpresaCardapio = findViewById(R.id.textNomeEmpresaCardapio);
        textCarrinhoQtd = findViewById(R.id.textCarrinhoQtd);
        textCarrinhoTotal = findViewById(R.id.textCarrinhoTotal);

    }


}