package tech.ssylix.friendlyrobot

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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
const val DELIMETER = '$'
const val TRIGGERS = "TriggerListTag"
const val ACTIONS = "ActionListTag"

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
                    preferences.edit {
                        putBoolean(MainActivity.STATUS_CONNECTION, false)
                    }
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
            putString(
                MainActivity.ID_PREFERENCE,
                uid /*"c9v2nVLo0mJHfnnPhuBBuVJrchoQk3k8FB660yM5YkXDB4OO"*/
            )
            this.apply()
        }
    }
}

fun initializeTriggers(preferences: SharedPreferences, triggerList: List<String>) {
    if (!preferences.contains(TRIGGERS)) {
        preferences.edit {
            val sb = StringBuilder()
            triggerList.forEachIndexed { index, s ->
                when (index) {
                    (triggerList.size - 1) -> sb.append(s)

                    else -> sb.append("$s$DELIMETER")
                }
            }

            putString(TRIGGERS, sb.toString())
            this.apply()
        }
    }
}

fun initializeActions(preferences: SharedPreferences, actionList: List<String>) {
    if (!preferences.contains(ACTIONS)) {
        preferences.edit {
            val sb = StringBuilder()
            actionList.forEachIndexed { index, s ->
                when (index) {
                    (actionList.size - 1) -> sb.append(s)

                    else -> sb.append("$s$DELIMETER")
                }
            }

            putString(ACTIONS, sb.toString())
            this.apply()
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

fun View.animateClicks(time: Int = 100, doOnComplete: () -> Unit) {
    val x_anim = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.9f, 1f)
    val y_anim = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.9f, 1f)

    AnimatorSet().apply {
        playTogether(x_anim, y_anim)
        duration = time.toLong()
        interpolator = AccelerateDecelerateInterpolator()
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                Log.e(TAG, "onAnimationEnd: Got here")
                doOnComplete.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
        start()
    }
}
