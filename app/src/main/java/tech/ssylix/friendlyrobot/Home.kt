package tech.ssylix.friendlyrobot

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.content_home.view.*
import tech.ssylix.friendlyrobot.MainActivity.Companion.ACTIONS_LIST
import tech.ssylix.friendlyrobot.MainActivity.Companion.TRIGGER_LIST
import java.util.*
import kotlin.collections.HashMap

class Home : AppCompatActivity() {

    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setSupportActionBar(findViewById(R.id.toolbar))
        //supportNavigateUpTo(Intent(this, MainActivity::class.java))

        toolbar.title = SELECT_TRIGGER_TEXT
        sharedPrefs = getSharedPreferences(MainActivity.SHARED_PREF_PERM_ID, Context.MODE_PRIVATE)

        option_list_recycler.apply {
            layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL)
            adapter = OptionListRecyclerAdapter(
                sharedPrefs.getString(TRIGGERS, "")?.split(DELIMETER) ?: TRIGGER_LIST
            )
            setHasFixedSize(true)
        }

        save_skill_button.setOnClickListener {
            val trigger = (frameView.getChildAt(0) as? TextView)?.text
            try {
                val actions =
                    (option_action_recycler.adapter as OptionActionsRecyclerAdapter).actions.apply {
                        remove(null)
                    }

                if (trigger != null && actions.isNotEmpty()) {
                    var actionsText = StringBuilder()
                    actions.forEachIndexed { index, s ->
                        actionsText.append(s)
                        if (index < actions.size - 1) {
                            actionsText.append(" and then ")
                        }
                    }
                    val fulltext =
                        "If I ${trigger.toString().toLowerCase(Locale.getDefault())} then" +
                                " I will ${actionsText.toString().toLowerCase(Locale.getDefault())}"

                    uploadToRemote(fulltext)

                } else if (trigger == null) {
                    trigger.toast(this, Toast.LENGTH_LONG, "Trigger failure")
                } else if (actions.isEmpty()) {
                    actions.toast(this, Toast.LENGTH_LONG, "Actions failure")
                }
            } catch (c: TypeCastException) {
                c.printStackTrace()
            }
        }
    }

    private fun uploadToRemote(fulltext: String) {
        val data = HashMap<String, String>()

        data[getRandomString(48)] = fulltext
        uploadToFirebaseFirestore(sharedPrefs, data)
        finish()
    }

    inner class OptionListRecyclerAdapter(val options: List<String>) :
        RecyclerView.Adapter<OptionListRecyclerAdapter.MyViewHolder>() {

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    it.animateClicks {
                        if (option_stage_recycler.adapter != null) {
                            (option_stage_recycler.adapter as OptionStageRecyclerAdapter).apply {
                                mutableOptions.add(options[adapterPosition])
                                notifyDataSetChanged()
                            }
                        } else {
                            option_stage_recycler.apply {
                                layoutManager = LinearLayoutManager(
                                    this@Home,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                                adapter =
                                    OptionStageRecyclerAdapter(arrayListOf(options[adapterPosition]))
                                setHasFixedSize(true)
                            }
                        }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(
                LayoutInflater.from(this@Home).inflate(R.layout.model_option, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return options.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            (holder.itemView as TextView).apply {
                text = options[position]
                backgroundTintList = if (this@Home.toolbar.title == SELECT_ACTION_TEXT) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ColorStateList.valueOf(resources.getColor(R.color.colorActionOption, theme))
                    } else {
                        ColorStateList.valueOf(resources.getColor(R.color.colorActionOption))
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ColorStateList.valueOf(resources.getColor(R.color.colorAccent, theme))
                    } else {
                        ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
                    }
                }
            }
        }
    }

    inner class OptionStageRecyclerAdapter(val mutableOptions: ArrayList<String>) :
        RecyclerView.Adapter<OptionStageRecyclerAdapter.MyViewHolder>() {

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    it.animateClicks {
                        if (toolbar.title == SELECT_TRIGGER_TEXT) {
                            val view = LayoutInflater.from(this@Home)
                                .inflate(R.layout.model_option, FrameLayout(this@Home), false)
                            (view as TextView).text = mutableOptions[adapterPosition]

                            frameView.apply {
                                removeAllViews()
                                addView(view)
                            }

                            option_action_recycler.apply {
                                layoutManager = LinearLayoutManager(this@Home)
                                adapter = OptionActionsRecyclerAdapter(arrayListOf(null))
                                setHasFixedSize(true)
                            }

                            this@Home.toolbar.title = SELECT_ACTION_TEXT

                            option_stage_recycler.apply {
                                layoutManager = LinearLayoutManager(
                                    this@Home,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                                adapter = OptionStageRecyclerAdapter(arrayListOf())
                                setHasFixedSize(true)
                            }

                            option_list_recycler.apply {
                                layoutManager =
                                    StaggeredGridLayoutManager(
                                        3,
                                        StaggeredGridLayoutManager.HORIZONTAL
                                    )
                                adapter = OptionListRecyclerAdapter(
                                    sharedPrefs.getString(ACTIONS, "")?.split(DELIMETER)
                                        ?: ACTIONS_LIST
                                )
                                setHasFixedSize(true)
                            }
                        } else {
                            (option_action_recycler.adapter as OptionActionsRecyclerAdapter).addNewAction(
                                mutableOptions[adapterPosition]
                            )
                        }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(
                LayoutInflater.from(this@Home).inflate(R.layout.model_option, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return mutableOptions.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            (holder.itemView as TextView).apply {
                text = mutableOptions[position]
                backgroundTintList = if (this@Home.toolbar.title == SELECT_ACTION_TEXT) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ColorStateList.valueOf(resources.getColor(R.color.colorActionOption, theme))
                    } else {
                        ColorStateList.valueOf(resources.getColor(R.color.colorActionOption))
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ColorStateList.valueOf(resources.getColor(R.color.colorAccent, theme))
                    } else {
                        ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
                    }
                }
            }
        }
    }

    inner class OptionActionsRecyclerAdapter(val actions: ArrayList<String?>) :
        RecyclerView.Adapter<OptionActionsRecyclerAdapter.MyViewHolder>() {

        val DEFAULT_ADD_NEW = 11001100

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    it.animateClicks {
                        actions.removeAt(adapterPosition)
                        this@OptionActionsRecyclerAdapter.notifyItemRemoved(adapterPosition)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return when (viewType) {
                DEFAULT_ADD_NEW -> MyViewHolder(
                    LayoutInflater.from(this@Home).inflate(R.layout.model_action, parent, false)
                )

                else -> MyViewHolder(
                    LayoutInflater.from(this@Home).inflate(R.layout.model_action_2, parent, false)
                )
            }
        }

        override fun getItemCount(): Int {
            return actions.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if (actions[position] != null) {
                val view = LayoutInflater.from(this@Home)
                    .inflate(R.layout.model_option, FrameLayout(this@Home), false)
                (view as TextView).apply {
                    text = actions[position]
                    backgroundTintList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ColorStateList.valueOf(resources.getColor(R.color.colorActionOption, theme))
                    } else {
                        ColorStateList.valueOf(resources.getColor(R.color.colorActionOption))
                    }
                }

                holder.itemView.frameView.apply {
                    removeAllViews()
                    addView(view)
                }
            } else {
                val view = LayoutInflater.from(this@Home)
                    .inflate(R.layout.model_add_option, FrameLayout(this@Home), false)

                holder.itemView.frameView.apply {
                    removeAllViews()
                    addView(view)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) DEFAULT_ADD_NEW else super.getItemViewType(position)
        }

        fun addNewAction(actionText: String) {
            actions[actions.size - 1] = actionText
            actions.add(null)
            notifyDataSetChanged()
        }
    }

    companion object {
        const val SELECT_ACTION_TEXT = "Select action"

        const val SELECT_TRIGGER_TEXT = "Select trigger"
    }
}