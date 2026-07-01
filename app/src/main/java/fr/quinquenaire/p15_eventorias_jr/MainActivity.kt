package fr.quinquenaire.p15_eventorias_jr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.EventListScreen
import fr.quinquenaire.p15_eventorias_jr.presentation.navigation.EventoriasBottomBar
import fr.quinquenaire.p15_eventorias_jr.presentation.navigation.EventoriasDestinations
import fr.quinquenaire.p15_eventorias_jr.presentation.navigation.EventoriasNavHost
import fr.quinquenaire.p15_eventorias_jr.presentation.theme.P15_eventorias_jrTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            P15_eventorias_jrTheme {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                // Écrans qui affichent la BottomBar
                val bottomBarRoutes = setOf(
                    EventoriasDestinations.EventList.route,
                    EventoriasDestinations.UserProfile.route
                )
                val showBottomBar = currentRoute in bottomBarRoutes

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            EventoriasBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        // évite d'empiler les écrans principaux
                                        popUpTo(EventoriasDestinations.EventList.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    EventoriasNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}