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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

            val navItems = listOf(
                NavItem("Календарь", Icons.Default.CalendarMonth),
                NavItem("Сегодня", Icons.Default.CalendarToday),
                NavItem("Люди", Icons.Default.SupervisorAccount),
                NavItem("Характеры", Icons.Default.Book)
            )

            DaysignTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            navItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        if (pagerState.currentPage == index && index == 3) {
                                            charactersResetTrigger++
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
                            0 -> CalendarScreen(viewModel)
                            1 -> HomeScreen(viewModel)
                            2 -> PeopleScreen(viewModel)
                            3 -> CharactersScreen(viewModel, charactersResetTrigger)
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

data class NavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)


