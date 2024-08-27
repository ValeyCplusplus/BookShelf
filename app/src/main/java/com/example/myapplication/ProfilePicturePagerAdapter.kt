package com.example.myapplication

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter.POSITION_NONE
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProfilePicturePagerAdapter(fragmentActivity: FragmentActivity, private val imageResourceIds: List<Int>) :
    FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int = imageResourceIds.size

        override fun createFragment(position: Int): Fragment {
            return ProfilePictureFragment.newInstance(imageResourceIds[position])
        }
    }