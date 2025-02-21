package com.nmims.zepto;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference firebaseConnectionRef;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextEmail = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewRegister = findViewById(R.id.textViewRegister);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth == null) {
            Toast.makeText(this, "Firebase Auth not initialized!", Toast.LENGTH_SHORT).show();
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://zepto-4a284-default-rtdb.asia-southeast1.firebasedatabase.app");
        firebaseConnectionRef = database.getReference(".info/connected");

        checkFirebaseConnection();

        buttonLogin.setOnClickListener(v -> loginUser());
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
    private void loginUser() {
        if (!isConnected) {
            Toast.makeText(this, "No internet connection. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
           cd          if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, HomePage.class));
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Please verify your email address.", Toast.LENGTH_LONG).show();
                                user.sendEmailVerification()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(MainActivity.this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Login failed!";
                        Toast.makeText(MainActivity.this, "Login failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void checkFirebaseConnection() {
        firebaseConnectionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FirebaseConnection", "onDataChange called");
                isConnected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseConnection", "onCancelled called", error.toException());
                Toast.makeText(MainActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        checkFirebaseConnection();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(MainActivity.this, HomePage.class));
            finish();
        }
    }
}