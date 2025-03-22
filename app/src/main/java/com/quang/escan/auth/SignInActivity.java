package com.quang.escan.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.quang.escan.MainActivity;
import com.quang.escan.R;
import com.quang.escan.databinding.ActivitySignInBinding;

/**
 * Activity that handles user sign-in with Firebase Authentication
 */
public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    
    private ActivitySignInBinding binding;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        
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
        // Close button click
        binding.btnClose.setOnClickListener(v -> {
            Log.d(TAG, "Close button clicked, finishing activity");
            finish();
        });
        
        // Sign in button click
        binding.signInButton.setOnClickListener(v -> {
            if (validateInputs()) {
                attemptSignIn();
            }
        });

        // Google sign in button click
        binding.googleSignInButton.setOnClickListener(v -> {
            Log.d(TAG, "Google sign-in button clicked");
            signInWithGoogle();
        });

        // Forgot password click
        binding.forgotPassword.setOnClickListener(v -> {
            String email = binding.email.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show();
                binding.email.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
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
        
        // Toggle password visibility
        binding.togglePasswordVisibility.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                // Show password
                binding.password.setTransformationMethod(null);
                binding.togglePasswordVisibility.setImageResource(android.R.drawable.ic_menu_view);
            } else {
                // Hide password
                binding.password.setTransformationMethod(new PasswordTransformationMethod());
                binding.togglePasswordVisibility.setImageResource(android.R.drawable.ic_menu_view);
            }
            // Move cursor to the end of the text
            binding.password.setSelection(binding.password.getText().length());
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
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Check password
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            isValid = false;
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
     * Starts the Google sign-in process
     */
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Authenticate with Firebase using the Google ID token
     */
    private void firebaseAuthWithGoogle(String idToken) {
        showLoading(true);
        
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        signInSuccess(user);
                    } else {
                        // Sign in failed
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(SignInActivity.this, "Authentication failed", 
                                Toast.LENGTH_SHORT).show();
                    }
                    
                    showLoading(false);
                });
    }

    private void handleSignInError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(this, "No user found with this email.", Toast.LENGTH_LONG).show();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(this, "Invalid password.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Authentication failed: " + exception.getMessage(), 
                    Toast.LENGTH_LONG).show();
        }
    }

    private void signInSuccess(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
            navigateToMainActivity();
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendPasswordResetEmail(String email) {
        showLoading(true);
        
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Password reset email sent");
                        Toast.makeText(SignInActivity.this, 
                                "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Error sending password reset email", task.getException());
                        Toast.makeText(SignInActivity.this, 
                                "Error sending password reset email: " + task.getException().getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                    
                    showLoading(false);
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.signInButton.setEnabled(false);
            binding.signInButton.setText("Signing In...");
            binding.googleSignInButton.setEnabled(false);
        } else {
            binding.signInButton.setEnabled(true);
            binding.signInButton.setText("LOG IN");
            binding.googleSignInButton.setEnabled(true);
        }
    }
} 