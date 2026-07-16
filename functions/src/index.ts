
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";

admin.initializeApp();

/**
 * Notifie les utilisateurs lors de la création d'un événement.
 */
export const notifyOnNewEvent = onDocumentCreated(
  "events/{eventId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      console.log("Pas de données");
      return;
    }

    const newEvent = snapshot.data();
    const eventName = newEvent.name || "Nouvel événement";

    // 1. Recherche des tokens
    const usersSnapshot = await admin.firestore().collection("users")
      .where("notificationEnabled", "==", true)
      .get();

    const tokens: string[] = [];
    usersSnapshot.forEach((doc) => {
      const data = doc.data();
      if (data.fcmToken) {
        tokens.push(data.fcmToken);
      }
    });

    if (tokens.length === 0) {
      console.log("Aucun token trouvé");
      return;
    }

    // 2. Envoi
    const message = {
      notification: {
        title: "Nouvel événement !",
        body: `L'événement "${eventName}" vient d'être publié.`,
      },
      tokens: tokens,
    };

    try {
      const response = await admin.messaging().sendEachForMulticast(message);
      console.log(`${response.successCount} notifications envoyées.`);
    } catch (error) {
      console.error("Erreur d'envoi:", error);
    }
  }
);

