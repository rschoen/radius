package com.ryanschoen.radius.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var viewModel: MapViewModel

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
                    .navigate(MapFragmentDirections.actionNavigationMapToNavigationSetup())
                viewModel.onNavigateToSetupDone()
            }
        })

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMap()

    }

    override fun onResume() {
        super.onResume()
        setupMap()
    }

    fun setupMap() {
        var constantMap = map
        if (constantMap == null || view == null) {
            return
        }

        val homeLatLng = LatLng(viewModel.getHomeLat(), viewModel.getHomeLng())


        viewModel.venues.observe(viewLifecycleOwner, Observer { venues ->
            constantMap.clear()


            constantMap.addMarker(
                MarkerOptions().position(homeLatLng).title("Home Base").icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            )

            for (venue in venues) {
                val position = LatLng(venue.lat, venue.lng)
                constantMap.addMarker(
                    MarkerOptions().position(position).title(venue.name)
                        .snippet("${venue.reviews} reviews, ${venue.rating} stars")
                )
                Timber.i("Adding to map: ${venue.name}")
            }
        })

        constantMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 16.0f))
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