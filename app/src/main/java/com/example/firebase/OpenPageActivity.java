package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OpenPageActivity extends AppCompatActivity {
    EditText name;
    Button vihod;
    Button update;
    Button delete;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_page);
        name = findViewById(R.id.textNazv);
        vihod = findViewById(R.id.vixodBTM);
        update = findViewById(R.id.updateButton);
        delete = findViewById(R.id.deleteButton);
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        String nameSercice = intent.getStringExtra("name");
        String id = intent.getStringExtra("ID");
        vihod.setOnClickListener(v -> {
            finish();
        });
        name.setText(nameSercice);

        vihod.setOnClickListener(v -> {
            finish();
        });

        update.setOnClickListener(v -> {
            String updatedName = name.getText().toString().trim();
            if (!updatedName.isEmpty()) {
                updateService(id, updatedName);
            }
        });

        delete.setOnClickListener(v -> {
            deleteService(id);
        });
    }

    private void updateService(String id, String newName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> servece = new HashMap<>();
            servece.put("serviceName", newName);
            DocumentReference docRef = db.collection("services").document(id);
            docRef.update(servece).addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Услуга обновлена", Toast.LENGTH_SHORT).show();

                        Map<String, Object> log = new HashMap<>();
                        log.put("action", "Изменение услуги");
                        log.put("details", "Новое название: " + newName);
                        log.put("adminId", user.getUid());

                        db.collection("admin_logs").add(log)
                                .addOnSuccessListener(logDocumentReference -> {
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Ошибка записи лога: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка обновления услуги: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteService(String id) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("services").document(id).delete()
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Услуга удалена", Toast.LENGTH_SHORT).show();
                        finish();

                        Map<String, Object> log = new HashMap<>();
                        log.put("action", "Удаление услуги");
                        log.put("details", "ID услуги: " + id);
                        log.put("adminId", user.getUid());

                        db.collection("admin_logs").add(log)
                                .addOnSuccessListener(logDocumentReference -> {
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Ошибка записи лога: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка удаления услуги: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
        }
    }
}
