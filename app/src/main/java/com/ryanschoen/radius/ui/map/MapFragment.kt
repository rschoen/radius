package com.ryanschoen.radius.ui.map

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.ryanschoen.radius.R
import com.ryanschoen.radius.databinding.FragmentMapBinding
import com.ryanschoen.radius.databinding.VenueInfoWindowBinding
import com.ryanschoen.radius.domain.Venue
import com.ryanschoen.radius.yelpIntent
import timber.log.Timber


class MapFragment : Fragment(), OnMapReadyCallback {


    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var map: GoogleMap? = null
    private lateinit var viewModel: MapViewModel
    private lateinit var homeLatLng: LatLng

    private lateinit var infoWindowBinding: VenueInfoWindowBinding
    private lateinit var infoWindow: ViewGroup

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
                this.findNavController()
                    .navigate(MapFragmentDirections.actionNavigationMapToNavigationSetup(false))
                viewModel.onNavigateToSetupDone()
            }
        }

        viewModel.quitActivity.observe(viewLifecycleOwner) { quit ->
            if (quit) {
                requireActivity().finish()
            }
        }

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
        binding.mapRelativeLayout.init(map, getPixelsFromDp(requireContext(), (39+20).toFloat()))
        infoWindowBinding = VenueInfoWindowBinding.inflate(layoutInflater)
        infoWindowBinding.infoWindowVisitedCheckbox.setOnTouchListener  { v,m ->
            if(m.action == MotionEvent.ACTION_UP) {
                val newState = !(v as CheckBox).isChecked
                (v as CheckBox).isChecked = newState
                infoWindowBinding.venue!!.visited = newState
                Timber.i("Setting visited equal to " + newState.toString())
                viewModel.setVenueVisited(infoWindowBinding.venue!!.id, newState)
                binding.mapRelativeLayout.redrawMarker(newState)
            }
            false
        }
        infoWindow = infoWindowBinding.root as ViewGroup
        /*infoWindowBinding.venueName.setOnTouchListener { _,m ->
            if(m.action == MotionEvent.ACTION_UP) {
                onInfoWindowClick(binding.mapRelativeLayout.marker!!)
                true
            }
            false
        }*/
        map!!.setInfoWindowAdapter(VenueInfoWindowAdapter(requireContext(), infoWindowBinding, binding.mapRelativeLayout))
        map!!.setOnInfoWindowClickListener { marker -> onInfoWindowClick(marker) }
        homeLatLng = LatLng(viewModel.getHomeLat(), viewModel.getHomeLng())
        map!!.moveCamera(CameraUpdateFactory.newLatLng(homeLatLng))
        Timber.i("Moving map to $homeLatLng")
        setupMap()

    }

    override fun onResume() {
        super.onResume()
        setupMap()
    }

    private fun setupMap() {
        if (map == null || view == null) {
            return
        }

        Timber.i("Setting up map...")

        viewModel.venues.observe(viewLifecycleOwner) { venues ->
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
                    MarkerOptions().position(position).apply {
                            if(venue.visited) {
                                icon(
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                                )
                            }
                        }
                )?.tag = venue
                //Timber.i("Adding to map: ${venue.name}")
            }
            viewModel.venues.removeObservers(viewLifecycleOwner)

        }

        viewModel.tenthVenueDistance.observe(viewLifecycleOwner) { distance ->
            Timber.i("Distance observer called")
            //if() {
            //var distance = distanceList.get(0)
            val zoom: Float

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
            viewModel.tenthVenueDistance.removeObservers(viewLifecycleOwner)
        }

        //map!!.setOnInfoWindowClickListener(this)


        //enableMyLocation()
    }

    fun onInfoWindowClick(p0: Marker) {
        yelpIntent(requireContext(), (p0.tag as Venue).url)
        //this.findNavController().navigate(MapFragmentDirections.actionNavigationMapToNavigationVenues((p0.tag as Venue).id))
    }

/*
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
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

    fun getPixelsFromDp(context: Context, dp: Float): Int {
        val scale: Float = context.getResources().getDisplayMetrics().density
        return (dp * scale + 0.5f).toInt()
    }


}

