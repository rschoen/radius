package com.ryanschoen.radius.ui.venues

import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ryanschoen.radius.databinding.FragmentVenuesBinding
import com.ryanschoen.radius.ui.RadiusFragment
import com.ryanschoen.radius.yelpIntent
import timber.log.Timber


class VenuesFragment : RadiusFragment() {

    private var _binding: FragmentVenuesBinding? = null
    private val binding get() = _binding!!

    private var listLoaded = false
    private lateinit var adapter: VenueAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(this)[VenuesViewModel::class.java]

        _binding = FragmentVenuesBinding.inflate(inflater, container, false)
        val root: View = binding.root


        viewModel.navigateToSetup.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                this.findNavController()
                    .navigate(VenuesFragmentDirections.actionNavigationVenuesToNavigationSetup(false))
                viewModel.onNavigateToSetupDone()
            }
        }

        viewModel.quitActivity.observe(viewLifecycleOwner) { quit ->
            if (quit) {
                requireActivity().finish()
            }
        }

        (viewModel as VenuesViewModel).venues.observe(viewLifecycleOwner) { venues ->
            if (!listLoaded) {
                adapter = VenueAdapter(
                    venues,
                    VenueAdapter.OnClickListener { venue ->
                        yelpIntent(requireContext(), venue.url)
                    },
                    VenueAdapter.OnLongClickListener { venue ->
                        (viewModel as VenuesViewModel).toggleVenueIsHidden(venue.id)
                        true
                    },
                    VenueAdapter.OnCheckListener { venue, checked ->
                        if (venue.visited != checked) {
                            Timber.i("Changed venue ${venue.id} from ${venue.visited} to ${checked}")
                            viewModel.setVenueVisited(
                                venue.id,
                                checked
                            )
                        }
                    })
                binding.venueList.adapter = adapter
                filterList()
                val layoutManager = LinearLayoutManager(requireContext())


                binding.venueList.layoutManager = layoutManager
                listLoaded = true
                Timber.i("List has been loaded")
            }
            Timber.i("UPDATE THAT LIST!")
            adapter.filterAndSubmitList(venues)

        }

        binding.checkboxFilterVisited.setOnClickListener {
            filterList()
        }
        binding.checkboxFilterUnvisited.setOnClickListener {
            filterList()
        }
        binding.checkboxFilterHidden.setOnClickListener {
            filterList()
        }

        return root
    }

    private fun filterList() {
        val adapter = binding.venueList.adapter as VenueAdapter
        val filter = adapter.filter as VenueAdapter.VenueFilter
        filter.setFilterAttributes(
            binding.checkboxFilterVisited.isChecked,
            binding.checkboxFilterUnvisited.isChecked,
            binding.checkboxFilterHidden.isChecked
        )
        filter.filter("")
    }


    override fun navigateToSetup() {
        findNavController().navigate(
            VenuesFragmentDirections.actionNavigationVenuesToNavigationSetup(
                true
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        listLoaded = false
    }

    override fun showLoadingIndicator() {
        val r = RotateAnimation(
            360f,
            0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        r.duration = 800
        r.repeatCount = Animation.INFINITE
        binding.loadingCircle.startAnimation(r)
        binding.loadingCircle.visibility = View.VISIBLE
    }

    override fun hideLoadingIndicator() {
        binding.loadingCircle.clearAnimation()
        binding.loadingCircle.visibility = View.GONE
    }

}

