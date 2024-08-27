package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class ProfilePictureFragment : Fragment() {

    private lateinit var imageView: ImageView

    companion object {
        private const val ARG_IMAGE_RESOURCE_ID = "imageResourceId"

        fun newInstance(imageResourceId: Int): ProfilePictureFragment {
            val fragment = ProfilePictureFragment()
            val args = Bundle()
            args.putInt(ARG_IMAGE_RESOURCE_ID, imageResourceId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =inflater.inflate(R.layout.profile_picture_item, container, false)
        val imageView = view.findViewById<ImageView>(R.id.profilePictureViewPager)
        val imageResourceId = arguments?.getInt(ARG_IMAGE_RESOURCE_ID) ?: 0
        imageView.setImageResource(imageResourceId)
        return view
    }
}