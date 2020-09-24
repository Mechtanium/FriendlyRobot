package tech.ssylix.friendlyrobot

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_first.view.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.skills_list_recycler.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = SkillsRecyclerAdapter(arrayListOf(""))
            setHasFixedSize(true)
        }
    }

    inner class SkillsRecyclerAdapter(val skills: ArrayList<String>) :
        RecyclerView.Adapter<SkillsRecyclerAdapter.MyViewHolder>() {

        val DEFAULT_ADD_NEW = 11001100

        init {
            skills.add(0, "null")
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    if (adapterPosition == 0) {
                        startActivity(Intent(requireContext(), Home::class.java))
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return when (viewType) {
                DEFAULT_ADD_NEW -> MyViewHolder(
                    LayoutInflater.from(requireContext())
                        .inflate(R.layout.model_add_new_skill, parent, false)
                )

                else -> MyViewHolder(
                    LayoutInflater.from(requireContext())
                        .inflate(R.layout.model_skill_item, parent, false)
                )
            }
        }

        override fun getItemCount(): Int {
            return skills.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) DEFAULT_ADD_NEW else super.getItemViewType(position)
        }
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): FirstFragment {
            return FirstFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}