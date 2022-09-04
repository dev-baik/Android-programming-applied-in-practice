package com.example.criminalIntent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.criminalIntent.databinding.PhotoDialogFragmentBinding
import java.io.File

class PhotoDialogFragment(val photofile : File) : DialogFragment() {
    private lateinit var binding: PhotoDialogFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PhotoDialogFragmentBinding.inflate(inflater, container, false)

        val image = getScaledBitmap(photofile.path, requireActivity())

        binding.imageView.setImageBitmap(image)

        binding.imageButton2.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    fun getScaledBitmap(path: String, activity: Activity): Bitmap {
        val size = Point()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = activity.display
            display?.getRealSize(size)
        } else {
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getSize(size)
        }
        return getScaledBitmap(path, size.x, size.y)
    }
}