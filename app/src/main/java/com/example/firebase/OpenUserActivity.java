package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OpenUserActivity extends AppCompatActivity {
    private EditText email;
    private EditText pass;
    private Button vihod;
    private Button update;
    private Button delete;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_user_activity);
        email = findViewById(R.id.email);
        pass = findViewById(R.id.pass);
        vihod = findViewById(R.id.vixodBTM);
        update = findViewById(R.id.updateButton);
        delete = findViewById(R.id.deleteButton);
        db = FirebaseFirestore.getInstance();
        Spinner roleSpinner = findViewById(R.id.roleSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(OpenUserActivity.this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
        Intent intent = getIntent();
        String emailGet = intent.getStringExtra("email");
        String passGet = intent.getStringExtra("pass");
        String roleGet = intent.getStringExtra("role");
        String id = intent.getStringExtra("ID");
        vihod.setOnClickListener(v -> {
            finish();
        });
        email.setText(emailGet);
        pass.setText(passGet);
        int position = getRolePosition(roleGet);
        if (position >= 0) {
            roleSpinner.setSelection(position);
        }

        vihod.setOnClickListener(v -> {
            finish();
        });

        update.setOnClickListener(v -> {
            String updatedEmail = email.getText().toString().trim();
            String updatedPass = email.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();
            if (!updatedEmail.isEmpty()) {
                updateUser(id, updatedEmail, updatedPass, role);
            }
        });

        delete.setOnClickListener(v -> {
            deleteUser(id, emailGet);
        });
    }

    private int getRolePosition(String role) {
        String[] rolesArray = getResources().getStringArray(R.array.roles_array);

        for (int i = 0; i < rolesArray.length; i++) {
            if (rolesArray[i].equals(role)) {
                return i;
            }
        }
        return -1;
    }

    private void deleteUser(String userId, String email) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(userId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(OpenUserActivity.this, "Пользователь удалён", Toast.LENGTH_SHORT).show();
                        finish();

                        Map<String, Object> log = new HashMap<>();
                        log.put("action", "Удаление пользователя");
                        log.put("details", "Email: " + email);
                        log.put("adminId", user.getUid());

                        db.collection("admin_logs").add(log)
                                .addOnSuccessListener(logDocumentReference -> {
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(OpenUserActivity.this, "Ошибка записи лога: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(OpenUserActivity.this, "Ошибка удаления пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(OpenUserActivity.this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUser(String userId, String newEmail, String newPassword, String newRole) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("email", newEmail);
            updatedData.put("pass", newPassword);
            updatedData.put("role", newRole);

            db.collection("users").document(userId)
                    .update(updatedData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(OpenUserActivity.this, "Данные пользователя обновлены", Toast.LENGTH_SHORT).show();

                        Map<String, Object> log = new HashMap<>();
                        log.put("action", "Изменение пользователя");
                        log.put("details", "Новый email: " + newEmail + ", Новая роль: " + newRole);
                        log.put("adminId", user.getUid());

                        db.collection("admin_logs").add(log)
                                .addOnSuccessListener(logDocumentReference -> {
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(OpenUserActivity.this, "Ошибка записи лога: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(OpenUserActivity.this, "Ошибка обновления данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(OpenUserActivity.this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
        }
    }
}
