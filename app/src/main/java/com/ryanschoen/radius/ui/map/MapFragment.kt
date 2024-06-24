package com.ryanschoen.radius.ui.map

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import android.Manifest
import androidx.core.app.ActivityCompat
import com.ryanschoen.radius.R
import com.ryanschoen.radius.databinding.FragmentMapBinding
import com.ryanschoen.radius.databinding.VenueInfoWindowBinding
import com.ryanschoen.radius.domain.Venue
import com.ryanschoen.radius.getPixelsFromDp
import com.ryanschoen.radius.metersEquals
import com.ryanschoen.radius.ui.RadiusFragment
import com.ryanschoen.radius.yelpIntent
import timber.log.Timber


class MapFragment : RadiusFragment(), OnMapReadyCallback, OnRequestPermissionsResultCallback {


    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var map: GoogleMap? = null
    private lateinit var homeLatLng: LatLng

    private var locationPermissionDenied = false
    private var askedForLocationPermission = false

    private lateinit var infoWindowBinding: VenueInfoWindowBinding
    private lateinit var infoWindow: ViewGroup

    private var venuesOnMap: Int = 0
    private var maxVenueDistanceOnMap: Double = 0.0
    private var maxVisitedDistanceOnMap: Double = 0.0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(this)[MapViewModel::class.java]

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root





        viewModel.navigateToSetup.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                Timber.i("Navigating to setup...")
                this.findNavController()
                    .navigate(MapFragmentDirections.actionNavigationMapToNavigationSetup(false))
                viewModel.onNavigateToSetupDone()
            }
        }



        if ((viewModel as MapViewModel).addressIsReady) {

            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(), R.raw.map_style
            )
        )
        binding.mapRelativeLayout.init(map, getPixelsFromDp(requireContext(), (39 + 20).toFloat()))
        infoWindowBinding = VenueInfoWindowBinding.inflate(layoutInflater)
        infoWindowBinding.infoWindowVisitedCheckbox.setOnTouchListener { v, m ->
            if (m.action == MotionEvent.ACTION_UP) {
                val newState = !(v as CheckBox).isChecked
                v.performClick()
                infoWindowBinding.venue!!.visited = newState
                viewModel.setVenueState(infoWindowBinding.venue!!.id, newState, infoWindowBinding.venue!!.hidden)
                binding.mapRelativeLayout.redrawMarker(newState)
            }
            false
        }
        infoWindow = infoWindowBinding.root as ViewGroup

        map!!.setInfoWindowAdapter(
            VenueInfoWindowAdapter(
                infoWindowBinding,
                binding.mapRelativeLayout
            )
        )
        map!!.setOnInfoWindowClickListener { marker -> onInfoWindowClick(marker) }
        homeLatLng = LatLng(viewModel.getHomeLat(), viewModel.getHomeLng())
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 15.0f))
        Timber.i("Moving map to $homeLatLng")

        Timber.i("onMapReady complete, trying to set up map...")
        setupMap()

    }

    private fun drawMap() {
        var infoWindowVenueId = ""

        map?.let {
            Timber.d("Redrawing the map from scratch")

            binding.mapRelativeLayout.marker?.apply {
                infoWindowVenueId = (tag as Venue).id
            }

            map!!.clear()
            val homeLatLng = LatLng(viewModel.getHomeLat(), viewModel.getHomeLng())

            map!!.addMarker(
                MarkerOptions().position(homeLatLng).title("Home Base").icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            )

            if ((viewModel as MapViewModel).venues.value != null && (viewModel as MapViewModel).venues.value!!.isNotEmpty()) {
                for (venue in (viewModel as MapViewModel).venues.value!!) {
                    val position = LatLng(venue.lat, venue.lng)
                    val marker = map!!.addMarker(
                        MarkerOptions().position(position).apply {
                            if (venue.visited) {
                                icon(
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                                )
                            }
                        }
                    )
                    marker?.let {
                        marker.tag = venue
                        if (venue.id == infoWindowVenueId) {
                            marker.showInfoWindow()
                        }
                    }

                }
            }




            if (maxVenueDistanceOnMap > 0) {
                val circleOptions = CircleOptions()
                    .center(homeLatLng)
                    .radius(maxVenueDistanceOnMap)
                    .strokeColor(Color.GRAY)
                map!!.addCircle(circleOptions)

            } else {
                Timber.d("No max venue distance")
            }

            if (maxVisitedDistanceOnMap > 0) {
                val circleOptions = CircleOptions()
                    .center(homeLatLng)
                    .radius(maxVisitedDistanceOnMap)
                    .strokeColor(Color.GREEN)
                map!!.addCircle(circleOptions)

            } else {
                Timber.d("No max visited distance")
            }

            binding.mapRelativeLayout.marker?.apply {
                Timber.d("Showing info window...")
                showInfoWindow()
            }

        }

    }

    override fun onResume() {
        super.onResume()
        Timber.i("onResume called, trying to set up map...")
        setupMap()
    }

    private fun setupMap() {
        if (map == null || view == null) {
            return
        }

        Timber.i("Setting up map...")

        (viewModel as MapViewModel).venues.observe(viewLifecycleOwner) { venues ->
            var redraw = false
            if (venues.size != venuesOnMap) {
                redraw = true
                venuesOnMap = venues.size
            }

            var maxVisitedDistance = 0.0
            var maxVenueDistance = 0.0
            var haveSeenUnvisited = false
            for (venue in venues) {
                // distance guaranteed to be increasing
                maxVenueDistance = venue.distance
                if (!venue.visited) {
                    haveSeenUnvisited = true
                }
                if (!haveSeenUnvisited) {
                    maxVisitedDistance = venue.distance
                }
            }


            if (!metersEquals(maxVenueDistance, maxVenueDistanceOnMap) ||
                !metersEquals(maxVisitedDistance, maxVisitedDistanceOnMap)
            ) {
                maxVenueDistanceOnMap = maxVenueDistance
                maxVisitedDistanceOnMap = maxVisitedDistance
                redraw = true

            }

            if (redraw) {
                drawMap()
            }

        }

        (viewModel as MapViewModel).tenthVenueDistance.observe(viewLifecycleOwner) { distance ->

            distance?.let {
                val zoom = distanceToZoom(distance)
                Timber.i("Distance: %f, Zoom: %f", distance, zoom)
                map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoom))
                //}
            }
            (viewModel as MapViewModel).tenthVenueDistance.removeObservers(viewLifecycleOwner)
        }

        if(!locationPermissionDenied) {
            enableMyLocation()
        }

    }

    private fun onInfoWindowClick(p0: Marker) {
        yelpIntent(requireContext(), (p0.tag as Venue).url)
    }

    override fun navigateToSetup() {
        findNavController().navigate(MapFragmentDirections.actionNavigationMapToNavigationSetup(true))
    }


    private fun distanceToZoom(distance: Double): Float {
        return if (distance < 100) {
            18.0f
        } else if (distance < 200) {
            17.0f
        } else if (distance < 500) {
            16.0f
        } else if (distance < 1000) {
            15.0f
        } else if (distance < 2000) {
            14.0f
        } else if (distance < 5000) {
            13.0f
        } else if (distance < 10_000) {
            12.0f
        } else if (distance < 20_000) {
            11.0f
        } else 10.0f
    }

    override fun showLoadingIndicator() {
        binding.loadingVenuesSpinnerCard.visibility = View.VISIBLE
    }

    override fun hideLoadingIndicator() {
        binding.loadingVenuesSpinnerCard.visibility = View.GONE
    }
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map?.let { it.isMyLocationEnabled = true }

            return
        }

        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            locationPermissionDenied = true
            return
        }

        // 3. Otherwise, request permission
        if(!askedForLocationPermission) {
            askedForLocationPermission = true
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
            || isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            locationPermissionDenied = true
        }
    }
    private fun isPermissionGranted(permission: String) : Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

}


