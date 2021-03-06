package com.quotum.quotum.quotum.ui.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.quotum.quotum.quotum.R
import com.quotum.quotum.quotum.localdatabase.LocalDB

class SendFragment : Fragment() {

    private lateinit var sendViewModel: SendViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sendViewModel = ViewModelProvider(this).get(SendViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_send, container, false)
        context?.let { LocalDB.setUserToken(it, null) }
        context?.let { LocalDB.clearAllAppData(it) }
        activity?.finish()
        return root
    }
}