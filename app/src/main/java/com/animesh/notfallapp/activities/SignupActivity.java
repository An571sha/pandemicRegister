package com.animesh.notfallapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.animesh.notfallapp.R;
import com.animesh.notfallapp.utility.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import se.aaro.gustav.passwordstrengthmeter.PasswordStrengthMeter;


public class SignupActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passwordField;
    private EditText repeatPasswordField;
    private EditText usernameField;
    private EditText phoneNumberField;
    private Button signUpButton;
    private PasswordStrengthMeter meter;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference userDatabase;

    private String email;
    private String username;
    private String password;
    private String repeatPassword;
    private String phoneNumber;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        emailField = findViewById(R.id.email_signup);
        passwordField = findViewById(R.id.password_signup);
        usernameField = findViewById(R.id.name_signup);
        phoneNumberField = findViewById(R.id.phone_number_signup);
        repeatPasswordField = findViewById(R.id.repeat_password_signup);
        signUpButton = findViewById(R.id.button_signup);
        meter = findViewById(R.id.passwordInputMeter);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userDatabase = FirebaseDatabase.getInstance().getReference();

        meter.setEditText(passwordField);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailField.getText().toString();
                password = passwordField.getText().toString();
                repeatPassword = repeatPasswordField.getText().toString();
                username = usernameField.getText().toString();
                phoneNumber = phoneNumberField.getText().toString();
                signUp(email, password, repeatPassword, username, phoneNumber);
            }
        });

    }


    public void signUp(final String email, String password, String repeatPassword, final String username, final String phoneNumber) {
        if (email != null && password != null && username != null && repeatPassword != null ) {

            if (Utility.isValidEmail(email) && password.equals(repeatPassword)) {

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    Log.i(Utility.getTAG(), "createUserWithEmail:success");
                                    Toast.makeText(SignupActivity.this, "Authentication success.",
                                            Toast.LENGTH_SHORT).show();

                                    Utility.writeNewUser(userDatabase, email, mAuth.getUid(), username, phoneNumber);

                                    emailField.getText().clear();
                                    passwordField.getText().clear();
                                    phoneNumberField.getText().clear();

                                    intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    startActivity(intent);

                                } else {
                                    String exception = Objects.requireNonNull(task.getException()).toString();
                                    String trimmedExceptionName = exception.substring(exception.lastIndexOf(":") + 1).trim();
                                    Toast.makeText(SignupActivity.this, trimmedExceptionName, Toast.LENGTH_SHORT).show();
                                    Log.e(Utility.getTAG(), trimmedExceptionName);
                                }
                            }
                        });

            } else {
                Toast.makeText(SignupActivity.this, R.string.invalid_email,
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(SignupActivity.this, R.string.input_error,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
