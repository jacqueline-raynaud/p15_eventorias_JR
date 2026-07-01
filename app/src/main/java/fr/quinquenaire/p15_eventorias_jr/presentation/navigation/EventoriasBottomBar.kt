package fr.quinquenaire.p15_eventorias_jr.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun EventoriasBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == EventoriasDestinations.EventList.route,
            onClick = { onNavigate(EventoriasDestinations.EventList.route) },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Événements") },
            label = { Text("Événements") }
        )
        NavigationBarItem(
            selected = currentRoute == EventoriasDestinations.UserProfile.route,
            onClick = { onNavigate(EventoriasDestinations.UserProfile.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil") }
        )
    }
}