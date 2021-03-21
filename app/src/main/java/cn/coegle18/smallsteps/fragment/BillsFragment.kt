package cn.coegle18.smallsteps.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.adapter.BillAdapter
import cn.coegle18.smallsteps.entity.BillView
import cn.coegle18.smallsteps.viewmodel.BillsViewModel
import kotlinx.android.synthetic.main.fragment_bills.*


class BillsFragment : Fragment() {

    private lateinit var viewModel: BillsViewModel
    private val mAdapter = BillAdapter(R.layout.item_bill)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bills, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BillsViewModel::class.java)
        billsRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mAdapter
        }
        viewModel.billList.observe(viewLifecycleOwner, {
            mAdapter.setList(it)
        })
        mAdapter.setOnItemClickListener { adapter, _, position ->
            val data = adapter.getItem(position) as BillView
            Log.d("data", adapter.getItem(position).toString())
            val accountId =
                if (data.displayTradeType == TradeType.INCOME) data.inAccountId else data.outAccountId
            val action = BillsFragmentDirections.editBillActionFromBills(data, accountId!!)
            findNavController().navigate(action)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 向 Activity 告知该 Fragment 参与选项菜单的填充
        setHasOptionsMenu(true)
    }

    // 重新绘制菜单栏
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.assets_fragment_menu, menu)
        menu[0].apply {
            title = "报表"
            setIcon(R.drawable.ic_baseline_pie_chart_24)
        }
    }

    // 相应菜单项目点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.rightIcon -> {
                val action = BillsFragmentDirections.showPieChartAction()
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}