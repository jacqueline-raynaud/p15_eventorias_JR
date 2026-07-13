package fr.quinquenaire.p15_eventorias_jr.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseAuthManager
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseFirestoreManager
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseMessagingManager
import fr.quinquenaire.p15_eventorias_jr.data.remote.FirebaseStorageManager
import fr.quinquenaire.p15_eventorias_jr.domain.model.UserProfile
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class UserProfileRepositoryImplTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    beforeSpec {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    afterSpec {
        unmockkStatic(Log::class)
    }

    val firestoreManager = mockk<FirebaseFirestoreManager>()
    val storageManager = mockk<FirebaseStorageManager>()
    val firebaseAuth = mockk<FirebaseAuth>()
    val firebaseAuthManager = mockk<FirebaseAuthManager>()
    val messagingManager = mockk<FirebaseMessagingManager>()
    val repository = UserProfileRepositoryImpl(
        firestoreManager,
        storageManager,
        firebaseAuth,
        firebaseAuthManager,
        messagingManager
    )

    Given("getUserProfile") {
        val uid = "user123"
        val profile = UserProfile(uid = uid, firstName = "Jean")
        every { firestoreManager.getUserProfile(uid) } returns flowOf(profile)

        When("appel de getUserProfile") {
            Then("doit retourner le flow de firestoreManager") {
                repository.getUserProfile(uid).collect {
                    it shouldBe profile
                }
            }
        }
    }

    Given("getCurrentUserId") {
        When("un utilisateur est connecté") {
            val firebaseUser = mockk<FirebaseUser>()
            every { firebaseAuth.currentUser } returns firebaseUser
            every { firebaseUser.uid } returns "user123"

            Then("doit retourner l'UID de l'utilisateur") {
                repository.getCurrentUserId() shouldBe "user123"
            }
        }

        When("aucun utilisateur n'est connecté") {
            every { firebaseAuth.currentUser } returns null

            Then("doit retourner null") {
                repository.getCurrentUserId() shouldBe null
            }
        }
    }

    Given("createUserProfileIfMissing") {
        val profile = UserProfile(uid = "user123")
        coEvery { firestoreManager.createUserProfileIfMissing(profile) } returns Unit

        When("appel de createUserProfileIfMissing") {
            runTest {
                repository.createUserProfileIfMissing(profile)
            }
            Then("doit appeler firestoreManager") {
                coVerify { firestoreManager.createUserProfileIfMissing(profile) }
            }
        }
    }

    Given("updateNotificationSetting") {
        val uid = "user123"
        coEvery { firestoreManager.updateNotificationSetting(uid, any()) } returns Unit
        coEvery { messagingManager.subscribe() } returns Unit
        coEvery { messagingManager.unsubscribe() } returns Unit

        When("activation des notifications") {
            runTest {
                repository.updateNotificationSetting(uid, true)
            }
            Then("doit s'abonner et mettre à jour firestore") {
                coVerify { messagingManager.subscribe() }
                coVerify { firestoreManager.updateNotificationSetting(uid, true) }
            }
        }

        When("désactivation des notifications") {
            runTest {
                repository.updateNotificationSetting(uid, false)
            }
            Then("doit se désabonner et mettre à jour firestore") {
                coVerify { messagingManager.unsubscribe() }
                coVerify { firestoreManager.updateNotificationSetting(uid, false) }
            }
        }
    }

    Given("syncNotificationSubscription") {
        coEvery { messagingManager.subscribe() } returns Unit
        coEvery { messagingManager.unsubscribe() } returns Unit

        When("synchronisation avec succès") {
            runTest {
                repository.syncNotificationSubscription(true)
            }
            Then("doit appeler messagingManager") {
                coVerify { messagingManager.subscribe() }
            }
        }

        When("synchronisation avec erreur") {
            coEvery { messagingManager.subscribe() } throws Exception("Sync error")
            runTest {
                repository.syncNotificationSubscription(true)
            }
            Then("l'erreur doit être absorbée (Log.e appelé)") {
                coVerify { Log.e(any(), any(), any()) }
            }
        }
    }

    Given("updateUserProfile") {
        val profile = UserProfile(uid = "user123", firstName = "Updated")
        coEvery { firestoreManager.updateUserProfile(profile) } returns Unit

        When("appel de updateUserProfile") {
            runTest {
                repository.updateUserProfile(profile)
            }
            Then("doit appeler firestoreManager") {
                coVerify { firestoreManager.updateUserProfile(profile) }
            }
        }
    }

    Given("uploadUserAvatar") {
        val uid = "user123"
        val uri = mockk<Uri>()
        coEvery { storageManager.uploadUserAvatar(uid, uri) } returns "http://avatar.url"

        When("appel de uploadUserAvatar") {
            runTest {
                val result = repository.uploadUserAvatar(uid, uri)
                result shouldBe "http://avatar.url"
            }
            Then("doit appeler storageManager") {
                coVerify { storageManager.uploadUserAvatar(uid, uri) }
            }
        }
    }

    Given("deleteProfileData") {
        val uid = "user123"
        val avatarUrl = "http://avatar.url"
        coEvery { storageManager.deleteAvatarByUrl(avatarUrl) } returns Unit
        coEvery { firestoreManager.deleteUserProfile(uid) } returns Unit

        When("appel de deleteProfileData") {
            runTest {
                repository.deleteProfileData(uid, avatarUrl)
            }
            Then("doit supprimer du storage et de firestore") {
                coVerify { storageManager.deleteAvatarByUrl(avatarUrl) }
                coVerify { firestoreManager.deleteUserProfile(uid) }
            }
        }
    }

    Given("deleteAuthAccount") {
        coEvery { firebaseAuthManager.deleteCurrentUserAccount() } returns Unit

        When("appel de deleteAuthAccount") {
            runTest {
                repository.deleteAuthAccount()
            }
            Then("doit appeler firebaseAuthManager") {
                coVerify { firebaseAuthManager.deleteCurrentUserAccount() }
            }
        }
    }
})
