package fr.quinquenaire.p15_eventorias_jr.domain.model


data class UserProfile (
    val uid:String ="", // id firebase auth
    val firstName:String ="",
    val lastName:String ="",
    val email:String ="",
    val avatarUrl:String ="",
    val notificationEnabled:Boolean = false,
    val fcmToken: String? = null // pour la fonction notification
)