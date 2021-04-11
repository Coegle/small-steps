package cn.coegle18.smallsteps.fragment

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.Visible
import cn.coegle18.smallsteps.activity.OverviewActivity
import cn.coegle18.smallsteps.adapter.BillByMonthAdapter
import cn.coegle18.smallsteps.adapter.MonthSubNode
import cn.coegle18.smallsteps.dialog.RETURN_KEY
import cn.coegle18.smallsteps.util.Util
import cn.coegle18.smallsteps.viewmodel.AssetDetailViewModel
import cn.coegle18.smallsteps.viewmodel.DetailViewModelFactory
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.internal.button.DialogActionButton
import kotlinx.android.synthetic.main.fragment_asset_detail.*
import kotlinx.android.synthetic.main.item_btn_two.*

const val PASSWORD = "password"
const val USER_ID = "userId"

class AssetDetailFragment : Fragment() {

    private lateinit var viewModel: AssetDetailViewModel
    private val args: AssetDetailFragmentArgs by navArgs()
    private lateinit var pres: SharedPreferences
    private var password: String? = null
    private var userId: String? = null

    fun checkFieldsForEmptyValues(userId: String, password: String, loginBtn: DialogActionButton) {
        loginBtn.isEnabled = !(userId == "" || password == "")
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_asset_detail, container, false)
    }

    @ExperimentalStdlibApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, DetailViewModelFactory(Application(), args.accountInfo.accountId)).get(AssetDetailViewModel::class.java)
        pres = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE)
        password = pres.getString(args.accountInfo.accountId.toString() + PASSWORD, null)
        userId = pres.getString(args.accountInfo.accountId.toString() + USER_ID, null)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(RETURN_KEY)?.observe(viewLifecycleOwner) { _ ->
            showLoginDialog()
        }
        (requireActivity() as OverviewActivity).setActionBarTitle(args.accountInfo.name)
        if (args.accountInfo.autoImport) {
            deleteBtn.text = "自动导入"
        } else {
            deleteBtn.visibility = View.GONE
        }
        okBtn.apply {
            text = "记一笔"
            setOnClickListener {
                val action = AssetDetailFragmentDirections.editBillActionzFromAsset(null, args.accountInfo.accountId)
                findNavController().navigate(action)
            }
        }
        deleteBtn.apply {
            text = "自动导入"

            setOnClickListener {
                if (userId == null || password == null) {
                    showLoginDialog()
                } else {
                    val action = AssetDetailFragmentDirections.goToLoadingAction(userId!!, password!!, args.accountInfo.accountId)
                    findNavController().navigate(action)
                }
            }
        }

        val mAdapter = BillByMonthAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        val headerCard = layoutInflater.inflate(R.layout.item_card_simple, recyclerView, false)
        mAdapter.addHeaderView(headerCard)
        val balanceText = headerCard.findViewById<TextView>(R.id.balanceText)
        viewModel.displayDataList.observe(viewLifecycleOwner) {
            mAdapter.setList(it)
        }
        mAdapter.addChildClickViewIds(R.id.bill_item)
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.bill_item) {
                val bill = (adapter.data[position] as MonthSubNode).node
                if (bill != null && bill.visible != Visible.SYSTEM) {
                    val action = AssetDetailFragmentDirections.editBillActionzFromAsset(
                            bill,
                            args.accountInfo.accountId
                    )
                    findNavController().navigate(action)
                }
            }
        }
        viewModel.accountBalance.observe(viewLifecycleOwner) {
            balanceText.text = Util.balanceFormatter.format(it)
        }
    }

    private fun showLoginDialog() {
        MaterialDialog(requireContext(), BottomSheet()).show {
            customView(R.layout.bottom_sheet_username_password, scrollable = true, horizontalPadding = true)
            val loginBtn = getActionButton(WhichButton.POSITIVE)
            val userIdText = (getCustomView().findViewById(R.id.userIdText) as EditText)
            val passwordText = (getCustomView().findViewById(R.id.passwordText) as EditText)
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(editable: Editable?) {
                    checkFieldsForEmptyValues(userIdText.text.toString(), passwordText.text.toString(), loginBtn)
                }
            }
            userIdText.addTextChangedListener(textWatcher)
            passwordText.addTextChangedListener(textWatcher)
            checkFieldsForEmptyValues(userIdText.text.toString(), passwordText.text.toString(), loginBtn)

            positiveButton(R.string.login) {
                userId = userIdText.text.toString()
                password = passwordText.text.toString()
                pres.edit {
                    putString(args.accountInfo.accountId.toString() + USER_ID, userId)
                    putString(args.accountInfo.accountId.toString() + PASSWORD, password)
                }
                val action = AssetDetailFragmentDirections.goToLoadingAction(userId!!, password!!, args.accountInfo.accountId)
                findNavController().navigate(action)
            }
            negativeButton(R.string.cancel)
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
            title = "确定"
            setIcon(R.drawable.ic_baseline_edit_24)
        }
    }

    // 相应菜单项目点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.rightIcon -> {
                val action = AssetDetailFragmentDirections.editAssetAction(
                    args.accountInfo.accountId,
                    args.accountInfo.accountTypeId
                )
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}