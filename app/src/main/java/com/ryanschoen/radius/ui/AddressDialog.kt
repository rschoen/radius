package com.ryanschoen.radius.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.ryanschoen.radius.R
import timber.log.Timber

class AddressDialog : DialogFragment() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.fragment_address_dialog, null))
                .setTitle(getString(R.string.address_dialog_title))
                .setNegativeButton("Cancel", DialogInterface.OnClickListener {dialog, which -> dialogCancelled() })
                .setPositiveButton("Save Address", DialogInterface.OnClickListener {dialog, which -> saveAddress( ((dialog as AlertDialog).findViewById(R.id.address) as EditText).text.toString()) })
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dialogCancelled()
    }

    fun dialogCancelled() {
        if(sharedPref.getString(getString(R.string.saved_address),null).isNullOrBlank()) {
            Timber.i("Didn't get an address, closing out!")
            requireActivity().finish()
        }
    }

    fun saveAddress(address: String) {

        Timber.i("Good news, got an address! It's ${address}")
        AddressManager.validateAndSaveAddress(address) { wasSuccessful, validatedAddress, lat, long ->
            if (wasSuccessful) {
                Timber.i("Address validated, now it's ${validatedAddress}")
                with(sharedPref.edit()) {
                    putString(getString(R.string.saved_address), address)
                    putFloat(getString(R.string.saved_lat), lat.toFloat())
                    putFloat(getString(R.string.saved_long), long.toFloat())
                    apply()
                }
            } else {
                Timber.i( "Address validation failed :(")
                //TODO: show an error and redo-validation
            }
        }

    }


}
