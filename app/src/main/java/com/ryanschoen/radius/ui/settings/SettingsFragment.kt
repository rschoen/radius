package com.ryanschoen.radius.ui.settings

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.ryanschoen.radius.BuildConfig
import com.ryanschoen.radius.R
import com.ryanschoen.radius.databinding.FragmentSettingsBinding
import com.ryanschoen.radius.ui.RadiusFragment
import timber.log.Timber


class SettingsFragment : RadiusFragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //private val args: SetupFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(this)[SettingsViewModel::class.java]
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (BuildConfig.DEBUG) {
            binding.clearDataButton.visibility = View.VISIBLE
            binding.clearNetworkDataButton.visibility = View.VISIBLE
        }

        (viewModel as SettingsViewModel).quitActivity.observe(viewLifecycleOwner) { quit ->
            if (quit) {
                requireActivity().finish()
            }
        }

        binding.changeHomeBaseButton.setOnClickListener {
            navigateToSetup()
        }
        binding.signInButton.setOnClickListener {
            if((viewModel as SettingsViewModel).userIsSignedIn) {
                (viewModel as SettingsViewModel).clearCurrentUser()
                AuthUI.getInstance()
                    .signOut(requireContext())
                    .addOnCompleteListener {
                        refreshSigninStatus()
                    }
            } else {
                val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
                val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build()
                signInLauncher.launch(signInIntent)
            }
        }
        binding.clearDataButton.setOnClickListener {
            (viewModel as SettingsViewModel).clearAllData()
        }
        binding.clearNetworkDataButton.setOnClickListener {
            (viewModel as SettingsViewModel).clearNetworkData()
        }

        (viewModel as SettingsViewModel).address?.let {
            binding.changeHomeBase.text = it
        }
        refreshSigninStatus()
        return root
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    private fun onSignInResult(res: FirebaseAuthUIAuthenticationResult) {
        //val response = res.idpResponse
        if (res.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let { viewModel.signIn(it) }
            Timber.d("FIREBASE AUTH: logged in with user %s", user.toString())
        } else {
            viewModel.clearCurrentUser()
            Timber.d("FIREBASE AUTH: sign in failed")
        }
        refreshSigninStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun navigateToSetup() {
        findNavController().navigate(
            SettingsFragmentDirections.actionNavigationSettingsToNavigationSetup(
                true
            )
        )
    }

    override fun showLoadingIndicator() {
        binding.loadingVenuesSpinnerCard.visibility = View.VISIBLE
    }

    override fun hideLoadingIndicator() {
        binding.loadingVenuesSpinnerCard.visibility = View.GONE
    }

    private fun refreshSigninStatus() {
        if((viewModel as SettingsViewModel).userIsSignedIn) {
            binding.signInTitle.text = "Sign out"
            binding.signIn.text = "Signed in as ${(viewModel as SettingsViewModel).userEmail}"
        } else {
            binding.signInTitle.text = resources.getString(R.string.sign_in)
            binding.signIn.text = resources.getString(R.string.sign_in_to_google_to_sync_your_data)
        }
    }
}