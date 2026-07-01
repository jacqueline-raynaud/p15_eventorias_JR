package fr.quinquenaire.p15_eventorias_jr.domain

import com.google.firebase.firestore.GeoPoint

data class Event (
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val locationName: String = "",
    val category: String = "",
    val imageUrl: String = "", // Lien Firebase Storage
    val location: GeoPoint? = null,
    val organizerId: String = "", // ID de l'utilisateur créateur
    val guests: List<String> = emptyList() // Liste des IDs des invités
)