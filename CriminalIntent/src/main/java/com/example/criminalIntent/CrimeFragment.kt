package com.example.criminalIntent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.criminalIntent.databinding.FragmentCrimeBinding
import java.io.File
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val DATE_FORMAT = "yyyy년 M월 d일 H시 m분, E요일"
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHONE = 2
private const val REQUEST_PHOTO = 3

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {
    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

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
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "com.example.businesspractice_app.fileprovider",
                        photoFile)
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
//            val packageManager: PackageManager = requireActivity().packageManager
//            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
//            Log.e("태그", resolvedActivity.toString())
//            if (resolvedActivity == null) {
//                isEnabled = false
//            }
        }

//        binding.crimePhone.apply {
//            val pickPhoneIntent = Intent(Intent.ACTION_PICK,ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
//            setOnClickListener {
//                startActivityForResult(pickPhoneIntent, REQUEST_PHONE)
//            }
//        }

        binding.crimePhone.setOnClickListener {
            Intent(Intent.ACTION_DIAL).apply {
                val phone = crime.phone
                data = Uri.parse("tel:$phone")
            }.also { intent ->
                startActivity(intent)
            }
        }

        binding.crimeCamera.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY)

                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
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
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            binding.crimePhoto.setImageBitmap(bitmap)
        } else {
            binding.crimePhoto.setImageDrawable(null)
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

//            requestCode == REQUEST_CONTACT && data != null -> {
//                val contactUri: Uri = data.data ?: return
//
//                // 쿼리에서 값으로 반환할 필드를 지정
//                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
//
//                // 쿼리를 수행. contactUri는 콘텐츠 제공자의 테이블을 나타낸다.
//                val cursor = requireActivity().contentResolver
//                    .query(contactUri, queryFields, null, null, null)
//
//                cursor?.use {
//                    // 쿼리 결과 데이터가 있는지 확인한다.
//                    if (it.count == 0) {
//                        return
//                    }
//                    // 첫 번째 데이터 행의 첫 번째 열의 값을 가져온다.
//                    // 이 값이 용의자의 이름
//                    it.moveToFirst()
//                    val suspect = it.getString(0)
//                    crime.suspect = suspect
//                    crimeDetailViewModel.saveCrime(crime)
//                    binding.crimeSuspect.text = suspect
//                }
//            }

//            requestCode == REQUEST_PHONE && data != null -> {
//                val contactURI : Uri? = data.data
//
//                //Got the phone ID
//                val queryFields = ContactsContract.CommonDataKinds.Phone._ID
//
//                //Perform Your Query - the Phone.CONTENT_URI is like a "where" clause here
//                val cursor =
//                    requireActivity().contentResolver
//                        .query(contactURI!!, null, queryFields, null, null)
//
//                cursor.use {
//                    if (it?.count == 0) {
//                        return
//                    }
//                    it?.moveToFirst()
//                    val noIdx = it?.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
//                    val number = it?.getString(noIdx!!)
//
//                    val dialNumber = Intent(Intent.ACTION_DIAL)
//                    dialNumber.data = Uri.parse("tel: $number")
//                    startActivity(dialNumber)
//                }
//                cursor?.close()
//            }

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri = data.data ?: return

                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

                val cursor = requireActivity().contentResolver
                    .query(contactUri, queryFields, null, null, null)

                cursor?.use {
                    if (it.count == 0) {
                        return
                    }
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    binding.crimeSuspect.text = suspect
                }

                val queryFieldsId = arrayOf(ContactsContract.Contacts._ID)

                val cursorId = requireActivity().contentResolver
                    .query(contactUri, queryFieldsId, null, null, null)

                cursorId?.use {
                    if (it.count == 0) {
                        return
                    }

                    it.moveToFirst()
                    val contactId = it.getString(0)

                    val phoneURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

                    val phoneNUmberQueryField = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    val phoneWhereClause = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"

                    val phoneQueryParameters = arrayOf(contactId)

                    val phoneCursor = requireActivity().contentResolver
                        .query(phoneURI, phoneNUmberQueryField, phoneWhereClause, phoneQueryParameters, null)

                    phoneCursor?.use { cursorPhone ->
                        cursorPhone.moveToFirst()
                        val phoneNumValue = cursorPhone.getString(0)
                        crime.phone = phoneNumValue
                    }

//                    TODO 연락처에 여러 번호가 있는 경우 번호를 선택하는 방법
//                    var phoneNumber: String = ""
//
//                    val allNumbers: ArrayList<String> = arrayListOf<String>()
//                    allNumbers.clear()
//
//                    phoneCursor?.use {cursorPhone ->
//
//                        cursorPhone.moveToFirst()
//                        while (cursorPhone.isAfterLast == false)
//                        {
//                            phoneNumber = cursorPhone.getString(0)
//                            allNumbers.add(phoneNumber)
//                            cursorPhone.moveToNext()
//                        }
//                    }

//                    val items = allNumbers.toTypedArray()
//
//                    var selectedNumber: String = ""
//
//                    val builder = AlertDialog.Builder(context)
//                    builder.setTitle("Choose a Number:")
//                    builder.setItems(items, DialogInterface.OnClickListener { dialog, which ->  selectedNumber = allNumbers[which].toString().replace("_","")
//                        crime.phone = selectedNumber
//                        binding.crimePhone.text = crime.phone
//                    })
//
//                    val alert = builder.create()
//                    if(allNumbers.size > 1) {
//                        alert.show()
//                    }
//                    else if (allNumbers.size == 1 && allNumbers[0].length != 0) {
//                        selectedNumber = allNumbers[0].toString().replace("_","")
//                        crime.phone = selectedNumber
//                        binding.crimePhone.text = crime.phone
//
//                    }
//
//                    else
//                    {
//                        binding.crimePhone.text = "no phone number found!"
//                        crime.phone = ""
//                    }

                    crimeDetailViewModel.saveCrime(crime)
                }
            }

            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
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