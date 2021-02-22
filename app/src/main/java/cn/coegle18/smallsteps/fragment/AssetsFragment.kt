package cn.coegle18.smallsteps.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.adapter.AccountAdapter
import cn.coegle18.smallsteps.entity.AccountList
import cn.coegle18.smallsteps.viewmodel.AssetsViewModel
import kotlinx.android.synthetic.main.fragment_assets.*
import kotlinx.android.synthetic.main.item_headline6.*

class AssetsFragment : Fragment() {

    companion object {
        fun newInstance() = AssetsFragment()
    }

    private lateinit var viewModel: AssetsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_assets, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AssetsViewModel::class.java)

        val mAdapter = AccountAdapter(R.layout.item_account, R.layout.header_view, mutableListOf())
        accountRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mAdapter
        }

        mAdapter.setOnItemClickListener { adapter, view, position ->
            val accountSection = viewModel.accountSection[position]
            if (!accountSection.isHeader) {
                val action =
                    AssetsFragmentDirections.showAssetDetailAction(accountSection.data as AccountList)
                findNavController().navigate(action)
            }
        }

        // 更新视图
        viewModel.allVisibleAccountList.observe(viewLifecycleOwner, {
            Log.d("livedata", "allAccountsOfAccountType, ${it.toString()}")
            viewModel.group(it)
            mAdapter.setList(viewModel.accountSection)
        })

        // 设置 item_headline6 中的文字
        headlineText.text = "资产明细"
        button.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 向 Activity 告知该 Fragment 参与选项菜单的填充
        setHasOptionsMenu(true)
    }

    // 重新绘制菜单栏
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.assets_fragment_menu, menu)
    }

    // 相应菜单项目点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.rightIcon -> {
                val action = AssetsFragmentDirections.selectNewAssetAction()
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}