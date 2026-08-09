package calendar.maya.daysign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import calendar.maya.daysign.ui.MainViewModel
import calendar.maya.daysign.ui.screens.CalendarScreen
import calendar.maya.daysign.ui.screens.CharactersScreen
import calendar.maya.daysign.ui.screens.HomeScreen
import calendar.maya.daysign.ui.screens.PeopleScreen
import calendar.maya.daysign.ui.theme.DaysignTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val pagerState = rememberPagerState(initialPage = 1) { 4 }
            val coroutineScope = rememberCoroutineScope()
            
            var charactersResetTrigger by remember { mutableIntStateOf(0) }
            var peopleResetTrigger by remember { mutableIntStateOf(0) }
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

            val navItems = listOf(
                NavItem(stringResource(R.string.calendar_title), Icons.Default.CalendarMonth),
                NavItem(stringResource(R.string.today), Icons.Default.CalendarToday),
                NavItem(stringResource(R.string.people), Icons.Default.SupervisorAccount),
                NavItem(stringResource(R.string.characters_title), Icons.Default.Book)
            )

            DaysignTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            navItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = pagerState.currentPage == index,
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
                            }
                        }
                    }
                ) { innerPadding ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.padding(innerPadding)
                    ) { page ->
                        when (page) {
                            0 -> CalendarScreen(viewModel, calendarResetTrigger)
                            1 -> HomeScreen(viewModel)
                            2 -> PeopleScreen(viewModel, peopleResetTrigger)
                            3 -> {
                                CharactersScreen(viewModel, charactersResetTrigger, targetCharacterId)
                                if (targetCharacterId != null && pagerState.currentPage == 3) {
                                    targetCharacterId = null
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

data class NavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)


