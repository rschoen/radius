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

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    private fun onSignInResult(res: FirebaseAuthUIAuthenticationResult) {
        val response = res.idpResponse
        if (res.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            Timber.d("FIREBASE AUTH: logged in with user %s", user.toString())
        } else {
            Timber.d("FIREBASE AUTH: sign in failed")
        }
    }


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

        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        //signInLauncher.launch(signInIntent)

    }


    abstract fun navigateToSetup()
    abstract fun showLoadingIndicator()
    abstract fun hideLoadingIndicator()

}