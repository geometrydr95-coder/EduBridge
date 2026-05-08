package com.elly.edubridge.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.elly.edubridge.data.model.ExchangeRequest
import com.elly.edubridge.data.model.Skill
import com.elly.edubridge.data.model.User
import com.elly.edubridge.data.network.CloudinaryService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cloudinaryService: CloudinaryService
) {

    // =====================================================
    // AUTHENTICATION
    // =====================================================

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String
    ): User {

        val result = auth
            .createUserWithEmailAndPassword(email, password)
            .await()

        val userId = result.user?.uid
            ?: throw Exception("User creation failed")

        val user = User(
            userId = userId,
            fullName = fullName,
            email = email
        )

        firestore
            .collection("users")
            .document(userId)
            .set(user)
            .await()

        return user
    }

    suspend fun signIn(
        email: String,
        password: String
    ) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun signOut() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // =====================================================
    // CURRENT USER
    // =====================================================

    suspend fun getUser(): User? {

        val uid = auth.currentUser?.uid ?: return null

        return firestore
            .collection("users")
            .document(uid)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun updateUser(user: User) {

        val uid = auth.currentUser?.uid ?: return

        firestore
            .collection("users")
            .document(uid)
            .set(user, SetOptions.merge())
            .await()
    }

    // =====================================================
    // MARKETPLACE
    // =====================================================

    suspend fun getAllUsers(): List<User> {

        val currentUid = auth.currentUser?.uid

        val snapshot = firestore
            .collection("users")
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { it.toObject(User::class.java) }
            .filter { it.userId != currentUid }
    }

    suspend fun getUserById(userId: String): User? {

        return firestore
            .collection("users")
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun searchUsersBySkill(skill: String): List<User> {

        val snapshot = firestore
            .collection("users")
            .whereArrayContains("skillsOffered", skill)
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { it.toObject(User::class.java) }
    }

    // =====================================================
    // USERNAME VALIDATION
    // =====================================================

    suspend fun checkUsernameUnique(
        username: String
    ): Boolean {

        val query = firestore
            .collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()

        return query.isEmpty
    }

    // =====================================================
    // SKILLS
    // =====================================================

    suspend fun getSkills(): List<Skill> {

        return firestore
            .collection("skills")
            .get()
            .await()
            .toObjects(Skill::class.java)
    }
    // Add this inside your UserRepository class
    suspend fun sendExchangeRequest(request: ExchangeRequest) {
        val docRef = firestore.collection("requests").document()
        val finalRequest = request.copy(requestId = docRef.id)
        docRef.set(finalRequest).await()
    }

    // =====================================================
    // IMAGE UPLOAD
    // =====================================================

    suspend fun uploadProfileImage(
        file: File
    ): String {

        val compressedFile = compressImage(file)

        val requestFile = compressedFile
            .asRequestBody("image/*".toMediaTypeOrNull())

        val body = MultipartBody.Part.createFormData(
            "file",
            compressedFile.name,
            requestFile
        )

        val preset = "edubridge_profiles"
            .toRequestBody("text/plain".toMediaTypeOrNull())

        val response = cloudinaryService.uploadImage(
            body,
            preset
        )

        return response.secure_url
    }

    // =====================================================
    // IMAGE COMPRESSION
    // =====================================================

    private fun compressImage(file: File): File {

        val bitmap = BitmapFactory.decodeFile(file.path)

        val compressedFile = File(
            file.parent,
            "compressed_${file.name}"
        )

        val outputStream = FileOutputStream(compressedFile)

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            70,
            outputStream
        )

        outputStream.flush()
        outputStream.close()

        return compressedFile
    }
}