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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister, btnClearForm;
    private TextView tvGoToLogin;
    private ProgressBar pbLoading;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = AuthHelper.getAuth(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilName = findViewById(R.id.tilRegisterName);
        tilEmail = findViewById(R.id.tilRegisterEmail);
        tilPassword = findViewById(R.id.tilRegisterPassword);
        tilConfirmPassword = findViewById(R.id.tilRegisterConfirmPassword);

        etName = findViewById(R.id.etRegisterName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnClearForm = findViewById(R.id.btnClearForm);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        pbLoading = findViewById(R.id.pbRegisterLoading);

        tvGoToLogin.setText(Html.fromHtml(getString(R.string.link_login), Html.FROM_HTML_MODE_LEGACY));
    }

    private void setupListeners() {
        // Working Button 1: Register User
        btnRegister.setOnClickListener(v -> performRegistration());

        // Working Button 2: Clear Form
        btnClearForm.setOnClickListener(v -> clearForm());

        // Working Button 3: Navigate to Login
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

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

        if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_min_length));
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            etConfirmPassword.requestFocus();
            return;
        }

        showLoading(true);

        if (mAuth != null) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && !TextUtils.isEmpty(name)) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                user.updateProfile(profileUpdates);
                            }
                        }
                        showLoading(false);
                        AuthHelper.registerUser(RegisterActivity.this, email, password, name);
                        AuthHelper.setLocalUserSession(RegisterActivity.this, email, name);
                        Toast.makeText(RegisterActivity.this, R.string.msg_register_success, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
        } else {
            showLoading(false);
            AuthHelper.registerUser(RegisterActivity.this, email, password, name);
                        AuthHelper.setLocalUserSession(RegisterActivity.this, email, name);
            Toast.makeText(RegisterActivity.this, R.string.msg_register_success, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void clearForm() {
        etName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");

        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
        btnClearForm.setEnabled(!isLoading);
    }
}
