package com.zumba.chatproyecto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class CrearCuentaActivity extends AppCompatActivity {

    private EditText nombre, pass, confpass;
    private String nombreU, password, confpassword;
    private TextView btnCrear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta);

        nombre = findViewById(R.id.nuevoUsuario);
        pass = findViewById(R.id.nuevaContrasena);
        confpass = findViewById(R.id.confirmarContasena);
        btnCrear = findViewById(R.id.crearcuenta);

        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComprobarDatos();
            }
        });
    }

    private void ComprobarDatos() {
        nombreU = nombre.getText().toString();
        password = pass.getText().toString();
        confpassword = confpass.getText().toString();

        if(nombreU.isEmpty()){
            Toast.makeText(CrearCuentaActivity.this, "Por favor ingrese un nombre de usuario",Toast.LENGTH_SHORT).show();
        }else if(password.isEmpty()){
            Toast.makeText(CrearCuentaActivity.this, "Por favor ingrese una contraseña",Toast.LENGTH_SHORT).show();
        }else if(confpassword.isEmpty()){
            Toast.makeText(CrearCuentaActivity.this, "Por favor confirme la contraseña",Toast.LENGTH_SHORT).show();
        }else{
            CrearCuenta(nombreU, password);
        }
    }

    private void CrearCuenta(final String nombreU, final String password) {

        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!(dataSnapshot.child("Usuarios").child(nombreU).exists())){

                    //Creamos el nuevo usuario
                    HashMap<String, Object> usuario = new HashMap<>();
                    usuario.put("nombre",nombreU);
                    usuario.put("password",password);

                    rootRef.child("Usuarios").child(nombreU).setValue(usuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(CrearCuentaActivity.this, " Usuario Creado ",
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(CrearCuentaActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }else {
                                Toast.makeText(CrearCuentaActivity.this, " Error de coneccion, por favor intente mas tarde ",
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }else {
                    Toast.makeText(CrearCuentaActivity.this, " Ese Nombre de usuario ya existe ",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    }

