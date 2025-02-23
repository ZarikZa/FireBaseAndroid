package com.example.firebase;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;

    public UserAdapter(Context context, List<User> users) {
        super(context, R.layout.user_item, users);
        this.context = context;
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        }

        User user = users.get(position);

        TextView userEmail = convertView.findViewById(R.id.userEmail);
        TextView userRole = convertView.findViewById(R.id.userRole);

        userEmail.setText(user.getEmail());
        userRole.setText("Роль: " + user.getRole());

        return convertView;
    }
}
