package tech.ssylix.friendlyrobot

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import tech.ssylix.friendlyrobot.ui.main.SectionsPagerAdapter

class MainActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        imageView.setOnClickListener {
            onBackPressed()
        }

        sharedPreferences = getSharedPreferences(SHARED_PREF_PERM_ID, Context.MODE_PRIVATE)
        readFromFirebaseFirestore(sharedPreferences) {
            offlineStatus.visibility = View.GONE
            true
        }
        initializeId(sharedPreferences)
        initializeTriggers(sharedPreferences, TRIGGER_LIST)
        initializeActions(sharedPreferences, ACTIONS_LIST)
    }

    companion object {
        val ID_PREFERENCE = "Permanent_id"
        val STATUS_CONNECTION = "Internet_Access"
        val SHARED_PREF_PERM_ID = "Uid_Prefs"

        val TRIGGER_LIST = listOf(
            "See a face",
            "Hear my name",
            "Hear a sound",
            "Feel a touch",
            "See a smile",
            "Get picked up"
        )

        val ACTIONS_LIST = listOf(
            "Sing a song",
            "Dance",
            "Spin in circles",
            "Cry",
            "Feel sad",
            "Laugh out loud"
        )
    }
}