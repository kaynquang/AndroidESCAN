package com.quang.escan.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.quang.escan.MainActivity;
import com.quang.escan.R;
import com.quang.escan.databinding.ActivitySignUpBinding;

/**
 * Activity that handles user registration with Firebase Authentication
 */
public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private static final int RC_SIGN_IN = 9001;
    
    private ActivitySignUpBinding binding;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
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

    private void setupClickListeners() {
        // Close button click
        binding.btnClose.setOnClickListener(v -> {
            Log.d(TAG, "Close button clicked, finishing activity");
            finish();
        });

        // Sign up button click
        binding.signUpButton.setOnClickListener(v -> {
            if (validateInputs()) {
                createAccount();
            }
        });

        // Google sign in button click
        binding.googleSignInButton.setOnClickListener(v -> {
            Log.d(TAG, "Google sign-in button clicked");
            signInWithGoogle();
        });

        // Login link click
        binding.signInLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
            // Optional: add transition animation
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
        
        // Toggle confirm password visibility
        binding.toggleConfirmPasswordVisibility.setOnClickListener(v -> {
            confirmPasswordVisible = !confirmPasswordVisible;
            if (confirmPasswordVisible) {
                // Show password
                binding.confirmPassword.setTransformationMethod(null);
                binding.toggleConfirmPasswordVisibility.setImageResource(android.R.drawable.ic_menu_view);
            } else {
                // Hide password
                binding.confirmPassword.setTransformationMethod(new PasswordTransformationMethod());
                binding.toggleConfirmPasswordVisibility.setImageResource(android.R.drawable.ic_menu_view);
            }
            // Move cursor to the end of the text
            binding.confirmPassword.setSelection(binding.confirmPassword.getText().length());
        });
    }

    /**
     * Validates registration inputs
     * @return true if inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        String name = binding.name.getText().toString().trim();
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        String confirmPassword = binding.confirmPassword.getText().toString().trim();
        boolean isValid = true;

        // Check name
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            binding.name.requestFocus();
            isValid = false;
        }

        // Check email
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            binding.email.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            binding.email.requestFocus();
            isValid = false;
        }

        // Check password
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            binding.password.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            binding.password.requestFocus();
            isValid = false;
        }

        // Check confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Confirm your password", Toast.LENGTH_SHORT).show();
            binding.confirmPassword.requestFocus();
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            binding.confirmPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Attempts to register the user with Firebase Authentication
     */
    private void createAccount() {
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
                        navigateToVerification(user);
                    } else {
                        // Sign in failed
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, "Authentication failed", 
                                Toast.LENGTH_SHORT).show();
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
                        navigateToVerification(user);
                    } else {
                        Log.w(TAG, "Error updating user profile", task.getException());
                        // Still consider sign-up successful even if profile update fails
                        navigateToVerification(user);
                    }
                });
        } else {
            showLoading(false);
        }
    }
    
    /**
     * Handles errors that occur during sign-up
     */
    private void handleSignUpError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            // Email already in use
            Toast.makeText(this, "This email is already registered", Toast.LENGTH_LONG).show();
            binding.email.requestFocus();
        } else {
            // Other errors
            Toast.makeText(this, "Registration failed: " + exception.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles successful registration and navigates to verification
     */
    private void navigateToVerification(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
            
            // Navigate to verification screen
            Intent intent = new Intent(SignUpActivity.this, VerificationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Shows or hides the loading indicator
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.signUpButton.setEnabled(false);
            binding.signUpButton.setText("Creating Account...");
            binding.googleSignInButton.setEnabled(false);
        } else {
            binding.signUpButton.setEnabled(true);
            binding.signUpButton.setText("CREATE ACCOUNT");
            binding.googleSignInButton.setEnabled(true);
        }
    }
} 