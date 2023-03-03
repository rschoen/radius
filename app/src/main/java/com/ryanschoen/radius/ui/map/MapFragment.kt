package com.ryanschoen.radius.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ryanschoen.radius.R
import com.ryanschoen.radius.databinding.FragmentMapBinding
import timber.log.Timber


class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var map: GoogleMap? = null
    private lateinit var viewModel: MapViewModel
    private lateinit var homeLatLng: LatLng

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(this).get(MapViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root



        viewModel.navigateToSetup.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                this.findNavController()
                    .navigate(MapFragmentDirections.actionNavigationMapToNavigationSetup(false))
                viewModel.onNavigateToSetupDone()
            }
        })

        viewModel.quitActivity.observe(viewLifecycleOwner, Observer { quit ->
            if(quit) {
                requireActivity().finish()
            }
        })

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setHasOptionsMenu(true)

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.overflow_menu, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.change_home_base -> {
                findNavController().navigate(MapFragmentDirections.actionNavigationMapToNavigationSetup(true))
                true
            }
            R.id.clear_data -> {
                viewModel.clearAllData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.setInfoWindowAdapter(VenueInfoWindowAdapter(requireContext()))
        homeLatLng = LatLng(viewModel.getHomeLat(), viewModel.getHomeLng())
        map!!.moveCamera(CameraUpdateFactory.newLatLng(homeLatLng))
        Timber.i("Moving map to ${homeLatLng.toString()}")
        setupMap()

    }

    override fun onResume() {
        super.onResume()
        setupMap()
    }

    fun setupMap() {
        if (map == null || view == null) {
            return
        }

        Timber.i("Setting up map...")

        viewModel.venues.observe(viewLifecycleOwner, Observer { venues ->
            Timber.i("Venues observer called")
            map!!.clear()

            val homeLatLng = LatLng(viewModel.getHomeLat(), viewModel.getHomeLng())

            map!!.addMarker(
                MarkerOptions().position(homeLatLng).title("Home Base").icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            )

            for (venue in venues) {
                val position = LatLng(venue.lat, venue.lng)
                map!!.addMarker(
                    MarkerOptions().position(position).title(venue.name)
                        .snippet("${venue.reviews} reviews, ${venue.rating} stars")
                )?.tag = venue
                //Timber.i("Adding to map: ${venue.name}")
            }

        })

        viewModel.tenthVenueDistance.observe(viewLifecycleOwner, Observer { distance ->
            Timber.i("Distance observer called")
            //if() {
            //var distance = distanceList.get(0)
            var zoom = 14.0f

            if (distance != null) {


                if (distance < 100) {
                    zoom = 18.0f
                } else if (distance < 200) {
                    zoom = 17.0f
                } else if (distance < 500) {
                    zoom = 16.0f
                } else if (distance < 1000) {
                    zoom = 15.0f
                } else if (distance < 2000) {
                    zoom = 14.0f
                } else if (distance < 5000) {
                    zoom = 13.0f
                } else if (distance < 10_000) {
                    zoom = 12.0f
                } else if (distance < 20_000) {
                    zoom = 11.0f
                } else zoom = 10.0f
                Timber.i("Distance: %f, Zoom: %f", distance, zoom)
                map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoom))
                //}
            }
        })


        //TODO: dynamically set this based on how close the closest venue is
        //enableMyLocation()
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /*@SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if(isPermissionGranted()) {
            map.isMyLocationEnabled = true
        }
        else {
            requestPermissions(arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),REQUEST_LOCATION_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == REQUEST_LOCATION_PERMISSION) {
            Timber.i("Got location permission back! ${grantResults.size.toString()} and ${grantResults[0].toString()}")
            if(grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Timber.i("Turn that baby on!")
                enableMyLocation()
            }
        }
    }*/


}

