package com.quotum.quotum.quotum.ui.myProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.quotum.quotum.quotum.R


class MyProfileFragment : Fragment() {

    private lateinit var myProfileViewModel: MyProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        myProfileViewModel = ViewModelProvider(this).get(MyProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_my_profile, container, false)
//        val textView: TextView = root.findViewById(R.id.text_slideshow)
//        myProfileViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }
}