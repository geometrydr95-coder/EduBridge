package com.elly.edubridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.elly.edubridge.ui.theme.EDUBRIDGE3Theme
import com.elly.edubridge.viewmodel.ProfileState
import com.elly.edubridge.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SkillSelectionScreen(navController: NavHostController, viewModel: ProfileViewModel = hiltViewModel()) {
    var selectedOffered by remember { mutableStateOf(setOf<String>()) }
    var selectedWanted by remember { mutableStateOf(setOf<String>()) }
    var isOfferedSelection by remember { mutableStateOf(true) }
    
    val currentUser by viewModel.currentUser.collectAsState()
    val profileState by viewModel.profileState.collectAsState()
    val availableSkills by viewModel.availableSkills.collectAsState()

    // Local Fallback if Firestore is empty during first run
    val skillsToShow = if (availableSkills.isEmpty()) {
        listOf(
            "Kotlin", "Java", "Python", "UI Design", "Figma", 
            "Graphic Design", "Video Editing", "Accounting", 
            "Public Speaking", "Math Tutoring", "Physics", "English"
        )
    } else {
        availableSkills.map { it.name }
    }

    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            if (!isOfferedSelection) {
                navController.navigate("home") {
                    popUpTo("skill_selection") { inclusive = true }
                }
            } else {
                isOfferedSelection = false
                viewModel.resetState()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (isOfferedSelection) "Skills You Offer" else "Skills You Want", 
                        fontWeight = FontWeight.Bold 
                    ) 
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = if (isOfferedSelection) 
                    "What can you teach others?" 
                else 
                    "What would you like to learn?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Select at least 3 skills to build a strong profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search skills...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            FlowRow(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                skillsToShow.forEach { skill ->
                    val isSelected = if (isOfferedSelection) 
                        selectedOffered.contains(skill) 
                    else 
                        selectedWanted.contains(skill)
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isOfferedSelection) {
                                selectedOffered = if (isSelected) selectedOffered - skill else selectedOffered + skill
                            } else {
                                selectedWanted = if (isSelected) selectedWanted - skill else selectedWanted + skill
                            }
                        },
                        label = { Text(skill, modifier = Modifier.padding(vertical = 4.dp)) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isOfferedSelection) {
                        // Normally we'd save offered here, but for smooth flow we save all at once
                        isOfferedSelection = false
                    } else {
                        currentUser?.let { user ->
                            viewModel.updateProfile(user.copy(
                                skillsOffered = selectedOffered.toList(),
                                skillsWanted = selectedWanted.toList()
                            ))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = if (isOfferedSelection) selectedOffered.isNotEmpty() else selectedWanted.isNotEmpty(),
                shape = MaterialTheme.shapes.medium
            ) {
                if (profileState is ProfileState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        if (isOfferedSelection) "Continue to Skills Wanted" else "Finish Setup",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SkillSelectionScreenPreview() {
    EDUBRIDGE3Theme {
        SkillSelectionScreen(navController = rememberNavController())
    }
}
