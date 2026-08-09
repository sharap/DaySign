package calendar.maya.daysign.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
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
                title = { Text(stringResource(R.string.characters_title)) },
                actions = {
                    val isWide = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT
                    if (!isWide) {
                        IconButton(onClick = onAboutClick) {
                            Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.about_app))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val scope = this
            val isCompact = scope.maxWidth < 600.dp
            val groups = listOf(
                stringResource(R.string.group_power) to listOf(1, 6, 11, 16),
                stringResource(R.string.group_desire) to listOf(2, 7, 12, 17),
                stringResource(R.string.group_goal) to listOf(3, 8, 13, 18),
                stringResource(R.string.group_resource) to listOf(4, 9, 14, 19),
                stringResource(R.string.group_decision) to listOf(5, 10, 15, 20)
            )

            LazyVerticalGrid(
                columns = if (isCompact) GridCells.Fixed(4) else GridCells.Adaptive(minSize = 140.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groups.forEach { (title, ids) ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    items(ids) { id ->
                        CharacterCard(
                            id = id,
                            name = daysignNames.getOrElse(id) { "" },
                            onClick = { onCharacterClick(id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterCard(id: Int, name: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .aspectRatio(0.75f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ImageSign(sign = id, size = 56.dp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                maxLines = 2
            )
        }
    }
}
