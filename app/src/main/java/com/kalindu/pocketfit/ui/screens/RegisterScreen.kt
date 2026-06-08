package com.kalindu.pocketfit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(value = false) }
    var confirmPasswordVisible by remember { mutableStateOf(value = false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Get authentication state from ViewModel
    val authState by remember { authViewModel.authState }

    LaunchedEffect(Unit) {
        authViewModel.clearError()
    }

    // When registration is successful, navigate to home
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onRegisterSuccess()
            authViewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Join PocketFit",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Start your fitness journey today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = null
            },
            label = { Text("Full Name") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Name"
                )
            },
            isError = nameError != null,
            supportingText = nameError?.let { message ->
                { Text(message) }
            },
            singleLine = true,
            enabled = authState !is AuthState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
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
                confirmPasswordError = null
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

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = null
            },
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Confirm Password"
                )
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible)
                            Icons.Default.Visibility
                        else
                            Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible)
                            "Hide password"
                        else
                            "Show password"
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError?.let { message ->
                { Text(message) }
            },
            singleLine = true,
            enabled = authState !is AuthState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Status Message Display
        if (authState is AuthState.Error || authState is AuthState.Authenticated) {
            val (color, message) = when (authState) {
                is AuthState.Error -> Color.Red to (authState as AuthState.Error).message
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

        // Register Button
        Button(
            onClick = {
                nameError = AuthValidation.nameError(name)
                emailError = AuthValidation.emailError(email)
                passwordError = AuthValidation.passwordError(password)
                confirmPasswordError =
                    AuthValidation.confirmPasswordError(password, confirmPassword)

                if (
                    nameError == null &&
                    emailError == null &&
                    passwordError == null &&
                    confirmPasswordError == null
                ) {
                    authViewModel.register(email.trim(), password, name.trim())
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
                text = if (authState is AuthState.Loading) "Creating Account..." else "Create Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = {
                    authViewModel.clearError()
                    onLoginClick()
                },
                enabled = authState !is AuthState.Loading
            ) {
                Text(
                    text = "Login",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
