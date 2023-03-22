package com.ryanschoen.radius.ui.setup

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.ryanschoen.radius.BuildConfig
import com.ryanschoen.radius.MainActivity
import com.ryanschoen.radius.R
import com.ryanschoen.radius.databinding.FragmentSetupBinding
import timber.log.Timber


class SetupFragment : Fragment() {

    private val viewModel: SetupViewModel by lazy {
        requireActivity()
        ViewModelProvider(this)[SetupViewModel::class.java]
    }


    private var _binding: FragmentSetupBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: SetupFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize the SDK
        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(requireContext())
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG))
            .setHint(getString(R.string.search_for_address))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Handler().postDelayed({
                    autocompleteFragment.setText(place.getAddress())
                }, 300)
                autocompleteFragment.setText(place.address)
                binding.venuesStatusIcon.setImageResource(R.drawable.baseline_change_circle_36)
                val r = RotateAnimation(
                    360f,
                    0f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                r.duration = 800
                r.repeatCount = Animation.INFINITE
                binding.venuesStatusIcon.startAnimation(r)

                binding.venuesStatusIcon.visibility = View.VISIBLE

                binding.venuesStatusText.text = getString(R.string.venue_search_processing)
                binding.venuesStatusText.visibility = View.VISIBLE
                viewModel.loadVenues(place.address, place.latLng)
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Timber.i("An error occurred: $status")
            }
        })



        viewModel.venuesChanged.observe(viewLifecycleOwner) { changed ->
            Timber.i("Venues list changed")
            if (changed) {
                binding.venuesStatusIcon.clearAnimation()
                if (viewModel.numVenues.value == 0) {
                    binding.venuesStatusIcon.setImageResource(R.drawable.baseline_dangerous_36)
                    binding.venuesStatusText.text = getText(R.string.venue_search_failed)
                } else {
                    binding.venuesStatusIcon.setImageResource(R.drawable.baseline_check_circle_36)
                    binding.venuesStatusText.text =
                        "Downloaded ${viewModel.numVenues.value} venues!"
                    findNavController().navigate(SetupFragmentDirections.actionNavigationSetupToNavigationMap())
                }
                viewModel.onVenuesChangedComplete()
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).showUpButton(args.isAddressAlreadySet)

    }






}