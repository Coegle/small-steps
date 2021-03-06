package cn.coegle18.smallsteps.fragment

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import cn.coegle18.smallsteps.util.Util
import cn.coegle18.smallsteps.viewmodel.IncomeBillViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.item_account_in_bill.*
import kotlinx.android.synthetic.main.item_bill.*
import kotlinx.android.synthetic.main.item_btn_two.*
import kotlinx.android.synthetic.main.item_edit_bill_info.*
import kotlinx.android.synthetic.main.item_edit_bill_info.remarkText
import kotlinx.android.synthetic.main.item_related_bill_card.*
import kotlinx.android.synthetic.main.layout_edit_bill_income.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
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

    // ?????????????????????
    private fun setCategoryView() {
        val mAdapter = CategoryGridAdapter()
        incomeCategoryRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 5)
            adapter = mAdapter
        }

        // ????????????????????????
        viewModel.displayList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                mAdapter.setList(it)
            }
        }
        viewModel.categoryView.observe(viewLifecycleOwner) {
            changeRelateAccount(it.relatedAccountType)
        }
        // ???????????????????????????
        mAdapter.setOnItemClickListener { _, _, position ->
            val data = viewModel.categoryList.value?.get(position)!!

            if (viewModel.newRelatedBillId.value != 0L) {
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
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<CategoryView>(SUB_INCOME_CATEGORY)?.observe(viewLifecycleOwner) { result ->
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
    private fun drawRelatedBillCard(data: BillView) {
        relatedBillCardInclude.visibility = View.VISIBLE
        relateAccountBtn.visibility = View.GONE
        val categoryName = data.categoryPName + if (data.categoryCName != null) {
            " - ${data.categoryCName}"
        } else {
            ""
        }
        categoryText.text = categoryName
        categoryImage.setImageResource(
                resources.getIdentifier(
                        "ic_category_${data.tradeType.name.toLowerCase(Locale.ROOT)}_${
                            if (data.categoryCId != null) {
                                data.categoryCIcon
                            } else {
                                data.categoryPIcon
                            }
                        }", "drawable", requireContext().packageName
                ))

        dateText.text = "${data.date.monthValue}???${data.date.dayOfMonth}???"
        val relatedBillRemark = relatedBillCardInclude.findViewById<View>(R.id.include).findViewById<TextView>(R.id.remarkText)
        relatedBillRemark.text = data.remark
        splitText.visibility = View.GONE
        refundText.visibility = View.GONE
        reimburseText.visibility = View.GONE
        moneyText.text = "???${Util.balanceFormatter.format(data.outMoney)}"
        moneyText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        categoryImage.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_bg_red)
        accountText.text = data.outAccountName
    }

    // ??????????????????
    private fun setRelatedBillCard() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<BillView>(REFUND_BILL)?.observe(viewLifecycleOwner) { result ->
            Log.d("data", "relatedBill: $result")
            viewModel.newRelatedBillId.value = result.billId
        }
        viewModel.relatedBillView.observe(viewLifecycleOwner) {
            //Log.d("relatedBill", it.toString())
            if (it != null) {
                drawRelatedBillCard(it)
            } else {
                relateAccountBtn.visibility = View.VISIBLE
                relatedBillCardInclude.visibility = View.GONE
            }
        }
    }

    // ??????????????????
    private fun changeRelateAccount(relatedAccountType: MainAccountType?) {
        if (relatedAccountType == null) {
            relateAccountBtn.visibility = View.GONE
        }
        // ???????????????
        else if (relatedAccountType == MainAccountType.REFUND || relatedAccountType == MainAccountType.REIMBURSEMENT) {
            viewModel.relatedAccountId.value = Constants.defaultAccountMap[relatedAccountType]
            relateAccountBtn.visibility = View.VISIBLE
            reselectBtn.setOnClickListener {
                val balance = balanceText.text.toString().toDoubleOrNull()
                if (balance != null && balance != 0.0) {
                    Log.d("Click", Util.balanceFormatter.format(balance))
                    Log.d("Click", relatedAccountType.toString())
                    val action = EditBillFragmentDirections.attachRefundBillAction(Util.balanceFormatter.format(balance), relatedAccountType)
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(requireContext(), "??????????????????", Toast.LENGTH_SHORT).show()
                }
            }
            disconnectBtn.setOnClickListener {
                viewModel.newRelatedBillId.value = 0L
            }
            if (billInfoArg?.outAccountMainAccountType != null && viewModel.relatedAccountId.value == billInfoArg?.outAccountId) { // ??????????????????
                Log.d("action", "??????????????????")
                viewModel.mainBillId.value = billInfoArg!!.billId
                viewModel.oldRelatedBill.observe(viewLifecycleOwner) {
                    if (it != null) {
                        Log.d("oldRelatedBill", it.toString())
                        viewModel.newRelatedBillId.value = it.billId
                    }
                    viewModel.oldRelatedBill.removeObservers(viewLifecycleOwner)
                }
                relateAccountBtn.setOnClickListener {
                    val balance = balanceText.text.toString().toDoubleOrNull()
                    if (balance != null && balance != 0.0) {

                        Log.d("Click", Util.balanceFormatter.format(balance))
                        Log.d("Click", relatedAccountType.toString())
                        val action = EditBillFragmentDirections.attachRefundBillAction(Util.balanceFormatter.format(balance), relatedAccountType)
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(requireContext(), "??????????????????", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("action", "??????????????????")
                relateAccountBtn.setOnClickListener {
                    val balance = balanceText.text.toString().toDoubleOrNull()
                    if (balance != null && balance != 0.0) {

                        Log.d("Click", Util.balanceFormatter.format(balance))
                        Log.d("Click", relatedAccountType.toString())
                        val action = EditBillFragmentDirections.attachRefundBillAction(Util.balanceFormatter.format(balance), relatedAccountType)
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(requireContext(), "??????????????????", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        // ???????????????
        else {
            relateAccountBtn.visibility = View.VISIBLE
            if (billInfoArg?.outAccountMainAccountType == relatedAccountType && viewModel.relatedAccountId.value == billInfoArg?.outAccountId) { // ??????????????????
                Log.d("action", "????????????????????????")
            } else {
                Log.d("action", "????????????????????????????????????")
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
        // ?????????????????? Dialog ?????????
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Long>(RELATED_ACCOUNT)?.observe(viewLifecycleOwner) { result ->
            Log.d("data", "outAccount: $result")
            viewModel.relatedAccountId.value = result
        }
        viewModel.relatedAccount.observe(viewLifecycleOwner) {
            if (it != null) {
                val relatedAccountType = viewModel.categoryView.value?.relatedAccountType
                if (relatedAccountType == MainAccountType.REFUND || relatedAccountType == MainAccountType.REIMBURSEMENT) {
                    relateAccountBtn.text = "????????????/???????????????"
                } else {
                    relateAccountBtn.text = it.name
                }
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
            val relatedAccountType = viewModel.categoryView.value?.relatedAccountType
            if (relatedAccountType != null) {
                Log.d("data, relatedAccount", viewModel.relatedAccount.value.toString())
                newBill.apply {
                    outAccount = viewModel.relatedAccountId.value
                    outMoney = newBill.inMoney
                    income = 0.0
                }
            }
            viewModel.relatedBillView.value?.let { relatedBillView ->
                viewModel.categoryView.value?.let {
                    Log.d("related", "?????????????????????$relatedBillView")
                    newBill.outMoney = newBill.inMoney
                    newBill.income = 0.0
                    when (it.relatedAccountType) {
                        MainAccountType.REIMBURSEMENT -> {
                            newBill.outAccount = relatedBillView.inAccountId
                        }
                        MainAccountType.REFUND -> {
                            newBill.outAccount = Constants.defaultAccountMap[MainAccountType.REFUND]
                        }
                    }
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
                    viewModel.modifyBill(bill, billInfoArg!!)
                    findNavController().navigateUp()
                } else { // ????????????
                    balanceText.setText("")
                    (requireActivity() as OverviewActivity).vibratePhone()
                    Toast.makeText(requireContext(), "????????????", Toast.LENGTH_SHORT).show()
                    viewModel.addBill(bill)
                }
            }
        }
        // ??????????????????
        deleteBtn.setOnClickListener {
            if (billInfoArg != null) { // ??????
                if (viewModel.relatedBillView.value == null) {
                    viewModel.deleteBill(billInfoArg!!.toBill())
                    findNavController().navigateUp()
                } else {
                    MaterialAlertDialogBuilder(requireContext())
                            .setTitle("??????????????????????????????????????????")
                            .setPositiveButton("??????") { _, _ -> }
                            .show()
                }
            } else { // ??????
                val bill = collectData()
                if (bill != null) {
                    viewModel.addBill(bill)
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
                Log.d("action", "????????????????????????")
            } else if (billInfoArg!!.outAccountMainAccountType != null && billInfoArg!!.outAccountMainAccountType != MainAccountType.REFUND) {
                Log.d("action", "????????????????????????")
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
        setRelatedBillCard()
    }
}