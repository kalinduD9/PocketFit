package com.kalindu.pocketfit.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.request.ImageRequest
import coil.compose.rememberAsyncImagePainter
import com.kalindu.pocketfit.ui.viewmodel.AuthState
import com.kalindu.pocketfit.ui.viewmodel.AuthViewModel
import com.kalindu.pocketfit.ui.viewmodel.NameUpdateState
import com.kalindu.pocketfit.ui.viewmodel.ProfileDetailsUiState
import com.kalindu.pocketfit.ui.viewmodel.ProfilePhotoUiState
import com.kalindu.pocketfit.ui.viewmodel.ProfileViewModel
import com.kalindu.pocketfit.ui.viewmodel.SessionViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    sessionViewModel: SessionViewModel,
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    val authState by authViewModel.authState
    val nameUpdateState by authViewModel.nameUpdateState
    val profilePhotoUri by profileViewModel.photoUri.collectAsState()
    val photoRevision by profileViewModel.photoRevision.collectAsState()
    val photoState by profileViewModel.photoState.collectAsState()
    val profileDetails by profileViewModel.profileDetails.collectAsState()
    val detailsState by profileViewModel.detailsState.collectAsState()
    val sessions by sessionViewModel.sessions.collectAsState()
    val isPhotoActionRunning = photoState is ProfilePhotoUiState.Saving

    // Profile Picture State
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoMenu by remember { mutableStateOf(false) }
    var showRemoveConfirmation by remember { mutableStateOf(false) }

    // Initialize name and email from the currently logged-in Firebase user.
    val name = authViewModel.currentUserName
    val email = authViewModel.currentUserEmail
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember(name) { mutableStateOf(name) }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    LaunchedEffect(profileDetails, isEditing) {
        if (!isEditing) {
            weight = profileDetails.weightKg?.let { "%.1f".format(it) }.orEmpty()
            height = profileDetails.heightCm?.toString().orEmpty()
            age = profileDetails.age?.toString().orEmpty()
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            val capturedUri = tempImageUri
            tempImageUri = null
            if (success && capturedUri != null) {
                profileViewModel.captureCompleted(capturedUri)
            } else {
                profileViewModel.captureCancelled(capturedUri)
            }
        }
    )

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchProfileCamera(
                context = context,
                onUriCreated = { uri ->
                    tempImageUri = uri
                    cameraLauncher.launch(uri)
                },
                onError = profileViewModel::reportError
            )
        } else {
            profileViewModel.reportError(
                "Camera permission is required to take a profile picture."
            )
        }
    }

    if (showRemoveConfirmation) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmation = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Remove profile picture?") },
            text = { Text("Your current profile picture will be removed from your account.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRemoveConfirmation = false
                        profileViewModel.removePhoto()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile picture with edit badge
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(100.dp)
                        ) {
                            if (profilePhotoUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(context)
                                            .data(profilePhotoUri)
                                            .memoryCacheKey("${profilePhotoUri}_$photoRevision")
                                            .diskCacheKey("${profilePhotoUri}_$photoRevision")
                                            .crossfade(true)
                                            .build()
                                    ),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Picture",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(25.dp)
                                )
                            }
                        }

                        if (isPhotoActionRunning) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f),
                                modifier = Modifier.size(100.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                        }

                        // Camera edit badge
                        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                IconButton(
                                    onClick = { showPhotoMenu = true },
                                    enabled = !isPhotoActionRunning,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Profile picture options",
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showPhotoMenu,
                                onDismissRequest = { showPhotoMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (profilePhotoUri == null) {
                                                "Take profile picture"
                                            } else {
                                                "Replace profile picture"
                                            }
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                                    },
                                    onClick = {
                                        showPhotoMenu = false
                                        profileViewModel.clearPhotoState()
                                        val permissionCheckResult = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        )
                                        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                            launchProfileCamera(
                                                context = context,
                                                onUriCreated = { uri ->
                                                    tempImageUri = uri
                                                    cameraLauncher.launch(uri)
                                                },
                                                onError = profileViewModel::reportError
                                            )
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }
                                )

                                if (profilePhotoUri != null) {
                                    DropdownMenuItem(
                                        text = { Text("Remove profile picture") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            showPhotoMenu = false
                                            showRemoveConfirmation = true
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (val state = photoState) {
                        is ProfilePhotoUiState.Error -> {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        is ProfilePhotoUiState.Success -> {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        else -> Unit
                    }

                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Account Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isEditingName) {
                            TextButton(
                                onClick = {
                                    editedName = name
                                    isEditingName = false
                                    authViewModel.clearNameUpdateState()
                                },
                                enabled = nameUpdateState !is NameUpdateState.Loading
                            ) {
                                Text("Cancel")
                            }
                        }
                        FilledTonalButton(
                            onClick = {
                                if (isEditingName) {
                                    authViewModel.updateName(editedName)
                                } else {
                                    editedName = name
                                    authViewModel.clearNameUpdateState()
                                    isEditingName = true
                                }
                            },
                            enabled = nameUpdateState !is NameUpdateState.Loading,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (nameUpdateState is NameUpdateState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isEditingName) {
                                        Icons.Default.Check
                                    } else {
                                        Icons.Default.Edit
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isEditingName) "Save" else "Edit")
                            }
                        }
                    }
                }

                ProfileDetailRow(
                    icon = Icons.Default.Person,
                    label = "Full Name",
                    value = editedName,
                    isEditing = isEditingName,
                    onValueChange = { editedName = it }
                )

                when (val state = nameUpdateState) {
                    is NameUpdateState.Error -> Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    is NameUpdateState.Success -> {
                        LaunchedEffect(state) {
                            isEditingName = false
                            editedName = name
                        }
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> Unit
                }

                HorizontalDivider()

                ProfileDetailRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = email
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Get a reset link by email.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FilledTonalButton(
                        onClick = { authViewModel.resetPassword(email) },
                        enabled = authState !is AuthState.Loading && email.isNotBlank()
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset")
                        }
                    }
                }

                when (val state = authState) {
                    is AuthState.Error -> Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    is AuthState.Success -> Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    else -> Unit
                }
            }
        }

        // Personal Details Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Personal Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    FilledTonalButton(
                        onClick = {
                            if (isEditing) {
                                if (profileViewModel.saveDetails(weight, height, age)) {
                                    isEditing = false
                                }
                            } else {
                                profileViewModel.clearDetailsState()
                                isEditing = true
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save" else "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEditing) "Save" else "Edit", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (val state = detailsState) {
                    is ProfileDetailsUiState.Error -> Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    is ProfileDetailsUiState.Success -> Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    ProfileDetailsUiState.Idle -> Unit
                }

                ProfileDetailRow(
                    icon = Icons.Default.FitnessCenter,
                    label = "Weight",
                    value = weight,
                    isEditing = isEditing,
                    onValueChange = { weight = it },
                    suffix = "kg"
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                ProfileDetailRow(
                    icon = Icons.Default.Height,
                    label = "Height",
                    value = height,
                    isEditing = isEditing,
                    onValueChange = { height = it },
                    suffix = "cm"
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                ProfileDetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Age",
                    value = age,
                    isEditing = isEditing,
                    onValueChange = { age = it }
                )
            }
        }

        // Statistics Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Your Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(
                        emoji = "📊",
                        value = sessions.size.toString(),
                        label = "Sessions"
                    )
                    StatBox(
                        emoji = "🚶",
                        value = sessions.sumOf { it.steps }.toString(),
                        label = "Steps"
                    )
                    StatBox(
                        emoji = "🔥",
                        value = sessions.sumOf { it.calories }.toString(),
                        label = "Calories"
                    )
                }
            }
        }

        // Logout Button
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Logout",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun launchProfileCamera(
    context: Context,
    onUriCreated: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    runCatching {
        createTempImageUri(context)
    }.onSuccess(onUriCreated)
        .onFailure {
            onError("Unable to prepare the camera. Please try again.")
        }
}
private fun createTempImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = File(context.cacheDir, "images").apply {
        if (!exists() && !mkdirs()) {
            error("Unable to create the image cache directory.")
        }
    }
    val file = File.createTempFile(imageFileName, ".jpg", storageDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean = false,
    onValueChange: (String) -> Unit = {},
    suffix: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    suffix = { if (suffix.isNotEmpty()) Text(suffix) }
                )
            } else {
                val displayValue = if (suffix.isNotEmpty() && value.isNotEmpty()) "$value $suffix" else value
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StatBox(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 28.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
