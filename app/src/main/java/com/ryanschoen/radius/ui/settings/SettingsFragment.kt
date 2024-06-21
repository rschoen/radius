package com.ryanschoen.radius.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ryanschoen.radius.BuildConfig
import com.ryanschoen.radius.databinding.FragmentSettingsBinding
import com.ryanschoen.radius.ui.RadiusFragment


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
            binding.clearYelpDataButton.visibility = View.VISIBLE
        }

        (viewModel as SettingsViewModel).quitActivity.observe(viewLifecycleOwner) { quit ->
            if (quit) {
                requireActivity().finish()
            }
        }

        binding.changeHomeBaseButton.setOnClickListener {
            navigateToSetup()
        }
        binding.clearDataButton.setOnClickListener {
            (viewModel as SettingsViewModel).clearAllData()
        }
        binding.clearYelpDataButton.setOnClickListener {
            (viewModel as SettingsViewModel).clearYelpData()
        }
//
//        val welcomeVisibility = if (args.isAddressAlreadySet) {
//            View.GONE
//        } else {
//            View.VISIBLE
//        }
//
//        binding.welcomeExplainer.visibility = welcomeVisibility
//        binding.welcomeImage.visibility = welcomeVisibility
//        binding.welcomeTitle.visibility = welcomeVisibility
//        binding.welcomeCallToAction.visibility = welcomeVisibility
//
//
//
//
//        viewModel.venuesChanged.observe(viewLifecycleOwner) { changed ->
//            if (changed) {
//                binding.venuesStatusIcon.clearAnimation()
//                if (viewModel.numVenues.value == 0) {
//                    binding.venuesStatusIcon.setImageResource(R.drawable.baseline_dangerous_36)
//                    binding.venuesStatusText.text = getText(R.string.venue_search_failed)
//                } else {
//                    binding.venuesStatusIcon.setImageResource(R.drawable.baseline_check_circle_36)
//                    binding.venuesStatusText.text = String.format(
//                        getString(R.string.downloaded_venues), viewModel.numVenues.value
//                    )
//                    findNavController().navigate(SetupFragmentDirections.actionNavigationSetupToNavigationMap())
//                }
//                viewModel.onVenuesChangedComplete()
//            }
//        }
        return root
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

}