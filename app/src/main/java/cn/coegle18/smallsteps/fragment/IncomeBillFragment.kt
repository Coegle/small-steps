package cn.coegle18.smallsteps.fragment

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import cn.coegle18.smallsteps.*
import cn.coegle18.smallsteps.activity.OverviewActivity
import cn.coegle18.smallsteps.adapter.CategoryGridAdapter
import cn.coegle18.smallsteps.entity.Bill
import cn.coegle18.smallsteps.entity.BillView
import cn.coegle18.smallsteps.entity.CategoryView
import cn.coegle18.smallsteps.util.CurrencyFormatInputFilter
import cn.coegle18.smallsteps.viewmodel.IncomeBillViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.item_account_in_bill.*
import kotlinx.android.synthetic.main.item_btn_two.*
import kotlinx.android.synthetic.main.item_edit_bill_info.*
import kotlinx.android.synthetic.main.layout_edit_bill_income.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.thread
import kotlin.properties.Delegates

private const val BILL_INFO = "BillInfo"
private const val SUB_INCOME_CATEGORY = "subIncomeCategory"
private const val ACCOUNT_ID = "account_id"
private const val ACCOUNT = "account"
private const val RELATED_ACCOUNT = "related_account"

class IncomeBillFragment : Fragment() {

    private var billInfoArg: BillView? = null
    private var accountIdArg by Delegates.notNull<Long>()

    private lateinit var viewModel: IncomeBillViewModel

    companion object {
        fun newInstance(billInfo: BillView?, accountId: Long) =
                IncomeBillFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(BILL_INFO, billInfo)
                        putLong(ACCOUNT_ID, accountId)
                    }
                }
    }

    // 分类相关的设置
    private fun setCategoryView() {
        val mAdapter = CategoryGridAdapter()
        incomeCategoryRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = mAdapter
        }

        // 设置重新设置列表
        viewModel.displayList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                mAdapter.setList(it)
            }
        }
        viewModel.categoryView.observe(viewLifecycleOwner) {
            changeRelateAccount(it.relatedAccountType)
        }
        // 设置点击事件的监听
        mAdapter.setOnItemClickListener { _, _, position ->
            val data = viewModel.categoryList.value?.get(position)!!

            if (billInfoArg?.refundFlag != null || billInfoArg?.reimbursementFlag != null) {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("更改分类需要先取消关联的账单")
                        .setPositiveButton("确定") { _, _ -> }
                        .show()
            } else {
                // 设置新的选中的分类 ID
                viewModel.selectedCategoryId.value = data.id

                // 如果有子分类，导航至子分类的 BottomSheet
                if (data.subCategoryNum != 0L) {
                    val action = EditBillFragmentDirections.selectSubCategoryAction(data)
                    findNavController().navigate(action)
                }
            }
        }

        // BottomSheet 返回的数据
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<CategoryView>(SUB_INCOME_CATEGORY)?.observe(viewLifecycleOwner) { result ->
            viewModel.selectedCategoryId.value = result.id
        }
    }

    // 账户相关设置
    private fun setAccountCard() {
        balanceText.filters = arrayOf<InputFilter>(CurrencyFormatInputFilter())

        viewModel.accountView.observe(viewLifecycleOwner) {
            accountNameText.text = it.name
            val icon = resources.getIdentifier("ic_fund_${it.icon}_white", "drawable", requireContext().packageName)
            accountIcon.setImageResource(icon)
            accountCaptionText.text = it.mainAccountType.caption
        }

        accountInfoLayout.setOnClickListener {
            val array = arrayOf(MainAccountType.ALI_PAY, MainAccountType.CASH, MainAccountType.DEPOSIT_CARD,
                    MainAccountType.WECHAT_PAY, MainAccountType.SCHOOL_CARD, MainAccountType.CUSTOM, MainAccountType.CREDIT_CARD)
            val action = EditBillFragmentDirections.selectAccountAction(array, ACCOUNT)
            findNavController().navigate(action)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Long>(ACCOUNT)?.observe(viewLifecycleOwner) { result ->
            Log.d("data", "inAccount: $result")
            viewModel.accountId.value = result
        }
    }

    // 时间相关设置
    private fun setDateCard() {
        val formatString = "yyyy年MM月dd日"
        // 设置按钮显示的时间
        dateBtn.text =
                if (billInfoArg != null) billInfoArg!!.date.format(DateTimeFormatter.ofPattern(formatString))
                else viewModel.newDateTime.format(DateTimeFormatter.ofPattern(formatString))
        // 设置时间 Chip 点击事件响应
        dateBtn.setOnClickListener {
            // 生成 datePicker
            val builder = MaterialDatePicker.Builder.datePicker().also {
                // 只能选择今天以及之前的日期
                val dateConstraints = DateValidatorPointBackward.now()
                it.setCalendarConstraints(CalendarConstraints.Builder()
                        .setValidator(dateConstraints)
                        .build())
                it.setTitleText("Pick Date")
                // 设置初始时间
                it.setSelection(
                        if (billInfoArg != null) billInfoArg!!.date.toInstant().toEpochMilli()
                        else OffsetDateTime.now().toInstant().toEpochMilli()
                )
            }
            val datePicker = builder.build()
            // 设置确定按钮点击事件监听
            datePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance()
                calendar.time = Date(it)
                val now = OffsetDateTime.now()
                // 设置时、分、秒，否则 it 默认为早上 8：00
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, now.hour)
                    set(Calendar.MINUTE, now.minute)
                    set(Calendar.SECOND, now.second)
                }
                viewModel.newDateTime = OffsetDateTime.ofInstant(calendar.toInstant(), TimeZone.getDefault().toZoneId())
                dateBtn.text = viewModel.newDateTime.format(DateTimeFormatter.ofPattern(formatString))
            }
            datePicker.show(parentFragmentManager, "datePicker")
        }
    }

    // 备注相关设置
    private fun setRemarkCard() {
        if (billInfoArg != null) {
            remarkText.setText(billInfoArg!!.remark)
        }
    }

    // 关联账户设置
    private fun changeRelateAccount(relatedAccountType: MainAccountType?) {
        if (relatedAccountType == null) {
            relateAccountBtn.visibility = View.GONE
        }
        // 可关联账单
        else if (relatedAccountType == MainAccountType.REFUND || relatedAccountType == MainAccountType.REIMBURSEMENT) {
            relateAccountBtn.visibility = View.VISIBLE
            if (billInfoArg?.outAccountMainAccountType != null && viewModel.relatedAccountId.value == billInfoArg?.outAccountId) { // 旧账单刚进来
                Log.d("action", "旧的退款账单")
                // todo 显示关联的账单
            } else {
                Log.d("action", "关联退款账单")
                relateAccountBtn.text = "选择退款/报销的订单"
                // todo 选择需要关联的账单
            }
        }
        // 可关联账户
        else {
            relateAccountBtn.visibility = View.VISIBLE
            if (billInfoArg?.outAccountMainAccountType == relatedAccountType && viewModel.relatedAccountId.value == billInfoArg?.outAccountId) { // 旧账单刚进来
                Log.d("action", "旧帐单，关联账户")
            } else {
                Log.d("action", "切换到该可关联账单的分类")
                viewModel.relatedAccountId.value = Constants.defaultAccountMap[relatedAccountType]
            }
            relateAccountBtn.setOnClickListener {
                val accountTypeArray = arrayOf(relatedAccountType)
                val action = EditBillFragmentDirections.selectAccountAction(accountTypeArray, RELATED_ACCOUNT)
                findNavController().navigate(action)
            }
        }
    }

    private fun setRelateAccount() {
        // 接收关联账户 Dialog 的返回
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Long>(RELATED_ACCOUNT)?.observe(viewLifecycleOwner) { result ->
            Log.d("data", "outAccount: $result")
            viewModel.relatedAccountId.value = result
        }
        viewModel.relatedAccount.observe(viewLifecycleOwner) {
            if (it != null) {
                relateAccountBtn.text = it.name
            }
        }
    }

    private fun collectData(): Bill? {
        val balance = balanceText.text.toString().toDoubleOrNull()
        if (balance != null && balance != 0.0) {
            val newBill = Bill(
                viewModel.newDateTime,
                viewModel.categoryView.value?.id!!,
                null,
                remarkText.text.toString(),
                null,
                null,
                balance,
                viewModel.accountId.value,
                0.0,
                balance,
                Source.MANUAL,
                Visible.ENABLED
            )
            if (relateAccountBtn.visibility == View.VISIBLE) {
                Log.d("data", viewModel.relatedAccount.value.toString())
                newBill.apply {
                    outAccount = viewModel.relatedAccountId.value
                    outMoney = newBill.inMoney
                    income = 0.0
                }
            }
            Log.d("data", newBill.toString())
            return newBill
        } else {
            Toast.makeText(context, "金额为空", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    // 按钮设置
    private fun setBtn() {
        if (billInfoArg != null) {
            okBtn.text = "保存"
            deleteBtn.text = "删除"
        } else {
            okBtn.text = "再记一笔"
            deleteBtn.text = "保存"
        }
        // 再记一笔或者保存修改
        okBtn.setOnClickListener {
            // todo: 分类没有设置和账户没有设置的问题
            val bill = collectData()
            if (bill != null) {
                if (billInfoArg != null) { // 修改
                    bill.billId = billInfoArg!!.billId
                    thread {
                        viewModel.billDao.updateBill(bill)
                        setNewBalance(
                            billInfoArg?.outMoney,
                            bill.outMoney,
                            billInfoArg?.inMoney,
                            bill.inMoney,
                            bill.outAccount,
                            bill.inAccount
                        )
                    }
                    findNavController().navigateUp()
                } else { // 再记一笔
                    balanceText.setText("")
                    (requireActivity() as OverviewActivity).vibratePhone()
                    Toast.makeText(requireContext(), "添加成功", Toast.LENGTH_SHORT).show()
                    thread {
                        viewModel.billDao.insertBill(bill)
                        setNewBalance(
                            null,
                            bill.outMoney,
                            null,
                            bill.inMoney,
                            bill.outAccount,
                            bill.inAccount
                        )
                    }
                }
            }
        }
        // 删除或者保存
        deleteBtn.setOnClickListener {
            if (billInfoArg != null) { // 删除
                thread {
                    viewModel.billDao.deleteBill(billInfoArg!!.billId)
                    setNewBalance(
                        billInfoArg?.outMoney,
                        0.00,
                        billInfoArg?.inMoney,
                        0.00,
                        billInfoArg?.outAccountId,
                        billInfoArg?.inAccountId
                    )
                }
                findNavController().navigateUp()
            } else { // 保存
                val bill = collectData()
                if (bill != null) {
                    thread {
                        viewModel.billDao.insertBill(bill)
                        setNewBalance(
                            null,
                            bill.outMoney,
                            null,
                            bill.inMoney,
                            bill.outAccount,
                            bill.inAccount
                        )
                    }
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_edit_bill_income, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            billInfoArg = it.getParcelable(BILL_INFO)
            accountIdArg = it.getLong(ACCOUNT_ID)
        }
    }

    private fun init() {
        if (billInfoArg != null) {
            balanceText.setText(billInfoArg!!.inMoney.toString())
            if (billInfoArg!!.outAccountMainAccountType != null && billInfoArg!!.outAccountMainAccountType == MainAccountType.REFUND) {
                Log.d("action", "设置退款关联账单")
                // todo 关联账单
            } else if (billInfoArg!!.outAccountMainAccountType != null && billInfoArg!!.outAccountMainAccountType != MainAccountType.REFUND) {
                Log.d("action", "设置其他关联账户")
                relateAccountBtn.visibility = View.VISIBLE
                viewModel.relatedAccountId.value = billInfoArg!!.outAccountId!!
            }
            viewModel.newDateTime = billInfoArg!!.date
            viewModel.accountId.value = billInfoArg!!.inAccountId!!
            viewModel.selectedCategoryId.value = billInfoArg!!.categoryCId
                    ?: billInfoArg!!.categoryPId
        } else {
            viewModel.accountId.value = accountIdArg
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(IncomeBillViewModel::class.java)
        init()
        setCategoryView()
        setAccountCard()
        setRelateAccount()
        setDateCard()
        setRemarkCard()
        setBtn()

    }

    private fun setNewBalance(
        oldOutMoney: Double?,
        newOutMoney: Double?,
        oldInMoney: Double?,
        newInMoney: Double?,
        outAccountId: Long?,
        inAccountId: Long?
    ) {
        val inDifference = (newInMoney ?: 0.00) - (oldInMoney ?: 0.00)
        val outDifference = (newOutMoney ?: 0.00) - (oldOutMoney ?: 0.00)
        if (inDifference != 0.00) inAccountId?.let {
            viewModel.accountDao.updateAccountBalance(
                it,
                inDifference
            )
        }
        if (outDifference != 0.00) outAccountId?.let {
            viewModel.accountDao.updateAccountBalance(
                it,
                -outDifference
            )
        }
    }
}