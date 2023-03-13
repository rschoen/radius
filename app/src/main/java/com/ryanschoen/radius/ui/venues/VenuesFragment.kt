package com.ryanschoen.radius.ui.venues

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ryanschoen.radius.R
import com.ryanschoen.radius.databinding.FragmentVenuesBinding
import com.ryanschoen.radius.yelpIntent
import timber.log.Timber


class VenuesFragment : Fragment() {

    private var _binding: FragmentVenuesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewModel: VenuesViewModel

    private val args: VenuesFragmentArgs by navArgs()
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

        viewModel.venues.observe(viewLifecycleOwner) { venues ->
            if (!listLoaded) {
                adapter = VenueAdapter(
                    venues,
                    VenueAdapter.OnClickListener { venue ->
                        yelpIntent(requireContext(), venue.url)
                    },
                    VenueAdapter.OnCheckListener { venue, checked ->
                        if(venue.visited != checked) {
                            Timber.i("Changed venue ${venue.id} from ${venue.visited} to ${checked}")
                            viewModel.setVenueVisited(
                                venue.id,
                                checked
                            )
                        }
                    })
                binding.venueList.adapter = adapter
                val layoutManager = LinearLayoutManager(requireContext())

                if (args.venueId.isNotBlank()) {
                    for (position in venues.indices) {
                        if (venues[position].id == args.venueId) {
                            layoutManager.scrollToPosition(position)
                            break
                        }
                    }
                }

                binding.venueList.layoutManager = layoutManager
                listLoaded = true
                Timber.i("List has been loaded")
            }
            Timber.i("UPDATE THAT LIST!")
            adapter.submitList(venues)

            // TODO: we only want to do this on the first load. how do we know if it's first or not?
        }

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
                findNavController().navigate(VenuesFragmentDirections.actionNavigationVenuesToNavigationSetup(true))
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


}

