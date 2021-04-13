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
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.adapter.BillAdapter
import cn.coegle18.smallsteps.entity.BillView
import cn.coegle18.smallsteps.viewmodel.PieChartDetailViewModel
import cn.coegle18.smallsteps.viewmodel.PieChartDetailViewModelFactory
import kotlinx.android.synthetic.main.fragment_pie_chart.*
import kotlinx.android.synthetic.main.toggle_group_two.*

class PieChartDetailFragment : Fragment() {

    val args: PieChartDetailFragmentArgs by navArgs()
    private lateinit var viewModel: PieChartDetailViewModel

    private fun setToggleBtn() {
        middleBtn.visibility = View.GONE
        leftBtn.text = "按日期"
        rightBtn.text = "按金额"
        toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (checkedId == R.id.leftBtn) viewModel.orderByDate.value = isChecked
        }
        toggleButton.apply {
            isSingleSelection = true
            isSelectionRequired = true
            check(R.id.leftBtn)
        }
    }

    private fun setList() {
        val mAdapter = BillAdapter(R.layout.item_bill)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mAdapter
        }

        viewModel.billList.observe(viewLifecycleOwner) {
            mAdapter.setList(it)
        }
        mAdapter.setOnItemClickListener { adapter, _, position ->
            val data = adapter.getItem(position) as BillView
            val accountId =
                    if (data.displayTradeType == TradeType.INCOME) data.inAccountId else data.outAccountId
            val action = PieChartDetailFragmentDirections.editBillActionFromCategory(data, accountId!!)
            findNavController().navigate(action)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pie_chart_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, PieChartDetailViewModelFactory(Application(), args.categoryId)).get(PieChartDetailViewModel::class.java)
        setToggleBtn()
        setList()
    }
}