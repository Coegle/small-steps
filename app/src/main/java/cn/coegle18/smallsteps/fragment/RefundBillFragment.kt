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
import cn.coegle18.smallsteps.adapter.BillAdapter
import cn.coegle18.smallsteps.entity.BillView
import cn.coegle18.smallsteps.viewmodel.RefundBillViewModel
import cn.coegle18.smallsteps.viewmodel.RefundBillViewModelFactory
import kotlinx.android.synthetic.main.fragment_refund_bill.*

const val REFUND_BILL = "refundBill"

class RefundBillFragment : Fragment() {

    private lateinit var viewModel: RefundBillViewModel
    private val args: RefundBillFragmentArgs by navArgs()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_refund_bill, container, false)
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
            val data = adapter.data[position] as BillView
            findNavController().previousBackStackEntry?.savedStateHandle?.set(REFUND_BILL, data)
            findNavController().navigateUp()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, RefundBillViewModelFactory(Application(), args.balance)).get(RefundBillViewModel::class.java)
        setList()
    }
}