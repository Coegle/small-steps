package cn.coegle18.smallsteps.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.adapter.CategoryListAdapter
import cn.coegle18.smallsteps.viewmodel.SubCategoryDialogViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_sheet.*

private const val SUB_INCOME_CATEGORY = "subIncomeCategory"
private const val SUB_EXPENSE_CATEGORY = "subExpenseCategory"

class SubCategoryDialog : BottomSheetDialogFragment() {
    lateinit var viewModel: SubCategoryDialogViewModel
    private val args: SubCategoryDialogArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bottom_sheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SubCategoryDialogViewModel::class.java)
        val mAdapter = CategoryListAdapter()
        viewModel.pCategoryView.value = args.pCategory
        viewModel.subCategoryList.observe(viewLifecycleOwner) {
            mAdapter.setList(it)
        }
        sheetRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        mAdapter.setOnItemClickListener { adapter, _, position ->
            if (args.pCategory.tradeType == TradeType.EXPENSE) {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(SUB_EXPENSE_CATEGORY, adapter.getItem(position))
            } else if (args.pCategory.tradeType == TradeType.INCOME) {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(SUB_INCOME_CATEGORY, adapter.getItem(position))
            }
            dismiss()
        }
    }
}