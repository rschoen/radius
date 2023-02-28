package com.ryanschoen.radius.ui.setup

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ryanschoen.radius.R
import com.ryanschoen.radius.databinding.FragmentSetupBinding
import timber.log.Timber

class SetupFragment : Fragment(), AdapterView.OnItemSelectedListener {

    companion object {
        fun newInstance() = SetupFragment()
    }

    private val viewModel: SetupViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access viewModel after onViewCreated()"
        }
        ViewModelProvider(this).get(SetupViewModel::class.java)
    }


    private var _binding: FragmentSetupBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.addressEdittext.doOnTextChanged { _, _, _, _ -> checkIfEntryComplete() }
        binding.cityEdittext.doOnTextChanged { _, _, _, _ -> checkIfEntryComplete() }
        binding.stateSpinner.onItemSelectedListener = this

        binding.saveAddress.setOnClickListener() {
            binding.saveAddress.isEnabled = false
            binding.verificationStatusIcon.setImageResource(R.drawable.baseline_change_circle_36)
            binding.verificationStatusIcon.visibility = View.VISIBLE
            binding.verificationStatusText.text = getText(R.string.verification_processing)
            binding.verificationStatusText.visibility = View.VISIBLE
            binding.venuesStatusText.visibility = View.GONE
            binding.venuesStatusIcon.visibility = View.GONE
            viewModel.verifyAddress(binding.addressEdittext.text.toString(),
                                    binding.cityEdittext.text.toString(),
                                    binding.stateSpinner.selectedItem.toString())
        }

        val spinner = binding.stateSpinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.states_abbreviations,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        viewModel.addressChanged.observe(viewLifecycleOwner, Observer { changed ->
            Timber.i("Changed: " + changed.toString())
            if(changed) {
                if(viewModel.verifiedAddress.value.isNullOrBlank()) {
                    binding.verificationStatusIcon.setImageResource(R.drawable.baseline_dangerous_36)
                    binding.verificationStatusText.text = getText(R.string.verification_failed)
                }
                else {
                    binding.verificationStatusIcon.setImageResource(R.drawable.baseline_check_circle_36)
                    binding.verificationStatusText.text = viewModel.verifiedAddress.value

                    binding.venuesStatusIcon.setImageResource(R.drawable.baseline_change_circle_36)
                    binding.venuesStatusIcon.visibility = View.VISIBLE

                    binding.venuesStatusText.text = getString(R.string.venue_search_processing)
                    binding.venuesStatusText.visibility = View.VISIBLE
                }
                viewModel.onAddressChangedComplete()
            }
        })

        viewModel.venuesChanged.observe(viewLifecycleOwner, Observer { changed ->
            if(changed) {
                if(viewModel.numVenues.value == 0) {
                    binding.venuesStatusIcon.setImageResource(R.drawable.baseline_dangerous_36)
                    binding.venuesStatusText.text = getText(R.string.venue_search_failed)
                }
                else {
                    binding.venuesStatusIcon.setImageResource(R.drawable.baseline_check_circle_36)
                    binding.venuesStatusText.text = "Downloaded ${viewModel.numVenues.value} venues!"
                    findNavController().navigate(SetupFragmentDirections.actionNavigationSetupToNavigationMap())
                }
                viewModel.onVenuesChangedComplete()
            }
        })

        return root
    }


    private fun checkIfEntryComplete() {
        if (binding.addressEdittext.text.isEmpty() ||
                binding.cityEdittext.text.isEmpty() ||
                binding.stateSpinner.selectedItem.toString() == "State") {
            binding.saveAddress.isEnabled = false
        }
        else {
            binding.saveAddress.isEnabled = true
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        checkIfEntryComplete()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        checkIfEntryComplete()
    }



}