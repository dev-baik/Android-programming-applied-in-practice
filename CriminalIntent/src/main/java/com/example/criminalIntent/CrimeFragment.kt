package com.example.criminalIntent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.criminalIntent.databinding.FragmentCrimeBinding
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val DATE_FORMAT = "yyyy년 M월 d일 H시 m분, E요일"
private const val REQUEST_CONTACT = 1

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {
    private lateinit var crime: Crime

    private lateinit var binding : FragmentCrimeBinding

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()

        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
//        Log.d(TAG, "args bundle crime ID: $crimeId")
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
//        val view = inflater.inflate(R.layout.fragment_crime, container, false)
//        return view

        binding = FragmentCrimeBinding.inflate(inflater, container, false)

//        binding.crimeDate.apply {
//            text = crime.date.toString()
//            isEnabled = false
//        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        binding.crimeTitle.addTextChangedListener(titleWatcher)

        binding.crimeSolved.apply {
            setOnCheckedChangeListener { buttonView, isChecked ->
                crime.isSolved = isChecked
            }
        }

        binding.crimeDate.setOnClickListener {
//            DatePickerFragment().apply {
//                show(this@CrimeFragment.getParentFragmentManager(), DIALOG_DATE)
//            }
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.getParentFragmentManager(), DIALOG_DATE)
            }
        }

        binding.crimeReport.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type="text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
//                startActivity(intent)
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        binding.crimeSuspect.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }
//            pickContactIntent.addCategory(Intent.CATEGORY_HOME)
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

    private fun updateUI() {
        binding.crimeTitle.setText(crime.title)
        binding.crimeDate.text = crime.date.toString()
//        binding.crimeSolved.isChecked = crime.isSolved
        binding.crimeSolved.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }

        if (crime.suspect.isNotEmpty()) {
            binding.crimeSuspect.text = crime.suspect
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri = data.data ?: return

                // 쿼리에서 값으로 반환할 필드를 지정
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

                // 쿼리를 수행. contactUri는 콘텐츠 제공자의 테이블을 나타낸다.
                val cursor = requireActivity().contentResolver.query(contactUri, queryFields, null, null, null)
                cursor?.use {
                    // 쿼리 결과 데이터가 있는지 확인한다.
                    if (it.count == 0) {
                        return
                    }
                    // 첫 번째 데이터 행의 첫 번째 열의 값을 가져온다.
                    // 이 값이 용의자의 이름
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    binding.crimeSuspect.text = suspect
                }
            }
        }
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(date: Date) {
        crime.date = date
        updateUI()
    }
}