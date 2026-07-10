package fr.quinquenaire.p15_eventorias_jr.testutils

import com.google.firebase.Timestamp
import java.util.Calendar

object TestTimestamps {

    fun of(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Timestamp {
        val calendar = Calendar.getInstance().apply {
            set(year, month, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(calendar.time)
    }
}