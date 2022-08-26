package com.example.crimefragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crimefragment.databinding.FragmentCrimeListBinding

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {
    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var binding: FragmentCrimeListBinding
    private var adapter: CrimeAdapter? = null

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Total crimes: ${crimeListViewModel.crimes.size}")
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

        updateUI()

        return binding.root
    }

    private fun updateUI() {
        val crimes = crimeListViewModel.crimes
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter

    }

    private inner class NormalCrimeHolder(view: View)
        : CrimeHolder(view), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = this.crime.date.toString()
        }

        override fun onClick(v: View?) {
            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class SeriousCrimeHolder(view: View)
        : CrimeHolder(view), View.OnClickListener {
        private val contactPoliceButton: Button = itemView.findViewById(R.id.contact_police_button)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = this.crime.date.toString()
            contactPoliceButton.setOnClickListener {
                Toast.makeText(context, "경찰에 연락", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onClick(v: View?) {
            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>)
        : RecyclerView.Adapter<CrimeHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            return when (viewType) {
                0 -> {
                    val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
                    NormalCrimeHolder(view)
                }
                else -> {
                    val view = layoutInflater.inflate(R.layout.list_item_serious_crime, parent, false)
                    SeriousCrimeHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            when (holder) {
                is NormalCrimeHolder -> holder.bind(crime)
                is SeriousCrimeHolder -> holder.bind(crime)
                else -> throw IllegalArgumentException()
            }
        }

        override fun getItemViewType(position: Int): Int {
            val crime = crimes[position]
            return when (crime.requiresPolice) {
                true -> 1
                else -> 0
            }
        }

        override fun getItemCount(): Int = crimes.size
    }
}