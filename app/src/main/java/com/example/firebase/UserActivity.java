package com.example.firebase;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserActivity extends AppCompatActivity {
    private ListView servicesListView;
    private Button bookServicesButton;
    private EditText searchField;
    private Button searchButton;
    private Button clearSearchButton;
    private FirebaseFirestore db;
    private List<String> servicesList;
    private List<String> ID;
    private ArrayAdapter<String> adapter;

    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use);

        db = FirebaseFirestore.getInstance();
        servicesListView = findViewById(R.id.servicesListView);
        bookServicesButton = findViewById(R.id.bookServiceButton);
        searchField = findViewById(R.id.searchField);
        searchButton = findViewById(R.id.searchButton);
        clearSearchButton = findViewById(R.id.clearSearchButton);
        servicesList = new ArrayList<>();
        ID = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, servicesList);
        servicesListView.setAdapter(adapter);

        servicesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        loadServices();

        calendar = Calendar.getInstance();

        searchButton.setOnClickListener(v -> {
            String searchText = searchField.getText().toString().trim();
            filterServices(searchText);
        });

        clearSearchButton.setOnClickListener(v -> {
            searchField.setText("");
            loadServices();
        });

        bookServicesButton.setOnClickListener(v -> {
            int selectedPosition = servicesListView.getCheckedItemPosition();
            if (selectedPosition != ListView.INVALID_POSITION) {
                String servicesID = ID.get(selectedPosition);
                showDateTimePeckerDialog(servicesID);
            } else {
                Toast.makeText(this, "Выберите услугу", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadServices() {
        db.collection("services").get().addOnSuccessListener(queryDocumentSnapshots -> {
            servicesList.clear();
            ID.clear();
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String servicesName = documentSnapshot.getString("serviceName");
                String servisesID = documentSnapshot.getId();
                servicesList.add(servicesName);
                ID.add(servisesID);
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Ошибка загрузки услуг " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void filterServices(String searchText) {
        List<String> filteredServices = new ArrayList<>();
        List<String> filteredIDs = new ArrayList<>();

        for (int i = 0; i < servicesList.size(); i++) {
            if (servicesList.get(i).toLowerCase().contains(searchText.toLowerCase())) {
                filteredServices.add(servicesList.get(i));
                filteredIDs.add(ID.get(i));
            }
        }

        adapter.clear();
        adapter.addAll(filteredServices);
        adapter.notifyDataSetChanged();

        ID.clear();
        ID.addAll(filteredIDs);
    }

    private void showDateTimePeckerDialog(String servicesID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите дату и время ");
        View view = getLayoutInflater().inflate(R.layout.dialog_date_time_picker, null);
        builder.setView(view);
        TextView dataField = view.findViewById(R.id.dateField);
        TextView timefield = view.findViewById(R.id.timeField);

        dataField.setOnClickListener(v -> showDatePickerDialog(dataField));
        timefield.setOnClickListener(v -> showTimePickerDialog(timefield));

        builder.setPositiveButton("Записаться", (dialog, which) -> {
            String date = dataField.getText().toString();
            String time = timefield.getText().toString();
            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Заполните дату и время", Toast.LENGTH_SHORT).show();
                return;
            }
            bookServicies(servicesID, date, time);
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void bookServicies(String servisesID, String date, String time) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String clientName = user.getEmail();
            db.collection("services").document(servisesID).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String servicesName = documentSnapshot.getString("serviceName");
                            Map<String, Object> appointment = new HashMap<>();
                            appointment.put("clientId", user.getUid());
                            appointment.put("clientName", clientName);
                            appointment.put("servicesName", servicesName);
                            appointment.put("date", date);
                            appointment.put("time", time);
                            db.collection("appointment").add(appointment)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(UserActivity.this, "Запись создана", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Запись не создана: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка при получении услуги: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog(TextView dataField) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    dataField.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog(TextView timefield) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            timefield.setText(hourOfDay + ":" + minute);
        },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }
}
