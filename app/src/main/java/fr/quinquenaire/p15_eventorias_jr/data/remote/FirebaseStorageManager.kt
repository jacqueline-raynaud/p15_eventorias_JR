package fr.quinquenaire.p15_eventorias_jr.android.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import android.util.Log

class FirebaseStorageManager(private val firebaseStorage: FirebaseStorage) {

    suspend fun uploadEventImage(eventId: String, imageUri: Uri): String {
        return try {
            val storageRef = firebaseStorage.reference
            val imageRef = storageRef.child("events/$eventId/image.jpg")
            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            //Log.e("EventoriasApp", e, "Error uploading event image")
            Log.e("EventoriasApp", "Error uploading event image", e)
            throw e
        }
    }

    suspend fun uploadUserAvatar(userId: String, avatarUri: Uri): String {
        return try {
            val storageRef = firebaseStorage.reference
            val avatarRef = storageRef.child("users/$userId/avatar.jpg")
            avatarRef.putFile(avatarUri).await()
            avatarRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            //Log.e("EventoriasApp", e, "Error uploading UserProfile avatar")
            Log.e("EventoriasApp", "Error uploading UserProfile avatar", e)
            throw e
        }
    }

    suspend fun deleteFile(filePath: String) {
        try {
            firebaseStorage.reference.child(filePath).delete().await()
        } catch (e: Exception) {
            //Log.e("EventoriasApp", e, "Error deleting file")
            Log.e("EventoriasApp", "Error deleting file", e)
            throw e
        }
    }
}
