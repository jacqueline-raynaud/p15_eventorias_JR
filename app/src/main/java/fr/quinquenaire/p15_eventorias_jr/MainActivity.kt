package fr.quinquenaire.p15_eventorias_jr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.EventListScreen
import fr.quinquenaire.p15_eventorias_jr.presentation.theme.P15_eventorias_jrTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            P15_eventorias_jrTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //EventoriasApp()
                    //FirebaseUiActivity()
                    EventListScreen(
                        onNavigateToDetail = { eventId ->
                            // todo faire les routes
                        }
                    )
                }

            }
        }
    }
}
