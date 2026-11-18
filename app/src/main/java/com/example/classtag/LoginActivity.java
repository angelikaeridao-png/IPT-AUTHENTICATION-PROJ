package com.example.classtag;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnDemoLogin;
    private TextView tvGoToRegister;
    private ProgressBar pbLoading;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = AuthHelper.getAuth(this);

        if (AuthHelper.isLoggedIn(this)) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnDemoLogin = findViewById(R.id.btnDemoLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        pbLoading = findViewById(R.id.pbLoading);

        tvGoToRegister.setText(Html.fromHtml(getString(R.string.link_register), Html.FROM_HTML_MODE_LEGACY));
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        btnDemoLogin.setOnClickListener(v -> performDemoLogin());

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_required));
            etPassword.requestFocus();
            return;
        }

        if (!AuthHelper.isEmailRegistered(this, email)) {
            tilEmail.setError(getString(R.string.error_account_not_registered));
            etEmail.requestFocus();
            return;
        }

        if (!AuthHelper.verifyUserPassword(this, email, password)) {
            tilPassword.setError(getString(R.string.error_invalid_credentials));
            etPassword.requestFocus();
            return;
        }

        showLoading(true);

        if (mAuth != null) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        showLoading(false);
                        if (task.isSuccessful() || AuthHelper.verifyUserPassword(LoginActivity.this, email, password)) {
                            String name = AuthHelper.getRegisteredName(LoginActivity.this, email);
                            if (TextUtils.isEmpty(name)) { name = email.split("@")[0]; }
                            AuthHelper.setLocalUserSession(LoginActivity.this, email, name);
                            Toast.makeText(LoginActivity.this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                            Toast.makeText(LoginActivity.this, getString(R.string.error_auth_failed, errorMsg), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            showLoading(false);
            String name = AuthHelper.getRegisteredName(LoginActivity.this, email);
                            if (TextUtils.isEmpty(name)) { name = email.split("@")[0]; }
            AuthHelper.setLocalUserSession(LoginActivity.this, email, name);
            Toast.makeText(LoginActivity.this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();
            navigateToMain();
        }
    }

    private void performDemoLogin() {
        etEmail.setText("student.demo@classtag.com");
        etPassword.setText("Password123!");

        showLoading(true);

        if (mAuth != null) {
            mAuth.signInWithEmailAndPassword("student.demo@classtag.com", "Password123!")
                    .addOnCompleteListener(this, task -> {
                        showLoading(false);
                        AuthHelper.setLocalUserSession(LoginActivity.this, "student.demo@classtag.com", "Demo Student");
                        Toast.makeText(LoginActivity.this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    });
        } else {
            showLoading(false);
            AuthHelper.setLocalUserSession(LoginActivity.this, "student.demo@classtag.com", "Demo Student");
            Toast.makeText(LoginActivity.this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();
            navigateToMain();
        }
    }

    private void showLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        btnDemoLogin.setEnabled(!isLoading);
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
