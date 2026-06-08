package com.kalindu.pocketfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalindu.pocketfit.ui.viewmodel.AuthState
import com.kalindu.pocketfit.ui.viewmodel.AuthViewModel
import com.kalindu.pocketfit.utils.AuthValidation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(value = false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Get authentication state from ViewModel
    val authState by remember { authViewModel.authState }

    LaunchedEffect(Unit) {
        authViewModel.clearError()
    }

    // When login is successful, navigate to home
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
            authViewModel.clearError()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title
            Text(
                text = "PocketFit",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Your Fitness Companion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                isError = emailError != null,
                supportingText = emailError?.let { message ->
                    { Text(message) }
                },
                singleLine = true,
                enabled = authState !is AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Hide password"
                            else
                                "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                isError = passwordError != null,
                supportingText = passwordError?.let { message ->
                    { Text(message) }
                },
                singleLine = true,
                enabled = authState !is AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Forgot Password Link
            TextButton(
                onClick = {
                    emailError = AuthValidation.emailError(email)
                    if (emailError == null) {
                        authViewModel.resetPassword(email.trim())
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 16.dp),
                enabled = authState !is AuthState.Loading
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Status/Error Message Display
            if (authState is AuthState.Error || authState is AuthState.Success || authState is AuthState.Authenticated) {
                val (color, message) = when (authState) {
                    is AuthState.Error -> MaterialTheme.colorScheme.error to (authState as AuthState.Error).message
                    is AuthState.Success -> Color(0xFF4CAF50) to (authState as AuthState.Success).message
                    is AuthState.Authenticated -> Color(0xFF4CAF50) to (authState as AuthState.Authenticated).message
                    else -> Color.Transparent to ""
                }
                
                Text(
                    text = message,
                    color = color,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    fontSize = 12.sp
                )
            }

            // Login Button
            Button(
                onClick = {
                    emailError = AuthValidation.emailError(email)
                    passwordError = AuthValidation.passwordError(password)
                    if (emailError == null && passwordError == null) {
                        authViewModel.login(email.trim(), password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(24.dp)
                            .padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = if (authState is AuthState.Loading) "Logging in..." else "Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        authViewModel.clearError()
                        onRegisterClick()
                    },
                    enabled = authState !is AuthState.Loading
                ) {
                    Text(
                        text = "Register",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
