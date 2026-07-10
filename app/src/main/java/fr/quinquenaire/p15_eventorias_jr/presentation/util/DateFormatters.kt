package fr.quinquenaire.p15_eventorias_jr.presentation.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

object DateFormatters {

    fun formatDate(timestamp: Timestamp?): String =
        timestamp?.toDate()?.let {
            SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault())
                .format(it)
        }.orEmpty().replaceFirstChar { it.uppercase() }

    fun formatTime(timestamp: Timestamp?): String =
        timestamp?.toDate()?.let {
            SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, Locale.getDefault())
                .format(it)
        }.orEmpty()
}