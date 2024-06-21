package com.ryanschoen.radius.ui

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

abstract class RadiusFragment : Fragment() {


    internal lateinit var viewModel: RadiusViewModel
    private lateinit var auth: FirebaseAuth



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