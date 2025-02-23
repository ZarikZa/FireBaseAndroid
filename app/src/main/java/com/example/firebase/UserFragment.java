package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserFragment extends Fragment {

    private ListView listview;
    private EditText email;
    private EditText passs;
    private Button addBtm;
    private FirebaseFirestore db;
    private List<String> userList;
    private List<String> ID;
    private String selectedTitle;
    private ArrayAdapter<String> adapterList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_frgment, container, false);
        listview = view.findViewById(R.id.listView);
        email = view.findViewById(R.id.email);
        passs = view.findViewById(R.id.pass);
        addBtm = view.findViewById(R.id.addButton);
        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        ID = new ArrayList<>();
        adapterList = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, userList);
        listview.setAdapter(adapterList);
        loadServices();
        Spinner roleSpinner = view.findViewById(R.id.roleSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
        addBtm.setOnClickListener(view1 -> {
            String name = email.getText().toString().trim();
            String pass = passs.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();

            if (!name.isEmpty() && !pass.isEmpty()) {
                addUser(name, pass, role);
            } else {
                Toast.makeText(getActivity(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTitle = adapterList.getItem(i);
                db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String userEmail = documentSnapshot.getString("email");
                        String pass = documentSnapshot.getString("pass");
                        String role = documentSnapshot.getString("role");
                        String userID = documentSnapshot.getId();
                        if (Objects.equals(selectedTitle, userEmail)){
                            Intent intent = new Intent(getActivity(), OpenUserActivity.class);
                            intent.putExtra("email", userEmail);
                            intent.putExtra("pass", pass);
                            intent.putExtra("role", role);
                            intent.putExtra("ID", userID);
                            startActivity(intent);
                            loadServices();
                            break;
                        }
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), "Ошибка загрузки данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
        return view;
    }

    private void loadServices(){
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            userList.clear();
            ID.clear();
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String userEmail = documentSnapshot.getString("email");
                String userID = documentSnapshot.getId();
                userList.add(userEmail);
                ID.add(userID);
                adapterList.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireActivity(), "Ошибка загрузки " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void addUser(String name, String pass, String role) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(name, pass)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("email", name);
                                userMap.put("pass", pass);
                                userMap.put("role", role);

                                db.collection("users").add(userMap)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(requireActivity(), "Пользователь добавлен", Toast.LENGTH_SHORT).show();
                                            adapterList.notifyDataSetChanged();
                                            email.setText("");
                                            passs.setText("");

                                            Map<String, Object> log = new HashMap<>();
                                            log.put("action", "Добавление пользователя");
                                            log.put("details", "Email: " + name + ", Роль: " + role);
                                            log.put("adminId", currentUser.getUid());

                                            db.collection("admin_logs").add(log)
                                                    .addOnSuccessListener(logDocumentReference -> {
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(requireActivity(), "Ошибка записи лога: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(requireActivity(), "Ошибка добавления пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(requireActivity(), "Ошибка регистрации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(requireActivity(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
        }
    }
}
