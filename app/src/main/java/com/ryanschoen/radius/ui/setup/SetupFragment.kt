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
import com.ryanschoen.radius.R
import com.ryanschoen.radius.databinding.FragmentSetupBinding

class SetupFragment : Fragment(), AdapterView.OnItemSelectedListener {

    companion object {
        fun newInstance() = SetupFragment()
    }

    private lateinit var viewModel: SetupViewModel


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

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SetupViewModel::class.java)
        //viewModel.checkForAddress()
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