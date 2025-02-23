package com.example.firebase;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class adminActivity extends AppCompatActivity {
    BottomNavigationView botton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        botton = findViewById(R.id.bottomNavigationView);
        setFragment(new SotrudFragment());
        botton.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_learn) {
                    setFragment(new SotrudFragment());
                    return true;
                }else if(item.getItemId() == R.id.nav_levels){
                    setFragment(new UserFragment());
                    return true;
                }
                return false;
            }
        });
    }

    public void setFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment, null).commit();
    }
}
