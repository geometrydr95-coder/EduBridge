package com.elly.edubridge.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.elly.edubridge.data.model.User
import com.elly.edubridge.ui.theme.EDUBRIDGE3Theme
import com.elly.edubridge.viewmodel.ProfileState
import com.elly.edubridge.viewmodel.ProfileViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(navController: NavHostController, viewModel: ProfileViewModel = hiltViewModel()) {
    var step by remember { mutableIntStateOf(1) }
    val currentUser by viewModel.currentUser.collectAsState()
    val profileState by viewModel.profileState.collectAsState()
    val isUsernameAvailable by viewModel.isUsernameAvailable.collectAsState()
    val context = LocalContext.current

    // Form State
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var headline by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            if (fullName.isEmpty()) fullName = it.fullName
            if (username.isEmpty()) username = it.username
            if (phoneNumber.isEmpty()) phoneNumber = it.phoneNumber
            if (institution.isEmpty()) institution = it.institution
            if (headline.isEmpty()) headline = it.headline
            if (bio.isEmpty()) bio = it.bio
        }
    }

    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            navController.navigate("skill_selection") {
                popUpTo("complete_profile") { inclusive = true }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Set Up Your Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (step > 1) {
                        IconButton(onClick = { step-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Indicator
            LinearProgressIndicator(
                progress = { step / 3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            when (step) {
                1 -> {
                    Text("Basic Info", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Help us identify you on campus", color = MaterialTheme.colorScheme.secondary)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { 
                            username = it
                            viewModel.checkUsername(it)
                        },
                        label = { Text("Unique Username") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        isError = isUsernameAvailable == false,
                        supportingText = {
                            if (isUsernameAvailable == false) {
                                Text("Username already taken", color = MaterialTheme.colorScheme.error)
                            } else if (isUsernameAvailable == true && username.isNotEmpty()) {
                                Text("Username available!", color = Color(0xFF00C853))
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = institution,
                        onValueChange = { institution = it },
                        label = { Text("Institution / University") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                }
                2 -> {
                    Text("Profile Photo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Accounts with photos get 3x more trades", color = MaterialTheme.colorScheme.secondary)
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AccountCircle, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text("Upload", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                3 -> {
                    Text("About You", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Briefly describe what you do", color = MaterialTheme.colorScheme.secondary)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    OutlinedTextField(
                        value = headline,
                        onValueChange = { headline = it },
                        label = { Text("Professional Headline (e.g. Logo Designer)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Short Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (profileState is ProfileState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (step < 3) {
                            if (step == 1 && (isUsernameAvailable == false || username.isEmpty())) return@Button
                            step++
                        } else {
                            val updatedUser = currentUser?.copy(
                                fullName = fullName,
                                username = username,
                                institution = institution,
                                headline = headline,
                                bio = bio
                            ) ?: User(
                                fullName = fullName,
                                username = username,
                                institution = institution,
                                headline = headline,
                                bio = bio
                            )
                            
                            var file: File? = null
                            imageUri?.let { uri ->
                                val inputStream = context.contentResolver.openInputStream(uri)
                                file = File(context.cacheDir, "profile_image.jpg")
                                val outputStream = FileOutputStream(file)
                                inputStream?.copyTo(outputStream)
                                inputStream?.close()
                                outputStream.close()
                            }
                            
                            viewModel.updateProfile(updatedUser, file)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(if (step < 3) "Continue" else "Complete Profile")
                }
            }
            
            if (profileState is ProfileState.Error) {
                Text(
                    text = (profileState as ProfileState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompleteProfileScreenPreview() {
    EDUBRIDGE3Theme {
        CompleteProfileScreen(navController = rememberNavController())
    }
}
