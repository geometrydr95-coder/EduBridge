package com.elly.edubridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.elly.edubridge.data.model.User
import com.elly.edubridge.viewmodel.MarketplaceState
import com.elly.edubridge.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    navController: NavHostController,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {

    val marketplaceState by viewModel.marketplaceState.collectAsState()

    var searchQuery by remember {
        mutableStateOf("")
    }

    val categories = listOf(
        "All",
        "Tech",
        "Design",
        "Business",
        "Music",
        "Academic"
    )

    Scaffold(
        topBar = {

            TopAppBar(
                title = {
                    Text(
                        text = "Discover Skills",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchUsers(it)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, null)
                },
                placeholder = {
                    Text("Search skills or students")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(18.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                categories.forEach { category ->

                    FilterChip(
                        selected = false,
                        onClick = {
                            viewModel.filterBySkill(category)
                        },
                        label = {
                            Text(category)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (marketplaceState) {

                is MarketplaceState.Loading -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is MarketplaceState.Error -> {

                    val message =
                        (marketplaceState as MarketplaceState.Error).message

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(message)
                    }
                }

                is MarketplaceState.Success -> {

                    val users =
                        (marketplaceState as MarketplaceState.Success).users

                    if (users.isEmpty()) {

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = "No students found",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                    } else {

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            items(users) { user ->

                                MarketplaceUserCard(
                                    user = user,
                                    onClick = {
                                        navController.navigate(
                                            "public_profile/${user.userId}"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketplaceUserCard(
    user: User,
    onClick: () -> Unit
) {

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 6.dp
        )
    ) {

        Column {

            AsyncImage(
                model = user.profileImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(14.dp)
            ) {

                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user.headline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    user.skillsOffered.take(2).forEach { skill ->

                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(skill)
                            }
                        )
                    }
                }
            }
        }
    }
}