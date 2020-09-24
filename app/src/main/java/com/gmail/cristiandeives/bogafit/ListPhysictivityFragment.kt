package com.gmail.cristiandeives.bogafit

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmail.cristiandeives.bogafit.data.Physictivity
import com.gmail.cristiandeives.bogafit.databinding.FragmentListPhysictivityBinding
import java.text.NumberFormat

@MainThread
class ListPhysictivityFragment : Fragment(),
        ListPhysictivityActionHandler {

    private lateinit var binding: FragmentListPhysictivityBinding
    private val navController by lazy { findNavController() }
    private val viewModel by viewModels<ListPhysictivityViewModel>()
    private val adapter = ListPhysictivityRecyclerAdapter()
    private val integerFormatter = NumberFormat.getIntegerInstance()

    private val listPhysictivitiesProgressDialog by lazy {
        ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.list_physictivities_loading_message))
            setCancelable(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(...)")

        binding = FragmentListPhysictivityBinding.inflate(inflater, container, false)

        val view = binding.root
        Log.v(TAG, "< onCreateView(...): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(...)")
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(viewModel)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner

            vm = viewModel
            action = this@ListPhysictivityFragment

            physictivityRecyclerView.adapter = adapter
            physictivityRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.apply {
            listPhysictivityStatus.observe(viewLifecycleOwner) { res: Resource<List<Physictivity>>? ->
                Log.v(TAG, "> listPhysictivitiesStatus#onChanged(t=$res)")

                when (res) {
                    is Resource.Loading -> onListPhysictivitiesLoading()
                    is Resource.Success -> onListPhysictivitiesSuccess(res)
                    is Resource.Error -> onListPhysictivitiesError(res)
                    is Resource.Canceled -> onListPhysictivitiesCanceled()
                }

                if (res?.isFinished == true) {
                    listPhysictivitiesProgressDialog.dismiss()
                }

                Log.v(TAG, "< listPhysictivitiesStatus#onChanged(t=$res)")
            }
        }

        setHasOptionsMenu(true)

        Log.v(TAG, "< onViewCreated(...)")
    }

    override fun onStart() {
        Log.v(TAG, "> onStart()")
        super.onStart()

        val goalVisibility = if (viewModel.isPhysictivityGoalEnabled) View.VISIBLE else View.GONE

        binding.apply {
            physictivityGoalSeparator.visibility = goalVisibility

            physictivityGoalValue.apply {
                visibility = goalVisibility
                text = integerFormatter.format(viewModel.physictivityGoal)
            }
        }

        Log.v(TAG, "< onStart()")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.v(TAG, "> onCreateOptionsMenu(...)")

        inflater.inflate(R.menu.list_physictivity, menu)

        Log.v(TAG, "< onCreateOptionsMenu(...)")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "> onOptionsItemSelected(item=${resources.getResourceName(item.itemId)})")

        Log.i(TAG, "user selected menu=$item")

        val consumed = when (item.itemId) {
            R.id.settings -> {
                val intent = Intent(requireContext(), SettingsActivity::class.java)
                startActivity(intent)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

        Log.v(TAG, "< onOptionsItemSelected(item=${resources.getResourceName(item.itemId)}): $consumed")
        return consumed
    }

    override fun onDestroyView() {
        Log.v(TAG, "> onDestroyView()")
        super.onDestroyView()

        lifecycle.removeObserver(viewModel)

        Log.v(TAG, "< onDestroyView()")
    }

    override fun onAddPhysictivityButtonClick(view: View) {
        val action = ListPhysictivityFragmentDirections.toAddPhysictivity()
        navController.navigate(action)
    }

    @UiThread
    private fun onListPhysictivitiesLoading() {
        listPhysictivitiesProgressDialog.show()
    }

    @UiThread
    private fun onListPhysictivitiesSuccess(res: Resource.Success<List<Physictivity>>) {
        val physictivities = res.data ?: emptyList()

        binding.physictivityCountValue.text = integerFormatter.format(physictivities.size)
        adapter.data = physictivities
    }

    @UiThread
    private fun onListPhysictivitiesError(res: Resource.Error<List<Physictivity>>) {
        res.exception?.consume()?.let { ex ->
            val message = when (ex as ListPhysictivityViewModel.Error) {
                is ListPhysictivityViewModel.Error.Server -> R.string.list_physictivities_error_server
            }

            requireView().showMessage(message)
        }
    }

    @UiThread
    private fun onListPhysictivitiesCanceled() {
        // nothing
    }

    companion object {
        private val TAG = ListPhysictivityFragment::class.java.simpleName
    }
}