package com.quang.escan.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.quang.escan.MainActivity;
import com.quang.escan.R;
import com.quang.escan.databinding.ActivitySignUpBinding;

/**
 * Activity that handles user registration with Firebase Authentication
 */
public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private ActivitySignUpBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Sign up button click
        binding.signUpButton.setOnClickListener(v -> {
            if (validateInputs()) {
                attemptSignUp();
            }
        });

        // Sign in link click
        binding.signInLink.setOnClickListener(v -> {
            // Return to sign in screen
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    /**
     * Validates all signup form inputs
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        String name = binding.name.getText().toString().trim();
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        String confirmPassword = binding.confirmPassword.getText().toString().trim();
        boolean isValid = true;

        // Validate name
        if (TextUtils.isEmpty(name)) {
            binding.nameLayout.setError("Name is required");
            isValid = false;
        } else {
            binding.nameLayout.setError(null);
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Please enter a valid email");
            isValid = false;
        } else {
            binding.emailLayout.setError(null);
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            binding.passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            binding.passwordLayout.setError(null);
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.confirmPasswordLayout.setError("Confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        } else {
            binding.confirmPasswordLayout.setError(null);
        }

        return isValid;
    }

    /**
     * Attempts to register the user with Firebase Authentication
     */
    private void attemptSignUp() {
        // Show loading indicator
        showLoading(true);

        String name = binding.name.getText().toString().trim();
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        // Create user with Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign up success
                    Log.d(TAG, "createUserWithEmail:success");
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    
                    // Update user profile with display name
                    updateUserProfile(user, name);
                } else {
                    // Sign up failed
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    handleSignUpError(task.getException());
                    
                    // Hide loading indicator
                    showLoading(false);
                }
            });
    }
    
    /**
     * Updates the user profile with the provided display name
     */
    private void updateUserProfile(FirebaseUser user, String name) {
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
                    
            user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated with name: " + name);
                        signUpSuccess(user);
                    } else {
                        Log.w(TAG, "Error updating user profile", task.getException());
                        // Still consider sign-up successful even if profile update fails
                        signUpSuccess(user);
                    }
                });
        }
    }
    
    /**
     * Handles errors that occur during sign-up
     */
    private void handleSignUpError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            // Email already in use
            binding.emailLayout.setError("Email already in use");
        } else {
            // Other errors
            Toast.makeText(this, "Registration failed: " + exception.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles successful registration
     */
    private void signUpSuccess(FirebaseUser user) {
        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
        
        // Navigate to main activity
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Shows or hides the loading indicator
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.signUpButton.setText("");
            binding.signUpButton.setEnabled(false);
            // TODO: Add a ProgressBar to the button and show it here
        } else {
            binding.signUpButton.setText("Create Account");
            binding.signUpButton.setEnabled(true);
            // TODO: Hide the ProgressBar
        }
    }
} 