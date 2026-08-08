package calendar.maya.daysign.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import calendar.maya.daysign.R
import calendar.maya.daysign.ui.MainViewModel
import calendar.maya.daysign.ui.components.ImageSign

@Composable
fun CharactersScreen(viewModel: MainViewModel, resetTrigger: Int = 0, targetCharacterId: Int? = null) {
    val navController = rememberNavController()

    LaunchedEffect(resetTrigger) {
        if (resetTrigger > 0) {
            navController.popBackStack(navController.graph.startDestinationId, false)
        }
    }

    LaunchedEffect(targetCharacterId) {
        targetCharacterId?.let { id ->
            navController.navigate("detail/$id") {
                // Avoid multiple instances on stack if needed
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            CharactersList(
                onCharacterClick = { id ->
                    navController.navigate("detail/$id")
                },
                onAboutClick = {
                    navController.navigate("about")
                }
            )
        }
        composable(
            route = "detail/{characterId}",
            arguments = listOf(navArgument("characterId") { type = NavType.IntType })
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getInt("characterId") ?: 1
            CharacterDetailScreen(
                characterId = characterId,
                onBack = { navController.popBackStack() }
            )
            
            BackHandler {
                navController.popBackStack()
            }
        }
        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
            
            BackHandler {
                navController.popBackStack()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersList(onCharacterClick: (Int) -> Unit, onAboutClick: () -> Unit) {
    val daysignNames = stringArrayResource(id = R.array.daysign_names)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Характеры") },
                actions = {
                    IconButton(onClick = onAboutClick) {
                        Icon(Icons.Outlined.Info, contentDescription = "О приложении")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CharacterGroup("Сила", listOf(1, 6, 11, 16), daysignNames, onCharacterClick)
            CharacterGroup("Желание", listOf(2, 7, 12, 17), daysignNames, onCharacterClick)
            CharacterGroup("Цель", listOf(3, 8, 13, 18), daysignNames, onCharacterClick)
            CharacterGroup("Ресурс", listOf(4, 9, 14, 19), daysignNames, onCharacterClick)
            CharacterGroup("Решение", listOf(5, 10, 15, 20), daysignNames, onCharacterClick)
        }
    }
}

@Composable
fun CharacterGroup(
    title: String,
    ids: List<Int>,
    names: Array<String>,
    onCharacterClick: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ids.forEach { id ->
                CharacterCard(
                    id = id,
                    name = names.getOrElse(id) { "" },
                    modifier = Modifier.weight(1f),
                    onClick = { onCharacterClick(id) }
                )
            }
        }
    }
}

@Composable
fun CharacterCard(id: Int, name: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .aspectRatio(0.7f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ImageSign(sign = id, size = 60.dp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
