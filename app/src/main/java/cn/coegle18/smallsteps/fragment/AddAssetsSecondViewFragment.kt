package cn.coegle18.smallsteps.fragment

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.adapter.AddAccountAdapter
import cn.coegle18.smallsteps.viewmodel.AddAssetsSecondViewModel
import cn.coegle18.smallsteps.viewmodel.AddAssetsSecondViewModelFactory
import kotlinx.android.synthetic.main.fragment_add_assets_second_view.*

class AddAssetsSecondViewFragment : Fragment() {

    private lateinit var viewModel: AddAssetsSecondViewModel
    private val args: AddAssetsSecondViewFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_assets_second_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mAdapter = AddAccountAdapter(R.layout.item_add_asset, mutableListOf())
        mAdapter.setOnItemClickListener { _, _, position ->
            val accountType = viewModel.accountTypeList.value?.get(position)
            if (accountType != null) {
                val action =
                    AddAssetsFragmentDirections.addAssetAction(0L, accountType.accountTypeId)
                findNavController().navigate(action)
            }
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mAdapter
        }
        viewModel = ViewModelProvider(this, AddAssetsSecondViewModelFactory(Application(), args.pAccountType)).get(
                AddAssetsSecondViewModel::class.java)
        viewModel.accountTypeList.observe(viewLifecycleOwner, {
            mAdapter.setList(it)
        })
    }
}