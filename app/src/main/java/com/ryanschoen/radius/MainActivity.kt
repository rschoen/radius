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



    fun showUpButton(show: Boolean) {
        supportActionBar!!.setDisplayHomeAsUpEnabled(show)
    }


    // Workaround to navigate back without triggering Maps SDK "background" mode
    // which causes it to drop to 2fps :(
    override fun onSupportNavigateUp(): Boolean {

        /*Timber.i("Navigating up...")
        return findNavController(R.id.nav_host_fragment_activity_main).navigateUp()
                    || super.onSupportNavigateUp();*/

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val backStackEntry = navController.previousBackStackEntry
        if (backStackEntry != null) {
            navController.navigate(backStackEntry.destination.id)
            return true
        }
        else {
            return super.onSupportNavigateUp()
        }
    }
}