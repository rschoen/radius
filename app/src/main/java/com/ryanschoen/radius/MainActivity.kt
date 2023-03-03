package com.ryanschoen.radius

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.ryanschoen.radius.databinding.ActivityMainBinding
import timber.log.Timber
import timber.log.Timber.Forest.plant


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
   // private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_map
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)



    }


    fun showAddressDialog() {
        //val addressDialog = AddressDialog()
        //addressDialog.show(supportFragmentManager, "address")

    }

    override fun onResume() {
        //if(sharedPref.getString(getString(R.string.saved_address),null).isNullOrBlank()) {
            //showAddressDialog()
        //}

        super.onResume()
    }

    fun showUpButton(show: Boolean) {
        supportActionBar!!.setDisplayHomeAsUpEnabled(show)
    }

    override fun onNavigateUp(): Boolean {
        return super.onNavigateUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment_activity_main).navigateUp()
                    || super.onSupportNavigateUp();
    }
}