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
import cn.coegle18.smallsteps.viewmodel.ExpenseBillViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.item_account_in_bill.*
import kotlinx.android.synthetic.main.item_btn_two.*
import kotlinx.android.synthetic.main.item_chips_two.*
import kotlinx.android.synthetic.main.item_edit_bill_info.*
import kotlinx.android.synthetic.main.layout_edit_bill_expense.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.thread
import kotlin.properties.Delegates


private const val BILL_INFO = "BillInfo"
private const val SUB_EXPENSE_CATEGORY = "subExpenseCategory"
private const val ACCOUNT_ID = "account_id"
private const val ACCOUNT = "account"
private const val RELATED_ACCOUNT = "relatedAccount"

class ExpenseBillFragment : Fragment() {

    private var billInfoArg: BillView? = null
    private var accountIdArg by Delegates.notNull<Long>()

    private lateinit var viewModel: ExpenseBillViewModel

    companion object {
        fun newInstance(billInfo: BillView?, accountId: Long) =
                ExpenseBillFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(BILL_INFO, billInfo)
                        putLong(ACCOUNT_ID, accountId)
                    }
                }
    }

    // ?????????????????????
    private fun setCategoryView() {
        val mAdapter = CategoryGridAdapter()
        expenseCategoryRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = mAdapter
        }

        // ????????????????????????
        viewModel.displayList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                //Log.d("data", it.toString())
                mAdapter.setList(it)
            }
        }
        viewModel.categoryView.observe(viewLifecycleOwner) {
            changeRelateAccount(it.tradeType, it.relatedAccountType)
        }
        // ???????????????????????????
        mAdapter.setOnItemClickListener { _, _, position ->
            val data = viewModel.categoryList.value?.get(position)!!

            if (data.tradeType != TradeType.EXPENSE && (billInfoArg?.reimbursementFlag != null || billInfoArg?.refundFlag != null)) {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("??????????????????????????????????????????")
                        .setPositiveButton("??????") { _, _ -> }
                        .show()
            } else {
                // ??????????????????????????? ID
                viewModel.selectedCategoryId.value = data.id

                // ?????????????????????????????????????????? BottomSheet
                if (data.subCategoryNum != 0L) {
                    val action = EditBillFragmentDirections.selectSubCategoryAction(data)
                    findNavController().navigate(action)
                }
            }

        }

        // BottomSheet ???????????????
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<CategoryView>(SUB_EXPENSE_CATEGORY)?.observe(viewLifecycleOwner) { result ->
            viewModel.selectedCategoryId.value = result.id
        }
    }

    // ??????????????????
    private fun setAccountCard() {
        balanceText.filters = arrayOf<InputFilter>(CurrencyFormatInputFilter())

        viewModel.accountView.observe(viewLifecycleOwner) {
            accountNameText.text = it.name
            val icon = resources.getIdentifier("ic_fund_${it.icon}_white", "drawable", requireContext().packageName)
            accountIcon.setImageResource(icon)
            accountCaptionText.text = it.mainAccountType.caption
        }

        accountInfoLayout.setOnClickListener {
            val array = arrayOf(
                MainAccountType.ALI_PAY,
                MainAccountType.CASH,
                MainAccountType.DEPOSIT_CARD,
                MainAccountType.WECHAT_PAY,
                MainAccountType.SCHOOL_CARD,
                MainAccountType.CUSTOM,
                MainAccountType.CREDIT_CARD
            )
            val action = EditBillFragmentDirections.selectAccountAction(array, ACCOUNT)
            findNavController().navigate(action)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Long>(ACCOUNT)?.observe(viewLifecycleOwner) { result ->
            Log.d("data", "outAccount: $result")
            viewModel.accountId.value = result
        }
    }

    // ??????????????????
    private fun setDateCard() {
        val formatString = "yyyy???MM???dd???"
        // ???????????????????????????
        dateBtn.text =
                if (billInfoArg != null) billInfoArg!!.date.format(DateTimeFormatter.ofPattern(formatString))
                else viewModel.newDateTime.format(DateTimeFormatter.ofPattern(formatString))
        // ???????????? Chip ??????????????????
        dateBtn.setOnClickListener {
            // ?????? datePicker
            val builder = MaterialDatePicker.Builder.datePicker().also {
                // ???????????????????????????????????????
                val dateConstraints = DateValidatorPointBackward.now()
                it.setCalendarConstraints(CalendarConstraints.Builder()
                        .setValidator(dateConstraints)
                        .build())
                it.setTitleText("Pick Date")
                // ??????????????????
                it.setSelection(
                        if (billInfoArg != null) billInfoArg!!.date.toInstant().toEpochMilli()
                        else OffsetDateTime.now().toInstant().toEpochMilli()
                )
            }
            val datePicker = builder.build()
            // ????????????????????????????????????
            datePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance()
                calendar.time = Date(it)
                val now = OffsetDateTime.now()
                // ?????????????????????????????? it ??????????????? 8???00
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

    // ??????????????????
    private fun setRemarkCard() {
        if (billInfoArg != null) {
            remarkText.setText(billInfoArg!!.remark)
        }
    }

    // ??????????????????
    private fun changeRelateAccount(tradeType: TradeType, relatedAccountType: MainAccountType?) {
        // ?????????
        if (tradeType == TradeType.EXPENSE) {
            if (billInfoArg?.inAccountMainAccountType == MainAccountType.REIMBURSEMENT && viewModel.relatedAccountId.value == billInfoArg?.inAccountId && relateBtn.isChecked) { // ?????????????????????????????????????????????
                Log.d("action", "?????????????????????")
                relateBtn.visibility = View.VISIBLE
                relateAccountBtn.visibility = View.VISIBLE
            } else if (relateBtn.isChecked && viewModel.relatedAccount.value?.mainAccountType == MainAccountType.REIMBURSEMENT) {
                Log.d("action", "???????????????????????????")
            } else {
                Log.d("action", "??????")
                viewModel.relatedAccountId.value = Constants.defaultAccountMap[MainAccountType.REIMBURSEMENT]
                relateBtn.isChecked = false
                relateBtn.visibility = View.VISIBLE
                relateAccountBtn.visibility = View.GONE
            }

            relateAccountBtn.setOnClickListener {
                val array = arrayOf(MainAccountType.REIMBURSEMENT)
                val action = EditBillFragmentDirections.selectAccountAction(array, RELATED_ACCOUNT)
                findNavController().navigate(action)
            }
        }
        // ??????
        else {
            if (billInfoArg?.inAccountMainAccountType == relatedAccountType && viewModel.relatedAccountId.value == billInfoArg?.inAccountId && relateBtn.isChecked) { // ??????????????????
                Log.d("action", "??????????????????????????????")
                relateBtn.visibility = View.GONE
                relateAccountBtn.visibility = View.VISIBLE
            } else {
                Log.d("action", "???????????????")
                viewModel.relatedAccountId.value =
                    Constants.defaultAccountMap[viewModel.categoryView.value?.relatedAccountType]
                relateBtn.isChecked = true
                relateBtn.visibility = View.GONE
                relateAccountBtn.visibility = View.VISIBLE
            }
            relateAccountBtn.setOnClickListener {
                val accountTypeArray = arrayOf(viewModel.categoryView.value?.relatedAccountType!!)
                val action = EditBillFragmentDirections.selectAccountAction(
                    accountTypeArray,
                    RELATED_ACCOUNT
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun setRelateAccount() {
        relateBtn.text = "?????????"
        // ??????????????????
        relateBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                relateAccountBtn.visibility = View.VISIBLE
            } else {
                relateAccountBtn.visibility = View.GONE
            }
        }
        // ?????????????????? Dialog ?????????
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Long>(RELATED_ACCOUNT)?.observe(viewLifecycleOwner) { result ->
            Log.d("data", "inAccount: $result")
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
                balance,
                viewModel.accountId.value,
                null,
                null,
                balance,
                0.0,
                Source.MANUAL,
                Visible.ENABLED
            )
            if (relateBtn.isChecked) {
                Log.d("data", viewModel.relatedAccount.value.toString())
                newBill.apply {
                    inAccount = viewModel.relatedAccountId.value
                    inMoney = newBill.outMoney
                    expense = 0.0
                    if (viewModel.relatedAccount.value?.mainAccountType == MainAccountType.REIMBURSEMENT) relation = Relation.REIMBURSEMENT
                }
            }
            Log.d("data", newBill.toString())
            return newBill
        } else {
            Toast.makeText(context, "????????????", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    // ????????????
    private fun setBtn() {
        if (billInfoArg != null) {
            okBtn.text = "??????"
            deleteBtn.text = "??????"
        } else {
            okBtn.text = "????????????"
            deleteBtn.text = "??????"
        }
        // ??????????????????????????????
        okBtn.setOnClickListener {
            // todo: ????????????????????????????????????????????????
            val bill = collectData()
            if (bill != null) {
                if (billInfoArg != null) { // ??????
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
                } else { // ????????????
                    balanceText.setText("")
                    (requireActivity() as OverviewActivity).vibratePhone()
                    Toast.makeText(requireContext(), "????????????", Toast.LENGTH_SHORT).show()
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
        // ??????????????????
        deleteBtn.setOnClickListener {
            if (billInfoArg != null) { // ??????
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
            } else { // ??????
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
        return inflater.inflate(R.layout.layout_edit_bill_expense, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            billInfoArg = it.getParcelable(BILL_INFO)
            accountIdArg = it.getLong(ACCOUNT_ID)
        }
    }

    private fun init() {
        if (billInfoArg != null && billInfoArg!!.displayTradeType == TradeType.EXPENSE) {
            balanceText.setText(billInfoArg!!.outMoney.toString())
            if (billInfoArg!!.inAccountMainAccountType != null && billInfoArg!!.inAccountMainAccountType == MainAccountType.REIMBURSEMENT) {
                Log.d("action", "????????????????????????")
                relateBtn.isChecked = true
                relateAccountBtn.visibility = View.VISIBLE
                viewModel.relatedAccountId.value = billInfoArg!!.inAccountId!!
            } else if (billInfoArg!!.inAccountMainAccountType != null && billInfoArg!!.inAccountMainAccountType != MainAccountType.REIMBURSEMENT) {
                Log.d("action", "????????????????????????")
                relateBtn.isChecked = true
                relateBtn.visibility = View.GONE
                relateAccountBtn.visibility = View.VISIBLE
                viewModel.relatedAccountId.value = billInfoArg!!.inAccountId!!
            }
            viewModel.newDateTime = billInfoArg!!.date
            viewModel.accountId.value = billInfoArg!!.outAccountId!!
            viewModel.selectedCategoryId.value = billInfoArg!!.categoryCId
                    ?: billInfoArg!!.categoryPId

        } else {
            viewModel.accountId.value = accountIdArg
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ExpenseBillViewModel::class.java)
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
