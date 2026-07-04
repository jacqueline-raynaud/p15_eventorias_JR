package fr.quinquenaire.p15_eventorias_jr.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import fr.quinquenaire.p15_eventorias_jr.presentation.eventcreation.EventCreationScreen
import fr.quinquenaire.p15_eventorias_jr.presentation.eventdetail.EventDetailScreen
import fr.quinquenaire.p15_eventorias_jr.presentation.eventedit.EventEditScreen
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.EventListScreen
import fr.quinquenaire.p15_eventorias_jr.presentation.userprofile.UserProfileScreen

@Composable
fun EventoriasNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Point de départ selon l'état de connexion
    val startDestination = EventoriasDestinations.EventList.route


    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {


        // Liste des événements
        composable(EventoriasDestinations.EventList.route) {
            EventListScreen(
                onNavigateToDetail = { eventId ->
                    navController.navigate(
                        EventoriasDestinations.EventDetail.createRoute(eventId)
                    )
                },
                onNavigateToCreate = {
                    navController.navigate(EventoriasDestinations.EventCreate.route)
                }
            )
        }

        // Détail d'un événement
        composable(
            route = EventoriasDestinations.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDetailScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(EventoriasDestinations.EventEdit.createRoute(id))
                }
            )
        }

        // Création d'un événement
        composable(EventoriasDestinations.EventCreate.route) {
            EventCreationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Modification d'un événement
        composable(
            route = EventoriasDestinations.EventEdit.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventEditScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Profil utilisateur
        composable(EventoriasDestinations.UserProfile.route) {
            UserProfileScreen(

            )
        }
    }
}