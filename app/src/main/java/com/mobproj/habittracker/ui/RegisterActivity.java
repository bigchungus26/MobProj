package com.mobproj.habittracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.Nullable;

import com.mobproj.habittracker.R;
import com.mobproj.habittracker.data.CategoryDao;
import com.mobproj.habittracker.data.UserDao;

public class RegisterActivity extends BaseActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.input_username);
        passwordInput = findViewById(R.id.input_password);
        confirmInput = findViewById(R.id.input_confirm);
        MaterialButton registerBtn = findViewById(R.id.btn_register);

        registerBtn.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirm = confirmInput.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.length() < 3) {
            Toast.makeText(this, R.string.error_username_short, Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, R.string.error_password_short, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, R.string.error_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        UserDao dao = new UserDao(this);
        long id = dao.register(username, password);
        if (id == -1) {
            Toast.makeText(this, R.string.error_username_taken, Toast.LENGTH_SHORT).show();
            return;
        }

        new CategoryDao(this).ensureDefaults(id);
        session.signIn(id, username);
        Toast.makeText(this, R.string.msg_account_created, Toast.LENGTH_SHORT).show();

        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
