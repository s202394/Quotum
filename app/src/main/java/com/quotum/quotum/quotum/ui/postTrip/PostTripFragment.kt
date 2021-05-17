package com.quotum.quotum.quotum.ui.postTrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.quotum.quotum.quotum.R

class PostTripFragment : Fragment() {

    private lateinit var postTripViewModel: PostTripViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        postTripViewModel = ViewModelProvider(this).get(PostTripViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_posttrip, container, false)
        /* val textView: TextView = root.findViewById(R.id.text_gallery)
         postTripViewModel.text.observe(viewLifecycleOwner, Observer {
             textView.text = it
         })*/
        return root
    }
}