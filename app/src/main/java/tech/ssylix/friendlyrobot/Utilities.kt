package tech.ssylix.friendlyrobot

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.constraintlayout.widget.Constraints.TAG
import androidx.core.content.edit
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + (0..9)
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

val firestore = Firebase.firestore
private val DATA_COLLECTION_FOLDER = "data"

@Synchronized
fun readFromFirebaseFirestore(
    preferences: SharedPreferences,
    action: (DocumentSnapshot) -> Boolean
) {
    val id = preferences.getString(MainActivity.ID_PREFERENCE, "")

    if (id != null) {
        firestore.collection(DATA_COLLECTION_FOLDER)
            .document(id).apply {
                get().addOnSuccessListener {
                    if (!action.invoke(it)) readFromFirebaseFirestore(preferences, action)
                }.addOnFailureListener {
                    Log.e(TAG, "readFromDatabase: ${it.message}")
                    readFromFirebaseFirestore(preferences, action)
                }
            }
    } else {
        initializeId(preferences)
        readFromFirebaseFirestore(preferences, action)
    }
}

fun uploadToFirebaseFirestore(preferences: SharedPreferences, data: HashMap<String, String>) {

    val id = preferences.getString(MainActivity.ID_PREFERENCE, "")

    if (id != null) {
        firestore.collection(DATA_COLLECTION_FOLDER)
            .document(id)
            .apply {
                get().addOnSuccessListener { snapshot ->
                    if (snapshot.data != null) {
                        (snapshot.data as HashMap<String, String>).forEach {
                            data[it.key] = it.value
                        }
                        this.set(data)
                    } else {
                        this.set(data)
                    }
                }
            }
    } else {
        initializeId(preferences)
        uploadToFirebaseFirestore(preferences, data)
    }
}

fun initializeId(preferences: SharedPreferences) {
    if (!preferences.contains(MainActivity.ID_PREFERENCE)) {
        val uid = getRandomString(48)
        preferences.edit {
            putString(MainActivity.ID_PREFERENCE, uid)
        }
    }
}

fun <T> T.toast(context: Context, duration: Int = Toast.LENGTH_LONG): T {
    Toast.makeText(context, this.toString(), duration).show()
    return this
}

fun <T> T.toast(context: Context, duration: Int = Toast.LENGTH_SHORT, message: Any = "debug"): T {
    Toast.makeText(
        context,
        """$message
                |${this.toString()}
                |""".trimMargin(),
        duration
    ).show()
    return this
}
