package com.ryanschoen.radius.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.ryanschoen.radius.R
import com.ryanschoen.radius.databinding.FragmentMapBinding
import com.ryanschoen.radius.databinding.VenueInfoWindowBinding
import com.ryanschoen.radius.domain.Venue
import com.ryanschoen.radius.metersEquals
import com.ryanschoen.radius.ui.RadiusFragment
import com.ryanschoen.radius.yelpIntent
import timber.log.Timber


class MapFragment : RadiusFragment(), OnMapReadyCallback {


    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var map: GoogleMap? = null
    private lateinit var homeLatLng: LatLng

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

        viewModel.quitActivity.observe(viewLifecycleOwner) { quit ->
            if (quit) {
                requireActivity().finish()
            }
        }
        (viewModel as MapViewModel).doneDownloadingVenues.observe(viewLifecycleOwner) { done ->
            if(done) {
                //rezoomMap()
                (viewModel as MapViewModel).onDoneDownloadingVenues()
            }
        }


        if((viewModel as MapViewModel).addressIsReady) {

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
                requireContext(), R.raw.map_style))
        binding.mapRelativeLayout.init(map, getPixelsFromDp(requireContext(), (39+20).toFloat()))
        infoWindowBinding = VenueInfoWindowBinding.inflate(layoutInflater)
        infoWindowBinding.infoWindowVisitedCheckbox.setOnTouchListener  { v,m ->
            if(m.action == MotionEvent.ACTION_UP) {
                Timber.d("Caught the click action.")
                val newState = !(v as CheckBox).isChecked
                v.performClick()
                Timber.d("Setting the info window binding to visited = ${newState}.")
                infoWindowBinding.venue!!.visited = newState
                Timber.d("Calling viewModel's setVenueVisited with visited = ${newState}.")
                viewModel.setVenueVisited(infoWindowBinding.venue!!.id, newState)
                Timber.d("Asking the map overlay to redraw the marker.")
                binding.mapRelativeLayout.redrawMarker(newState)
            }
            false
        }
        infoWindow = infoWindowBinding.root as ViewGroup

        map!!.setInfoWindowAdapter(VenueInfoWindowAdapter(requireContext(), infoWindowBinding, binding.mapRelativeLayout))
        map!!.setOnInfoWindowClickListener { marker -> onInfoWindowClick(marker) }
        homeLatLng = LatLng(viewModel.getHomeLat(), viewModel.getHomeLng())
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 15.0f))
        Timber.i("Moving map to $homeLatLng")
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

            if((viewModel as MapViewModel).venues.value != null && (viewModel as MapViewModel).venues.value!!.isNotEmpty()) {
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
                        if(venue.id == infoWindowVenueId) {
                            Timber.d("Re-popping the info window!")
                            marker.showInfoWindow()
                        }
                    }

                }
            }




            if(maxVenueDistanceOnMap > 0) {
                val circleOptions = CircleOptions()
                    .center(homeLatLng)
                    .radius(maxVenueDistanceOnMap)
                    .strokeColor(Color.GRAY)
                map!!.addCircle(circleOptions)

            } else {
                Timber.d("No max venue distance")
            }

            if(maxVisitedDistanceOnMap > 0) {
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
        setupMap()
    }

    private fun setupMap() {
        if (map == null || view == null) {
            return
        }

        Timber.i("Setting up map...")

        (viewModel as MapViewModel).venues.observe(viewLifecycleOwner) { venues ->
            Timber.i("Venues observer called. Should we redraw?")
            var redraw = false
            if(venues.size != venuesOnMap) {
                Timber.i("Drawing map because # of venues changed!")
                redraw = true
                venuesOnMap = venues.size
            }

            var maxVisitedDistance = 0.0
            var maxVenueDistance = 0.0
            var haveSeenUnvisited = false
            for(venue in venues) {
                // distance guaranteed to be increasing
                maxVenueDistance = venue.distance
                if(!venue.visited) {
                    haveSeenUnvisited = true
                }
                if(!haveSeenUnvisited) {
                    maxVisitedDistance = venue.distance
                }
            }


            if(!metersEquals(maxVenueDistance,maxVenueDistanceOnMap) ||
                !metersEquals(maxVisitedDistance,maxVisitedDistanceOnMap)) {
                Timber.d("Drawing map because one of the circles changed")
                maxVenueDistanceOnMap = maxVenueDistance
                maxVisitedDistanceOnMap = maxVisitedDistance
                redraw = true

            }

            if(redraw) {
                drawMap()
            }



        }

        (viewModel as MapViewModel).tenthVenueDistance.observe(viewLifecycleOwner) { distance ->
            Timber.i("Distance observer called")

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
            (viewModel as MapViewModel).tenthVenueDistance.removeObservers(viewLifecycleOwner)
        }

    }

    private fun onInfoWindowClick(p0: Marker) {
        yelpIntent(requireContext(), (p0.tag as Venue).url)
    }

    override fun navigateToSetup() {
        findNavController().navigate(MapFragmentDirections.actionNavigationMapToNavigationSetup(true))
    }

    fun getPixelsFromDp(context: Context, dp: Float): Int {
        val scale: Float = context.getResources().getDisplayMetrics().density
        return (dp * scale + 0.5f).toInt()
    }


}

