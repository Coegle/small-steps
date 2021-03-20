package cn.coegle18.smallsteps.fragment

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.coegle18.smallsteps.PrimaryAccountType
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.adapter.AddAccountAdapter
import cn.coegle18.smallsteps.entity.AccountView
import cn.coegle18.smallsteps.viewmodel.SubAddAssetsViewModel
import cn.coegle18.smallsteps.viewmodel.SubAddAssetsViewModelFactory
import kotlinx.android.synthetic.main.fragment_sub_add_assets.*

private const val PRIMARY_ACCOUNT_TYPE = "PrimaryAccountType"

class SubAddAssetsFragment : Fragment() {

    companion object {
        fun newInstance(primaryAccountType: PrimaryAccountType) =
                SubAddAssetsFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(PRIMARY_ACCOUNT_TYPE, primaryAccountType)
                    }
                }
    }

    private lateinit var viewModel: SubAddAssetsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sub_add_assets, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = AddAccountAdapter(R.layout.item_add_asset, mutableListOf())
        adapter.setOnItemClickListener { _, _, position ->
            val accountType = viewModel.accountTypeList.value?.get(position)
            if (accountType != null) {
                if (accountType.finalType) {
                    // 添加账户
                    val newAccount = AccountView(null, accountType.baseAccountType, accountType.primaryAccountType,
                            accountType.mainAccountType, accountType.name, accountType.icon, accountType.autoImport, accountType.custom, accountType.hint,
                            accountType.accountTypeId, 0L, accountType.name, true, 0L, 0.0, "")
                    val action = AddAssetsFragmentDirections.addAssetAction(newAccount)
                    findNavController().navigate(action)
                } else {
                    // 转到二级视图
                    val action = AddAssetsFragmentDirections.showSecondViewAction(accountType)
                    findNavController().navigate(action)
                }
            }

        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setAdapter(adapter)
        }
        viewModel.accountTypeList.observe(viewLifecycleOwner, {
            adapter.setList(it)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val primaryAccountType = it.getParcelable<PrimaryAccountType>(PRIMARY_ACCOUNT_TYPE)!!
            viewModel = ViewModelProvider(this, SubAddAssetsViewModelFactory(Application(), primaryAccountType)).get(SubAddAssetsViewModel::class.java)
        }
    }

}