package com.zumba.chatproyecto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText nombreUsuario, passwordUsuario;
    private TextView loginUsuario, nuevoUsuario;
    private String pass;
    public static String nombreUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("Usuario Nuevo");

        setContentView(R.layout.activity_login);
        nombreUsuario = findViewById(R.id.user);
        passwordUsuario = findViewById(R.id.passuser);
        loginUsuario = findViewById(R.id.loginuser);
        nuevoUsuario = findViewById(R.id.newuser);

        loginUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComprobarDatos();
            }
        });

        nuevoUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CrearCuentaActivity.class);
                startActivity(intent);
            }
        });
    }

    private void ComprobarDatos() {
        nombreUser = nombreUsuario.getText().toString();
        pass = passwordUsuario.getText().toString();
        if(nombreUser.isEmpty()){
            Toast.makeText(LoginActivity.this, "Por favor ingrese un nombre de usuario",Toast.LENGTH_SHORT).show();
        }else  if(pass.isEmpty()){
            Toast.makeText(LoginActivity.this, "Por favor ingrese una contraseña",Toast.LENGTH_SHORT).show();
        }else{
            ValidarUsuario(nombreUser, passwordUsuario);
        }
    }

    private void ValidarUsuario(final String nombreUser, final EditText passwordUsuario) {

        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();
        //consultanos el usuario

        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("Usuarios").child(nombreUser).exists()) {


                    String passregistrada = dataSnapshot.child("Usuarios").child(nombreUser).child("password").getValue().toString();

                    if (passregistrada.equals(pass)) {


                        Toast.makeText(LoginActivity.this, "Bienvenido....", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        //Limpiamos la pila de actividades
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        //Guardamos el usuario que se logeo
                        startActivity(intent);
                    } else {

                        //Cuando la password es incorrecta
                        Toast.makeText(LoginActivity.this, "Password Incorrecta",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "La cuenta  no existe",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
