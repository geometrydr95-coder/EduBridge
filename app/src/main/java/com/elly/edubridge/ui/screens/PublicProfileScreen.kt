package com.elly.edubridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.elly.edubridge.viewmodel.PublicProfileState
import com.elly.edubridge.viewmodel.PublicProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PublicProfileScreen(
    navController: NavHostController,
    userId: String,
    viewModel: PublicProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is PublicProfileState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is PublicProfileState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is PublicProfileState.Success -> {
                val user = state.user
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Header Gradient & Profile Image
                    Box(modifier = Modifier.height(200.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                                    )
                                )
                        )
                        AsyncImage(
                            model = user.profileImage,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // User Info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (user.isVerified) {
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, "Verified", tint = Color(0xFF1DA1F2), modifier = Modifier.size(20.dp))
                            }
                        }
                        Text(
                            text = "@${user.username} • ${user.institution}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = user.headline,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(16.dp))

                        // Action Button
                        Button(
                            onClick = { /* Implemented via dialog logic below */ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Request Skill Exchange")
                        }
                    }

                    // Bio Section
                    ProfileSection(title = "About") {
                        Text(
                            text = user.bio.ifEmpty { "No bio provided." },
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }

                    // Skills Offered
                    ProfileSection(title = "Skills I Can Teach") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            user.skillsOffered.forEach { skill ->
                                SuggestionChip(onClick = {}, label = { Text(skill) })
                            }
                        }
                    }

                    // Skills Wanted
                    ProfileSection(title = "Skills I Want to Learn") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            user.skillsWanted.forEach { skill ->
                                AssistChip(onClick = {}, label = { Text(skill) })
                            }
                        }
                    }

                    Spacer(Modifier.height(40.dp))

                    var showRequestDialog by remember { mutableStateOf(false) }
                    val isSending by viewModel.sendRequestState.collectAsState()

                    if (showRequestDialog) {
                        AlertDialog(
                            onDismissRequest = { showRequestDialog = false },
                            title = { Text("Propose an Exchange") },
                            text = {
                                Column {
                                    Text("What do you want to learn from ${user.fullName}?")
                                    Text("Skill: ${user.skillsOffered.firstOrNull() ?: "General"}", fontWeight = FontWeight.Bold)
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.sendRequest(
                                            receiver = user,
                                            skillOffered = "My Skill",
                                            skillWanted = user.skillsOffered.firstOrNull() ?: ""
                                        )
                                        showRequestDialog = false
                                    },
                                    enabled = !isSending
                                ) {
                                    if (isSending) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Send Request")
                                    }
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showRequestDialog = false }) { Text("Cancel") }
                            }
                        )
                    }

                    Button(
                        onClick = { showRequestDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Request Skill Exchange")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        content()
        HorizontalDivider(Modifier.padding(top = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
    }
}
