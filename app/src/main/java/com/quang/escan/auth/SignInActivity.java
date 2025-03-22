package com.quang.escan.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.quang.escan.MainActivity;
import com.quang.escan.R;
import com.quang.escan.databinding.ActivitySignInBinding;

/**
 * Activity that handles user sign-in with Firebase Authentication
 */
public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private ActivitySignInBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        
        setupClickListeners();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already signed in: " + currentUser.getEmail());
            navigateToMainActivity();
        }
    }

    private void setupClickListeners() {
        // Sign in button click
        binding.signInButton.setOnClickListener(v -> {
            if (validateInputs()) {
                attemptSignIn();
            }
        });

        // Forgot password click
        binding.forgotPassword.setOnClickListener(v -> {
            String email = binding.email.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                binding.emailLayout.setError("Enter your email to reset password");
                binding.email.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailLayout.setError("Enter a valid email address");
                binding.email.requestFocus();
            } else {
                sendPasswordResetEmail(email);
            }
        });

        // Sign up link click
        binding.signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            // Optional: add transition animation
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    /**
     * Validates email and password inputs
     * @return true if inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        boolean isValid = true;

        // Check email
        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Please enter a valid email");
            isValid = false;
        } else {
            binding.emailLayout.setError(null);
        }

        // Check password
        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            binding.passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            binding.passwordLayout.setError(null);
        }

        return isValid;
    }

    /**
     * Attempts to sign in the user using Firebase Authentication
     */
    private void attemptSignIn() {
        // Show loading indicator
        showLoading(true);

        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        // Use Firebase Auth to sign in
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    signInSuccess(user);
                } else {
                    // Sign in failed
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    handleSignInError(task.getException());
                }
                
                // Hide loading indicator
                showLoading(false);
            });
    }

    /**
     * Handles errors that occur during sign-in
     */
    private void handleSignInError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            // Email doesn't exist
            binding.emailLayout.setError("Account doesn't exist with this email");
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            // Wrong password
            binding.passwordLayout.setError("Incorrect password");
        } else {
            // Other errors
            Toast.makeText(this, "Authentication failed: " + exception.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles successful sign-in
     */
    private void signInSuccess(FirebaseUser user) {
        Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
        navigateToMainActivity();
    }
    
    /**
     * Navigate to the main activity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Sends a password reset email using Firebase Auth
     */
    private void sendPasswordResetEmail(String email) {
        showLoading(true);
        
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                showLoading(false);
                if (task.isSuccessful()) {
                    Log.d(TAG, "Password reset email sent to " + email);
                    Toast.makeText(SignInActivity.this, 
                            "Password reset email sent to " + email, 
                            Toast.LENGTH_LONG).show();
                } else {
                    Log.w(TAG, "Error sending reset email", task.getException());
                    Toast.makeText(SignInActivity.this, 
                            "Error sending reset email: " + task.getException().getMessage(), 
                            Toast.LENGTH_LONG).show();
                }
            });
    }

    /**
     * Shows or hides the loading indicator
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.signInButton.setText("");
            binding.signInButton.setEnabled(false);
            // TODO: Add a ProgressBar to the button and show it here
        } else {
            binding.signInButton.setText("Sign In");
            binding.signInButton.setEnabled(true);
            // TODO: Hide the ProgressBar
        }
    }
} 