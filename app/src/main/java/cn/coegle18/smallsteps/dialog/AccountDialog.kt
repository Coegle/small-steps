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
import cn.coegle18.smallsteps.adapter.AccountListAdapter
import cn.coegle18.smallsteps.entity.AccountView
import cn.coegle18.smallsteps.viewmodel.AccountDialogViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_sheet.*

//private const val ACCOUNT = "account"
//private const val RELATED_ACCOUNT = "relatedAccount"
//private const val IN_ACCOUNT = "inAccount"
//private const val OUT_ACCOUNT = "outAccount"

class AccountDialog : BottomSheetDialogFragment() {
    lateinit var viewModel: AccountDialogViewModel
    private val args: AccountDialogArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bottom_sheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AccountDialogViewModel::class.java)
        val mAdapter = AccountListAdapter()
        viewModel.accountType.value = args.accountType.asList()
        viewModel.accountList.observe(viewLifecycleOwner) {
            mAdapter.setList(it)
        }
        sheetRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        mAdapter.setOnItemClickListener { adapter, _, position ->
            val data = (adapter.getItem(position) as AccountView).accountId
            findNavController().previousBackStackEntry?.savedStateHandle?.set(args.source, data)
            dismiss()
        }
    }
}