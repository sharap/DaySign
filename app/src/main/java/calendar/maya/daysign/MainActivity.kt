package calendar.maya.daysign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import calendar.maya.daysign.ui.MainViewModel
import calendar.maya.daysign.ui.screens.HomeScreen
import calendar.maya.daysign.ui.screens.PeopleScreen
import calendar.maya.daysign.ui.theme.DaysignTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val navController = rememberNavController()
            
            DaysignTheme {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToPeople = { navController.navigate("people") }
                        )
                    }
                    composable("people") {
                        PeopleScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
