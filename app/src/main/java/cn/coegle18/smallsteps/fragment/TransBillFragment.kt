package cn.coegle18.smallsteps.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cn.coegle18.smallsteps.MainAccountType
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.Source
import cn.coegle18.smallsteps.activity.OverviewActivity
import cn.coegle18.smallsteps.entity.Bill
import cn.coegle18.smallsteps.entity.BillView
import cn.coegle18.smallsteps.util.CurrencyFormatInputFilter
import cn.coegle18.smallsteps.util.Util
import cn.coegle18.smallsteps.viewmodel.TransBillViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.item_btn_two.*
import kotlinx.android.synthetic.main.item_edit_bill_info.*
import kotlinx.android.synthetic.main.layout_edit_bill_transfer.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.thread
import kotlin.properties.Delegates


private const val BILL_INFO = "BillInfo"
private const val ACCOUNT_ID = "account_id"
private const val IN_ACCOUNT = "inAccount"
private const val OUT_ACCOUNT = "outAccount"

class TransBillFragment : Fragment() {

    private var billInfoArg: BillView? = null
    private var accountIdArg by Delegates.notNull<Long>()
    lateinit var viewModel: TransBillViewModel
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(editable: Editable?) {
            Log.d("data", editable.toString())
            val balance = editable.toString().toDoubleOrNull()
            viewModel.money.value = balance ?: 0.0
        }
    }

    companion object {
        fun newInstance(billInfo: BillView?, accountId: Long) =
                TransBillFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(BILL_INFO, billInfo)
                        putLong(ACCOUNT_ID, accountId)
                    }
                }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_edit_bill_transfer, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            billInfoArg = it.getParcelable(BILL_INFO)
            accountIdArg = it.getLong(ACCOUNT_ID)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TransBillViewModel::class.java)
        init()
        setDateCard()
        setRemarkCard()
        setBalance()
        setAccount()
        setSwitch()
        setBtn()
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

    // 金额相关设置
    private fun setBalance() {
        val outMoneyText = outAccount.findViewById<TextInputEditText>(R.id.balanceText)
        val inMoneyText = inAccount.findViewById<TextInputEditText>(R.id.balanceText)

        outMoneyText.filters = arrayOf(CurrencyFormatInputFilter())
        inMoneyText.filters = arrayOf(CurrencyFormatInputFilter())

        inMoneyText.addTextChangedListener(textWatcher)
        outMoneyText.addTextChangedListener(textWatcher)

        viewModel.money.observe(viewLifecycleOwner) {
            Log.d("action", "money change $it")
            if (it != 0.0) {
                if (inMoneyText.isFocused) {
                    outMoneyText.removeTextChangedListener(textWatcher)
                    outMoneyText.setText(Util.balanceFormatter.format(it))
                    outMoneyText.addTextChangedListener(textWatcher)
                } else if (outMoneyText.isFocused) {
                    inMoneyText.removeTextChangedListener(textWatcher)
                    inMoneyText.setText(Util.balanceFormatter.format(it))
                    inMoneyText.addTextChangedListener(textWatcher)
                } else if (!outMoneyText.isFocused && !inMoneyText.isFocused) {
                    outMoneyText.removeTextChangedListener(textWatcher)
                    inMoneyText.removeTextChangedListener(textWatcher)
                    inMoneyText.setText(Util.balanceFormatter.format(it))
                    outMoneyText.setText(Util.balanceFormatter.format(it))
                    outMoneyText.addTextChangedListener(textWatcher)
                    inMoneyText.addTextChangedListener(textWatcher)
                }
            } else {
                if (inMoneyText.isFocused) {
                    outMoneyText.removeTextChangedListener(textWatcher)
                    outMoneyText.setText("")
                    outMoneyText.addTextChangedListener(textWatcher)
                }
                if (outMoneyText.isFocused) {
                    inMoneyText.removeTextChangedListener(textWatcher)
                    inMoneyText.setText("")
                    inMoneyText.addTextChangedListener(textWatcher)
                }
            }
        }

    }

    // 账户相关设置
    private fun setAccount() {
        val inAccountLayout = inAccount.findViewById<ConstraintLayout>(R.id.accountInfoLayout)
        val outAccountLayout = outAccount.findViewById<ConstraintLayout>(R.id.accountInfoLayout)

        val inNameText = inAccountLayout.findViewById<TextView>(R.id.accountNameText)
        val outNameText = outAccountLayout.findViewById<TextView>(R.id.accountNameText)

        val inCaptionText = inAccountLayout.findViewById<TextView>(R.id.accountCaptionText)
        val outCaptionText = outAccountLayout.findViewById<TextView>(R.id.accountCaptionText)

        val inAccountIcon = inAccountLayout.findViewById<ImageView>(R.id.accountIcon)
        val outAccountIcon = outAccountLayout.findViewById<ImageView>(R.id.accountIcon)

        viewModel.inAccountView.observe(viewLifecycleOwner) {
            inNameText.text = it.name
            val icon = resources.getIdentifier("ic_fund_${it.icon}_white", "drawable", requireContext().packageName)
            inAccountIcon.setImageResource(icon)
            inCaptionText.text = it.mainAccountType.caption
        }

        viewModel.outAccountView.observe(viewLifecycleOwner) {
            outNameText.text = it.name
            val icon = resources.getIdentifier("ic_fund_${it.icon}_white", "drawable", requireContext().packageName)
            outAccountIcon.setImageResource(icon)
            outCaptionText.text = it.mainAccountType.caption
        }

        inAccountLayout.setOnClickListener {
            val array = arrayOf(MainAccountType.ALI_PAY, MainAccountType.CASH, MainAccountType.DEPOSIT_CARD,
                    MainAccountType.WECHAT_PAY, MainAccountType.SCHOOL_CARD, MainAccountType.CUSTOM, MainAccountType.CREDIT_CARD)
            val action = EditBillFragmentDirections.selectAccountAction(array, IN_ACCOUNT)
            findNavController().navigate(action)
        }
        outAccountLayout.setOnClickListener {
            val array = arrayOf(MainAccountType.ALI_PAY, MainAccountType.CASH, MainAccountType.DEPOSIT_CARD,
                    MainAccountType.WECHAT_PAY, MainAccountType.SCHOOL_CARD, MainAccountType.CUSTOM, MainAccountType.CREDIT_CARD)
            val action = EditBillFragmentDirections.selectAccountAction(array, OUT_ACCOUNT)
            findNavController().navigate(action)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Long>(IN_ACCOUNT)?.observe(viewLifecycleOwner) { result ->
            Log.d("data", "outAccount: $result")
            viewModel.inAccountId.value = result
        }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Long>(OUT_ACCOUNT)?.observe(viewLifecycleOwner) { result ->
            Log.d("data", "outAccount: $result")
            viewModel.outAccountId.value = result
        }
    }

    // 切换账户
    private fun setSwitch() {
        switchBtn.setOnClickListener {
            val temp = viewModel.inAccountId.value
            viewModel.inAccountId.value = viewModel.outAccountId.value
            viewModel.outAccountId.value = temp
        }
    }

    private fun collectData(): Bill? {
        val balance = viewModel.money.value
        if (balance == null || balance == 0.0) {
            Toast.makeText(context, "金额为空", Toast.LENGTH_SHORT).show()
        } else if (viewModel.inAccountId.value == viewModel.outAccountId.value) {
            Toast.makeText(context, "转入和转出的账户不能相等", Toast.LENGTH_SHORT).show()
        } else {
            val newBill = Bill(viewModel.newDateTime, viewModel.categoryId, null, remarkText.text.toString(),
                    balance, viewModel.outAccountId.value, balance, viewModel.inAccountId.value,
                    0.0, 0.0, Source.MANUAL, true)
            Log.d("data", newBill.toString())
            return newBill
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
                if (billInfoArg != null) {
                    bill.billId = billInfoArg!!.billId
                    thread {
                        viewModel.billDao.updateBill(bill)
                    }
                    findNavController().navigateUp()
                } else {
                    val outMoneyText = outAccount.findViewById<TextInputEditText>(R.id.balanceText)
                    outMoneyText.removeTextChangedListener(textWatcher)
                    outMoneyText.setText("")
                    outMoneyText.addTextChangedListener(textWatcher)
                    viewModel.money.value = 0.0
                    thread {
                        viewModel.billDao.insertBill(bill)
                    }
                    (requireActivity() as OverviewActivity).vibratePhone()
                    Toast.makeText(requireContext(), "添加成功", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 删除或者保存
        deleteBtn.setOnClickListener {
            if (billInfoArg != null) {
                thread {
                    viewModel.billDao.deleteBill(billInfoArg!!.billId)
                }
                findNavController().navigateUp()
            } else {
                val bill = collectData()
                if (bill != null) {
                    thread {
                        viewModel.billDao.insertBill(bill)
                    }
                    findNavController().navigateUp()
                }
            }
        }
    }

    // 初始化
    private fun init() {
        if (billInfoArg != null) {
            Log.d("data", billInfoArg?.inMoney.toString())
            viewModel.money.value = billInfoArg?.inMoney
            viewModel.newDateTime = billInfoArg?.date!!
            billInfoArg?.inAccountId?.let {
                viewModel.inAccountId.value = it
            }
            billInfoArg?.outAccountId?.let {
                viewModel.outAccountId.value = it
            }
            remarkText.setText(billInfoArg?.remark)
        }
    }
}