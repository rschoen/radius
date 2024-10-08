package com.ryanschoen.radius.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

abstract class RadiusFragment : Fragment() {


    internal lateinit var viewModel: RadiusViewModel



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.startedDownloadingVenues.observe(viewLifecycleOwner) { started ->
            if (started) {
                showLoadingIndicator()
                viewModel.onStartedDownloadingVenues()
            }
        }
        viewModel.doneDownloadingVenues.observe(viewLifecycleOwner) { done ->
            if (done) {
                hideLoadingIndicator()
                viewModel.onDoneDownloadingVenues()
            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            viewModel.clearCurrentUser()
        } else {
            viewModel.setCurrentUser(user)
        }


    }


    abstract fun navigateToSetup()
    abstract fun showLoadingIndicator()
    abstract fun hideLoadingIndicator()

}