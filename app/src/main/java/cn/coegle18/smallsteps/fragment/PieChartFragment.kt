package cn.coegle18.smallsteps.fragment

import android.os.Bundle
import android.view.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.adapter.PieChartAdapter
import cn.coegle18.smallsteps.entity.PieChartData
import cn.coegle18.smallsteps.viewmodel.PieChartViewModel
import kotlinx.android.synthetic.main.fragment_pie_chart.*


class PieChartFragment : Fragment() {

    private lateinit var viewModel: PieChartViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pie_chart, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PieChartViewModel::class.java)
        val mAdapter = PieChartAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mAdapter
        }
        viewModel.pieChartDataList.observe(viewLifecycleOwner) {
            mAdapter.setList(it)
        }
        mAdapter.setOnItemClickListener { adapter, _, position ->
            val action = PieChartFragmentDirections.showPieChartDetailAction((adapter.getItem(position) as PieChartData).id)
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
            title = "账单"
            setIcon(R.drawable.ic_baseline_equalizer_24)
        }
    }

    // 相应菜单项目点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.rightIcon -> {
                val action = PieChartFragmentDirections.showBillsAction()
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}