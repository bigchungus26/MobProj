package com.mobproj.habittracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.Nullable;

import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.CategoryDao;
import com.mobproj.habittracker.data.UserDao;
import com.mobproj.habittracker.model.User;

public class LoginActivity extends BaseActivity {

    private EditText usernameInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.input_username);
        passwordInput = findViewById(R.id.input_password);
        MaterialButton signInBtn = findViewById(R.id.btn_sign_in);
        TextView registerLink = findViewById(R.id.link_register);

        signInBtn.setOnClickListener(v -> attemptSignIn());
        registerLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptSignIn() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        UserDao dao = new UserDao(this);
        User user = dao.authenticate(username, password);
        if (user == null) {
            Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        session.signIn(user.getId(), user.getUsername());
        new CategoryDao(this).ensureDefaults(user.getId());

        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
