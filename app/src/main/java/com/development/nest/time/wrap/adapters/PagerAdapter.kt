package com.development.nest.time.wrap.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.development.nest.time.wrap.fragments.FilesFragment

class PagerAdapter(val fragment: List<Fragment>, fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = fragment.size

    override fun createFragment(position: Int): Fragment {
        val fragment = FilesFragment()
        fragment.arguments = Bundle().apply {
            if (position == 0) {
                putString("fragmentType", "images")
            } else {
                putString("fragmentType", "videos")
            }
        }
        return fragment
    }
}