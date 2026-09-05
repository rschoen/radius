package com.ryanschoen.radius

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ryanschoen.radius.databinding.ActivityMainBinding
import timber.log.Timber


class MainActivity : AppCompatActivity(), OnMapsSdkInitializedCallback {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_map,
                R.id.navigation_venues,
            )
        )

        val navView: BottomNavigationView = binding.navView
        navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_setup) {
                binding.navView.visibility = View.GONE
            } else {

                binding.navView.visibility = View.VISIBLE
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    onSupportNavigateUp()
                }
            }
        )


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.statusBars())
            val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(v.layoutParams )
            params.setMargins(0, systemBarInsets.top, 0, systemBarInsets.bottom)
            v.layoutParams = params
            WindowInsetsCompat.CONSUMED
        }
    }
    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        if (renderer == MapsInitializer.Renderer.LATEST) {
            Timber.d( "The latest version of the renderer is used.")
        } else {
            Timber.d("The legacy version of the renderer is used.")
        }
    }

    fun showUpButton(show: Boolean) {
        supportActionBar!!.setDisplayHomeAsUpEnabled(show)
    }


    // Workaround to navigate back without triggering Maps SDK "background" mode
    // which causes it to drop to 2fps :(
    override fun onSupportNavigateUp(): Boolean {
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