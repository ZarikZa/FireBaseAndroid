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

public class SotrudFragment extends Fragment {

    private ListView listview;
    private ArrayAdapter<String> adapter;
    private EditText name;
    private Button addBtm;
    private FirebaseFirestore db;
    private List<String> servicesList;
    private List<String> ID;
    private String selectedTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sotrud_fragment, container, false);

        listview = view.findViewById(R.id.listView);
        name = view.findViewById(R.id.name);
        addBtm = view.findViewById(R.id.addButton);
        db = FirebaseFirestore.getInstance();
        servicesList = new ArrayList<>();
        ID = new ArrayList<>();
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, servicesList);
        listview.setAdapter(adapter);
        loadServices();
        addBtm.setOnClickListener(v -> {
            String nameService = name.getText().toString();
            addServicies(nameService);
            loadServices();
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTitle = adapter.getItem(i);
                db.collection("services").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String servicesName = documentSnapshot.getString("serviceName");
                        String serviceID = documentSnapshot.getId();
                        if (Objects.equals(selectedTitle, servicesName)){
                            Intent intent = new Intent(getActivity(), OpenPageActivity.class);
                            intent.putExtra("name", servicesName);
                            intent.putExtra("ID", serviceID);
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
        db.collection("services").get().addOnSuccessListener(queryDocumentSnapshots -> {
            servicesList.clear();
            ID.clear();
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String servicesName = documentSnapshot.getString("serviceName");
                String servisesID = documentSnapshot.getId();
                servicesList.add(servicesName);
                ID.add(servisesID);
                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireActivity(), "Ошибка загрузки услуг " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void addServicies(String name) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> servece = new HashMap<>();
            servece.put("serviceName", name);
            db.collection("services").add(servece)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(requireActivity(), "Услуга добавлена", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();

                        Map<String, Object> log = new HashMap<>();
                        log.put("action", "Добавление услуги");
                        log.put("details", "Название услуги: " + name);
                        log.put("adminId", user.getUid());

                        db.collection("admin_logs").add(log)
                                .addOnSuccessListener(logDocumentReference -> {
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireActivity(), "Ошибка записи лога: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireActivity(), "Услуга не создана: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireActivity(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
        }
    }
}
