package com.ryanschoen.radius.ui

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.ryanschoen.radius.BuildConfig
import com.ryanschoen.radius.R
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

        setupMenu()
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        //signInLauncher.launch(signInIntent)

    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                if (!BuildConfig.DEBUG) {
                    val debugOptions = listOf(R.id.clear_data, R.id.clear_yelp_data)
                    for (option in debugOptions) {
                        val item = menu.findItem(option)
                        item.isVisible = false
                    }
                }
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.overflow_menu, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.change_home_base -> {
                        navigateToSetup()
                        true
                    }
                    R.id.clear_data -> {
                        viewModel.clearAllData()
                        true
                    }
                    R.id.clear_yelp_data -> {
                        viewModel.clearYelpData()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    abstract fun navigateToSetup()
    abstract fun showLoadingIndicator()
    abstract fun hideLoadingIndicator()

}