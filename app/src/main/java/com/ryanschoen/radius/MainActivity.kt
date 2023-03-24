package com.ryanschoen.radius

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ryanschoen.radius.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_map,
                R.id.navigation_venues
            )
        )

        val navView: BottomNavigationView = binding.navView
        navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if(destination.id == R.id.navigation_setup) {
                binding.navView.visibility = View.GONE
            } else {

                binding.navView.visibility = View.VISIBLE
            }
        }

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
        return if (backStackEntry != null) {
            navController.navigate(backStackEntry.destination.id)
            true
        } else {
            super.onSupportNavigateUp()
        }
    }
}