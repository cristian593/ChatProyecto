package com.zumba.chatproyecto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.nio.channels.GatheringByteChannel;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private CircleImageView fotoPerfil;
    private TextView nombre;
    private RecyclerView recyclerMensajes;
    private EditText txtMensaje;
    private Button btnEnviar;
    private ImageButton btnEnviarFoto;

    private AdapterMensajes adapter;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private static final int PHOTO_SEND = 1;
    private static final int PHOTO_PERFIl = 2;

    private String fotoPerfilCadena;
    private String imgSave;

    private static final int RC_GALLERY = 21;
    private static final String PATH_PROFILE="perfil";
    private static final String PATH_PHOTO_URL="fotoUrl";

    private Uri imagenSeleccionada;
    private Uri imageUri;

    private String nombreLogueado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nombreLogueado = LoginActivity.nombreUser;

        getSupportActionBar().setTitle("Sala de Chat");
        fotoPerfil = findViewById(R.id.fotoPerfil);
        nombre = findViewById(R.id.nombreUsuario);
        recyclerMensajes = findViewById(R.id.recyclerMensajes);
        txtMensaje = findViewById(R.id.txtMensaje);
        btnEnviar = findViewById(R.id.btn_enviar);
        btnEnviarFoto = findViewById(R.id.btnEnviarFoto);

        nombre.setText(nombreLogueado);

        database =FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("chat");
        storage = FirebaseStorage.getInstance();
        fotoPerfilCadena="";

        storageReference = FirebaseStorage.getInstance().getReference();


        MostrarFoto();
        adapter = new AdapterMensajes(MainActivity.this);
        LinearLayoutManager l = new LinearLayoutManager(MainActivity.this);
        recyclerMensajes.setLayoutManager(l);
        recyclerMensajes.setAdapter(adapter);

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.push().setValue(new MensajeEnviar(txtMensaje.getText().toString(), nombre.getText().toString(),
                        fotoPerfilCadena, "1", ServerValue.TIMESTAMP));
                txtMensaje.setText("");
            }
        });

        btnEnviarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent, "Selecciona una Foto"), PHOTO_SEND);
            }
        });

        fotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent, "Selecciona una Foto"), PHOTO_PERFIl);
                //fromGallecry();



            }
        });

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                setScrollBar();

            }
        });

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MensajeRecibir m = dataSnapshot.getValue(MensajeRecibir.class);
                adapter.addMensaje(m);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PHOTO_PERFIl && resultCode ==RESULT_OK ){

            final Uri uri = data.getData();
            //fotoPerfil.setImageURI(uri);
            final StorageReference filepath = storageReference.child(PATH_PROFILE).child(nombreLogueado).child(PATH_PHOTO_URL+".jpg");
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this, "Foto Subida",Toast.LENGTH_LONG).show();
                    //Obtenemos la url de la imagen
                    String downloadImng =filepath.getDownloadUrl().toString();
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            DatabaseReference imageStore = FirebaseDatabase.getInstance().getReference().child("Usuarios").child(nombreLogueado).child(PATH_PROFILE);
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("imageurl", String.valueOf(uri));
                            imageStore.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Toast.makeText(MainActivity.this, ":)",Toast.LENGTH_LONG).show();

                                }
                            });
                        }
                    });


                }
            });
        }else{
            Toast.makeText(this, "Error, try again",Toast.LENGTH_LONG).show();

        }

    }

    private void MostrarFoto() {
        DatabaseReference img = FirebaseDatabase.getInstance().getReference().child("Usuarios").child(nombreLogueado)
                .child(PATH_PROFILE);
        /*DatabaseReference img = FirebaseDatabase.getInstance().getReference()
                .child(PATH_PROFILE);*/
        img.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    imgSave = dataSnapshot.child("imageurl").getValue().toString();
                    //Toast.makeText(MainActivity.this, imgSave,Toast.LENGTH_LONG).show();

                    Log.i("url",imgSave);
                    Glide.with(MainActivity.this).load(imgSave).into(fotoPerfil);
                    fotoPerfilCadena=imgSave;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setScrollBar(){
         recyclerMensajes.scrollToPosition(adapter.getItemCount()-1);
    }


}
