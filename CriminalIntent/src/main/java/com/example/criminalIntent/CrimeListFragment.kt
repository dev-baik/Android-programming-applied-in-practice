package com.example.criminalIntent

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalIntent.databinding.FragmentCrimeListBinding
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {
    // 호스팅 액티비티에서 구현할 인터페이스
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null

    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var binding: FragmentCrimeListBinding
//    private var adapter: CrimeAdapter? = null
//    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())
    private var adapter: CrimeAdapter? = CrimeAdapter()

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.d(TAG, "Total crimes: ${crimeListViewModel.crimes.size}")
//    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    private abstract class CrimeHolder(view: View) : RecyclerView.ViewHolder(view) {
        var crime = Crime()
        val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentCrimeListBinding.inflate(inflater, container, false)

        crimeRecyclerView = binding.crimeRecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

//        updateUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer { crimes ->
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    updateUI(crimes)
                }
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    //    private fun updateUI() {
//        val crimes = crimeListViewModel.crimes
//        adapter = CrimeAdapter(crimes)
//        crimeRecyclerView.adapter = adapter
//    }

//    private fun updateUI(crimes: List<Crime>) {
//        adapter = CrimeAdapter(crimes)
//        crimeRecyclerView.adapter = adapter
//    }

    private fun updateUI(crimes: List<Crime>) {
        adapter?.submitList(crimes)
    }

    private inner class NormalCrimeHolder(view: View)
        : CrimeHolder(view), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = DateFormat.format("EEEE, MMM dd, yyyy", this.crime.date)
            val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

//    private inner class SeriousCrimeHolder(view: View)
//        : CrimeHolder(view), View.OnClickListener {
//        private val contactPoliceButton: Button = itemView.findViewById(R.id.contact_police_button)
//
//        init {
//            itemView.setOnClickListener(this)
//        }
//
//        fun bind(crime: Crime) {
//            this.crime = crime
//            titleTextView.text = this.crime.title
//            dateTextView.text = DateFormat.format("EEEE, MMM dd, yyyy", this.crime.date)
//            contactPoliceButton.setOnClickListener {
//                Toast.makeText(context, "경찰에 연락", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        override fun onClick(v: View?) {
//            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
//        }
//    }

//    private inner class CrimeAdapter(var crimes: List<Crime>)
//        : RecyclerView.Adapter<CrimeHolder>() {
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
////            return when (viewType) {
////                0 -> {
////                    val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
////                    NormalCrimeHolder(view)
////                }
////                else -> {
////                    val view = layoutInflater.inflate(R.layout.list_item_serious_crime, parent, false)
////                    SeriousCrimeHolder(view)
////                }
////            }
//            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
//            return NormalCrimeHolder(view)
//        }
//
//        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
//            val crime = crimes[position]
//            when (holder) {
//                is NormalCrimeHolder -> holder.bind(crime)
////                is SeriousCrimeHolder -> holder.bind(crime)
//                else -> throw IllegalArgumentException()
//            }
//        }
//
////        override fun getItemViewType(position: Int): Int {
////            val crime = crimes[position]
////            return when (crime.requiresPolice) {
////                true -> 1
////                else -> 0
////            }
////        }
//
//        override fun getItemCount(): Int = crimes.size
//    }

    private inner class CrimeAdapter
        : ListAdapter<Crime, CrimeHolder>(DiffCallback) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return NormalCrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = currentList[position]
            when (holder) {
                is NormalCrimeHolder -> holder.bind(crime)
//                is SeriousCrimeHolder -> holder.bind(crime)
                else -> throw IllegalArgumentException()
            }
        }

        override fun getItemCount() = currentList.size
    }

    object DiffCallback : DiffUtil.ItemCallback<Crime>() {
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem == newItem
        }
    }
}