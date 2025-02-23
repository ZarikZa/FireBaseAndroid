package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity  extends AppCompatActivity {
    private EditText emailDield, passDield;
    private Button loginButton, registerButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        auth = FirebaseAuth.getInstance();

        emailDield = findViewById(R.id.emailField);
        passDield = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        loginButton.setOnClickListener(v->loginUser());
        registerButton.setOnClickListener(v->regiserUser());
    }

    private void regiserUser() {
        String email = emailDield.getText().toString();
        String pass = passDield.getText().toString();
        if(email.isEmpty() || pass.isEmpty()){
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 5){
            Toast.makeText(this, "Пароль должен быть больше 5", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Неверный формат почты", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, task ->{
           if(task.isSuccessful()){
               Toast.makeText(AuthActivity.this, "УРа ты крутой", Toast.LENGTH_SHORT).show();
               saveUserToFirestore(email, pass);
           }else {
               if(task.getException() != null){
                   String errorMes = task.getException().getMessage();
                   Toast.makeText(AuthActivity.this, "Ошибка: " + errorMes, Toast.LENGTH_SHORT).show();
                   Log.e("AuthError","Ошибка регистрации", task.getException());
               }
           }
        });
    }

    private void saveUserToFirestore(String email, String pass) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String,Object> user = new HashMap<>();
        user.put("email", email);
        user.put("pass", pass);
        user.put("role", "user");
        db.collection("users").document(auth.getCurrentUser().getUid()).
            set(user).addOnSuccessListener(v-> {
                    Toast.makeText(AuthActivity.this, "Данные пользователя сохранены", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AuthActivity.this, MainActivity.class));
                    finish();
                }).addOnFailureListener(e-> {
                    Toast.makeText(AuthActivity.this, "Данные пользователя не сохранены((((((((((( " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

    private void loginUser() {
        String email = emailDield.getText().toString();
        String pass = passDield.getText().toString();

        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this,task -> {
            if (task.isSuccessful()){
                chekUserRole();
            }else{
                chekUserRole();
                Toast.makeText(AuthActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chekUserRole() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                String role = documentSnapshot.getString("role");
                if (role != null){
                    switch (role){
                        case "admin":
                            startActivity(new Intent(AuthActivity.this, adminActivity.class));
                            break;
                        case "user":
                            startActivity(new Intent(AuthActivity.this, UserActivity.class));
                            break;
                        case "employee":
                            startActivity(new Intent(AuthActivity.this, sotrudActivity.class));
                            break;
                    }
                    finish();
                }
            });
        }
    }
}
