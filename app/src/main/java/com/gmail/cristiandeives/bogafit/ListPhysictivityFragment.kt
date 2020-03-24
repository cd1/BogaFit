package com.gmail.cristiandeives.bogafit

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
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

        binding.apply {
            action = this@ListPhysictivityFragment

            physictivityRecyclerView.adapter = adapter
            physictivityRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.listPhysictivityStatus.observe(viewLifecycleOwner) { res: Resource<List<Physictivity>>? ->
            Log.v(TAG, "> listPhysictivitiesStatus#onChanged(t=$res)")

            when (res) {
                is Resource.Loading -> onListPhysictivitiesLoading()
                is Resource.Success -> onListPhysictivitiesSuccess(res)
                is Resource.Error -> onListPhysictivitiesError()
                is Resource.Canceled -> onListPhysictivitiesCanceled()
            }

            if (res?.isFinished == true) {
                listPhysictivitiesProgressDialog.dismiss()
            }

            Log.v(TAG, "< listPhysictivitiesStatus#onChanged(t=$res)")
        }

        Log.v(TAG, "< onViewCreated(...)")
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
    private fun onListPhysictivitiesError() {

    }

    @UiThread
    private fun onListPhysictivitiesCanceled() {

    }

    companion object {
        private val TAG = ListPhysictivityFragment::class.java.simpleName
    }
}