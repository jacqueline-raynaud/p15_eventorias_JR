package fr.quinquenaire.p15_eventorias_jr.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import fr.quinquenaire.p15_eventorias_jr.domain.Event
import fr.quinquenaire.p15_eventorias_jr.domain.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log
import dagger.Provides


class FirebaseFirestoreManager(private val firestore: FirebaseFirestore) {

    // Events Collection
    fun getEvents(): Flow<List<Event>> = callbackFlow {
        val listener = firestore.collection("events")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    //Log.e("EventoriasApp", error, "Error fetching events")
                    Log.e("EventoriasApp", "Error fetching events", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    fun getEventDetail(eventId: String): Flow<Event?> = callbackFlow {
        val listener = firestore.collection("events").document(eventId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    //Log.e("EventoriasApp", error, "Error fetching event detail")
                    Log.e("EventoriasApp", "Error fetching event detail", error)
                    trySend(null)
                    return@addSnapshotListener
                }
                val event = snapshot?.toObject(Event::class.java)?.copy(id = snapshot.id)
                trySend(event)
            }
        awaitClose { listener.remove() }
    }

    fun searchEvents(query: String): Flow<List<Event>> = callbackFlow {
        val listener = firestore.collection("events")
            .orderBy("title")
            .startAt(query)
            .endAt(query + "")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    fun getEventsByCategory(category: String): Flow<List<Event>> = callbackFlow {
        val listener = firestore.collection("events")
            .whereEqualTo("category", category)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createEvent(event: Event): String {
        return try {
            val ref = firestore.collection("events").document()
            val eventToSave = event.copy(
                id = ref.id
            )
            ref.set(eventToSave).await()
            ref.id
        } catch (e: Exception) {
            //Log.e("EventoriasApp", e, "Error creating event")
            Log.e("EventoriasApp", "Error creating event", e)
            throw e
        }
    }

    suspend fun updateEvent(event: Event) {
        try {
            val updateMap = mapOf(
                "name" to event.name,
                "description" to event.description,
                "date" to event.date,
                "time" to event.time,
                "locationName" to event.locationName,
                "location" to event.location,
                "category" to event.category,
                "imageUrl" to event.imageUrl,
                "organizerId" to event.organizerId,
                "guests" to event.guests

            )
            firestore.collection("events").document(event.id).update(updateMap).await()
        } catch (e: Exception) {
            //Log.e("EventoriasApp", e, "Error updating event")
            Log.e("EventoriasApp", "Error updating event", e)
            throw e
        }
    }

    suspend fun deleteEvent(eventId: String) {
        try {
            firestore.collection("events").document(eventId).delete().await()
        } catch (e: Exception) {
            //Log.e("EventoriasApp", e, "Error deleting event")
            Log.e("EventoriasApp", "Error deleting event", e)
            throw e
        }
    }

    // Users Collection
    fun getUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(UserProfile::class.java)?.copy(uid = snapshot.id)
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createUserProfile(user: UserProfile) {
        try {
            firestore.collection("users").document(user.uid).set(user).await()
        } catch (e: Exception) {
            //Log.e("EventoriasApp", e, "Error creating user profile")
            Log.e("EventoriasApp", "Error creating user profile", e)
            throw e
        }
    }

    suspend fun updateUserProfile(user: UserProfile) {
        try {
            firestore.collection("users").document(user.uid)
                .update(
                    mapOf(
                        "firstName" to user.firstName,
                        "lastName" to user.lastName,
                        "avatarUrl" to user.avatarUrl,
                    )
                )
                .await()
        } catch (e: Exception) {
            //Log.e("EventoriasApp", e, "Error updating user profile")
            Log.e("EventoriasApp", "Error updating user profile", e)
            throw e
        }
    }

    suspend fun toggleNotifications(userId: String, enabled: Boolean) {
        try {
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "notificationsEnabled" to enabled,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            //Log.e("EventoriasApp", e, "Error toggling notifications")
            Log.e("EventoriasApp", "Error toggling notifications", e)
            throw e
        }
    }
}