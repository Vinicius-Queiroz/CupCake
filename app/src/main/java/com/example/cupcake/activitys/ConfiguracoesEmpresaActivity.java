package com.example.cupcake.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cupcake.R;
import com.example.cupcake.helper.ConfiguracaoFirebase;
import com.example.cupcake.helper.UsuarioFirebase;
import com.example.cupcake.model.Empresa;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class ConfiguracoesEmpresaActivity extends AppCompatActivity {
    private EditText editNomeEmpresa,editCategoriaEmpresa,editTempoEmpresa,editTaxaEmpresa;
    private Button btnSalvarDados;
    private ImageView imagePerfilEmpresa;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private DatabaseReference firebaseRef;
    private String idUsuarioLogado;
    private String urlImagemSelecionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_empresa);
        //configuracoes iniciais
        inicializarComponentes();
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();
        //configuracao da toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Empresa - Configurações");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imagePerfilEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i,SELECAO_GALERIA);
                }
            }
        });

        btnSalvarDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //valida se os campos foram preenchidos
                String nome = editNomeEmpresa.getText().toString();
                String categoria = editCategoriaEmpresa.getText().toString();
                String tempo = editTempoEmpresa.getText().toString();
                String taxa = editTaxaEmpresa.getText().toString();
                if (!nome.isEmpty()) {
                    if (!categoria.isEmpty()) {
                        if (!tempo.isEmpty()) {
                            if (!taxa.isEmpty()) {
                                Empresa empresa = new Empresa();
                                empresa.setUrlImagem(urlImagemSelecionada);
                                empresa.setIdUsuario(idUsuarioLogado);
                                empresa.setNome(nome);
                                empresa.setCategoria(categoria);
                                empresa.setTempo(tempo);
                                empresa.setPrecoEntrega(Double.parseDouble(taxa));
                                empresa.salvar();
                                finish();
                                exibirMensagem("Empresa salvo com Sucesso");
                            }else {
                                exibirMensagem("Digite o valor");
                            }
                        }else {
                            exibirMensagem("Digite o tempo de entrega");
                        }
                    }else {
                        exibirMensagem("Digite a categoria");
                    }
                }else {
                    exibirMensagem("Digite um nome para empresa");
                }
            }

        });
        //recuperar dados da empresa
        recuperarDadosEmpresa();
    }

    private void recuperarDadosEmpresa() {
        DatabaseReference empresaRef = firebaseRef.child("empresas").child(idUsuarioLogado);
        empresaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Empresa empresa = snapshot.getValue(Empresa.class);
                    editNomeEmpresa.setText(empresa.getNome());
                    editCategoriaEmpresa.setText(empresa.getCategoria());
                    editTempoEmpresa.setText(empresa.getTempo());
                    editTaxaEmpresa.setText(empresa.getPrecoEntrega().toString());
                    urlImagemSelecionada = empresa.getUrlImagem();
                    if (urlImagemSelecionada != "") {  //precisa recuperar a imagem do upload no realtime database e especificar != de ""
                        Picasso.get().load(urlImagemSelecionada).into(imagePerfilEmpresa);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void exibirMensagem(String texto) {
        Toast.makeText(this,texto,Toast.LENGTH_SHORT).show();
    }
    //Tratamento da imagem no Firebase Upload e Download
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;
            try {
                switch (requestCode) {
                    case SELECAO_GALERIA:
                        Uri localImagem = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),localImagem);
                        break;
                    default:
                }
                if (imagem != null) {
                    imagePerfilEmpresa.setImageBitmap(imagem);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG,70,baos);
                    byte[] dadosImagem = baos.toByteArray();
                    String nomeArquivo = UUID.randomUUID().toString();
                    final StorageReference imagemRef = storageReference.child(nomeArquivo + ".jpeg");

                    //retorna objeto que irá controlar o upload
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfiguracoesEmpresaActivity.this,"Falha no Upload",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                    urlImagemSelecionada = String.valueOf(url);
                               }
                           });
                            Toast.makeText(ConfiguracoesEmpresaActivity.this,"Sucesso no Upload",Toast.LENGTH_SHORT).show();
                       }
                    });
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void inicializarComponentes() {
        imagePerfilEmpresa = findViewById(R.id.imagePerfilEmpresa);
        editNomeEmpresa = findViewById(R.id.editTextNomeEmpresa);
        editCategoriaEmpresa = findViewById(R.id.editTextCategoriaEmpresa);
        editTempoEmpresa = findViewById(R.id.editTextTempoEntrega);
        editTaxaEmpresa = findViewById(R.id.editTextTaxaEmpresa);
        btnSalvarDados = findViewById(R.id.buttonSalvarConfigEmpre);
    }
}