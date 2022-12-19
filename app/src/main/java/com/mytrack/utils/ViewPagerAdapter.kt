package com.mytrack.utils

import java.util.ArrayList
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: FragmentActivity?) : FragmentStateAdapter(activity!!) {

    private val fragList = ArrayList<Fragment>()
    private val fragTitle = ArrayList<String>()

    fun add(fragment: Fragment?, title: String?) {
        fragList.add(fragment!!)
        fragTitle.add(title!!)
    }

    fun getTabTitle(position: Int): String {
        return fragTitle[position]
    }

    override fun getItemCount(): Int {
        return fragList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragList[position]
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}