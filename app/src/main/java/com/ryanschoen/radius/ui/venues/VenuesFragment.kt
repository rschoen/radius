package com.ryanschoen.radius.ui.venues

import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ryanschoen.radius.databinding.FragmentVenuesBinding
import com.ryanschoen.radius.ui.RadiusFragment
import com.ryanschoen.radius.venueDetailsIntent
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


        (viewModel as VenuesViewModel).venues.observe(viewLifecycleOwner) { venues ->
            if (!listLoaded) {
                adapter = VenueAdapter(
                    venues,
                    VenueAdapter.OnClickListener { venue ->
                        venueDetailsIntent(requireContext(), venue.url)
                    },
                    VenueAdapter.OnLongClickListener { venue ->
                        (viewModel as VenuesViewModel).setVenueState(venue.id, venue.visited, !venue.hidden)
                        true
                    },
                    VenueAdapter.OnCheckListener { venue, checked ->
                        if (venue.visited != checked) {
                            Timber.i("Changed venue ${venue.id} from ${venue.visited} to $checked")
                            viewModel.setVenueState(
                                venue.id,
                                checked,
                                venue.hidden
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

        val pullToRefresh: SwipeRefreshLayout = binding.pullToRefresh
        pullToRefresh.setOnRefreshListener {
            viewModel.refreshData()
            pullToRefresh.isRefreshing = false
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
        binding.loadingVenuesSpinnerCard.visibility = View.VISIBLE
    }

    override fun hideLoadingIndicator() {
        binding.loadingVenuesSpinnerCard.visibility = View.GONE
    }

}

