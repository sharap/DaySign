package calendar.maya.daysign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowWidthSizeClass
import calendar.maya.daysign.ui.MainViewModel
import calendar.maya.daysign.ui.screens.AboutScreen
import calendar.maya.daysign.ui.screens.CalendarScreen
import calendar.maya.daysign.ui.screens.CharactersScreen
import calendar.maya.daysign.ui.screens.HomeScreen
import calendar.maya.daysign.ui.screens.PeopleScreen
import calendar.maya.daysign.ui.screens.PeopleScreenMode
import calendar.maya.daysign.ui.theme.DaysignTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val pagerState = rememberPagerState(initialPage = 1) { 6 }
            val coroutineScope = rememberCoroutineScope()
            
            var charactersResetTrigger by remember { mutableIntStateOf(0) }
            var peopleResetTrigger by remember { mutableIntStateOf(0) }
            var groupsResetTrigger by remember { mutableIntStateOf(0) }
            var calendarResetTrigger by remember { mutableIntStateOf(0) }
            var targetCharacterId by remember { mutableStateOf<Int?>(null) }

            LaunchedEffect(viewModel.navigateToCharacter) {
                viewModel.navigateToCharacter.collect { characterId ->
                    targetCharacterId = characterId
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(3)
                    }
                }
            }

            LaunchedEffect(viewModel.navigateToPage) {
                viewModel.navigateToPage.collect { pageIndex ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pageIndex)
                    }
                }
            }

            val adaptiveInfo = currentWindowAdaptiveInfo()
            val isWide = adaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

            val navItems = listOf(
                NavItem(stringResource(R.string.calendar_title), Icons.Default.CalendarMonth),
                NavItem(stringResource(R.string.today), Icons.Default.CalendarToday),
                NavItem(stringResource(R.string.people), Icons.Default.SupervisorAccount),
                NavItem(stringResource(R.string.characters_title), Icons.Default.Book)
            )
            val groupsItem = NavItem(stringResource(R.string.groups), Icons.Default.Group)
            val aboutItem = NavItem(stringResource(R.string.about), Icons.Default.Info)

            DaysignTheme {
                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        navItems.forEachIndexed { index, item ->
                            // Hide Calendar (index 0) on wide screens as it's merged with Today
                            if (isWide && index == 0) return@forEachIndexed
                            
                            item(
                                selected = (pagerState.currentPage == index) || (isWide && index == 1 && pagerState.currentPage == 0),
                                onClick = {
                                    if (pagerState.currentPage == index) {
                                        when (index) {
                                            0 -> calendarResetTrigger++
                                            2 -> peopleResetTrigger++
                                            3 -> charactersResetTrigger++
                                        }
                                    }
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) }
                            )

                            if (isWide && index == 2) {
                                item(
                                    selected = pagerState.currentPage == 5,
                                    onClick = {
                                        if (pagerState.currentPage == 5) groupsResetTrigger++
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(5)
                                        }
                                    },
                                    icon = { Icon(groupsItem.icon, contentDescription = groupsItem.label) },
                                    label = { Text(groupsItem.label) }
                                )
                            }
                        }
                        if (isWide) {
                            item(
                                selected = pagerState.currentPage == 4,
                                onClick = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(4) }
                                },
                                icon = { Icon(aboutItem.icon, contentDescription = aboutItem.label) },
                                label = { Text(aboutItem.label) }
                            )
                        }
                    }
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = !isWide
                    ) { page ->
                        when (page) {
                            0, 1 -> {
                                if (isWide) {
                                    Row(modifier = Modifier.fillMaxSize()) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            HomeScreen(viewModel)
                                        }
                                        VerticalDivider()
                                        Box(modifier = Modifier.weight(1f)) {
                                            CalendarScreen(viewModel, calendarResetTrigger)
                                        }
                                    }
                                } else {
                                    if (page == 0) CalendarScreen(viewModel, calendarResetTrigger)
                                    else HomeScreen(viewModel)
                                }
                            }
                            2 -> PeopleScreen(
                                viewModel = viewModel,
                                resetTrigger = peopleResetTrigger,
                                mode = if (isWide) PeopleScreenMode.PEOPLE_ONLY else PeopleScreenMode.ALL
                            )
                            3 -> {
                                CharactersScreen(viewModel, charactersResetTrigger, targetCharacterId)
                                if (targetCharacterId != null && pagerState.currentPage == 3) {
                                    targetCharacterId = null
                                }
                            }
                            4 -> AboutScreen(onBack = { 
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            })
                            5 -> PeopleScreen(
                                viewModel = viewModel,
                                resetTrigger = groupsResetTrigger,
                                mode = PeopleScreenMode.GROUPS_ONLY
                            )
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

data class NavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
