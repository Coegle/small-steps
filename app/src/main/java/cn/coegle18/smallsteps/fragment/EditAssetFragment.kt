package cn.coegle18.smallsteps.fragment

import android.app.Application
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cn.coegle18.smallsteps.*
import cn.coegle18.smallsteps.activity.OverviewActivity
import cn.coegle18.smallsteps.entity.Account
import cn.coegle18.smallsteps.entity.Bill
import cn.coegle18.smallsteps.util.ActivityUtil
import cn.coegle18.smallsteps.util.CurrencyFormatInputFilter
import cn.coegle18.smallsteps.util.Util.balanceFormatterSimple
import cn.coegle18.smallsteps.viewmodel.EditAssetViewModel
import cn.coegle18.smallsteps.viewmodel.EditAssetViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_edit_asset.*
import kotlinx.android.synthetic.main.item_btn_two.*
import java.time.OffsetDateTime
import kotlin.concurrent.thread

/*

 */
class EditAssetFragment : Fragment() {

    private val args: EditAssetFragmentArgs by navArgs()

    private lateinit var viewModel: EditAssetViewModel

    // 为了符合调整余额后账户余额和账单的流入和流出对应，需要添加一笔账单记录。
    private fun adjustBalance(accountId: Long, oldBalance: Double): Bill? {
        val newBalance = balanceText.text.toString().toDoubleOrNull() ?: 0.00
        val adjustMoney = newBalance - oldBalance
        if (adjustMoney == 0.00) return null
        var adjustBill = Bill(
            OffsetDateTime.now(),
            Constants.defaultInitAccountCategoryMap[TradeType.EXPENSE]!!,
            null,
            "余额调整为￥${balanceFormatterSimple.format(newBalance)}",
            -adjustMoney,
            accountId,
            null,
            null,
            -adjustMoney,
            0.00,
            Source.SYSTEM,
            Visible.SYSTEM
        )
        if (adjustMoney > 0.00) {
            adjustBill = Bill(
                OffsetDateTime.now(),
                Constants.defaultInitAccountCategoryMap[TradeType.INCOME]!!,
                null,
                "余额调整为￥${balanceFormatterSimple.format(newBalance)}",
                null,
                null,
                adjustMoney,
                accountId,
                0.00,
                adjustMoney,
                Source.SYSTEM,
                Visible.SYSTEM
            )
        }
        return adjustBill
    }

    private fun setIcon(resName: String) {
        val icon = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.ic_icon_constraint
        ) as LayerDrawable
        val resId = requireContext().resources.getIdentifier(
            resName,
            "drawable",
            requireContext().packageName
        )
        val res = ContextCompat.getDrawable(requireContext(), resId)
        if (res != null) {
            icon.setDrawableByLayerId(R.id.firstLayer, res)
            accountField.startIconDrawable = icon
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_asset, container, false)
    }

    private fun collectData(account: Account): Account {
        val balance = balanceText.text.toString().toDoubleOrNull()
        account.name = accountNameText.text.toString()
        account.balance = balance ?: 0.0
        account.remark = remarkText.text.toString()

        return account
    }

    private fun setBtn() {
        if (args.accountId != 0L) { // 旧账户的删除按钮
            val accountView = viewModel.accountView.value
            deleteBtn.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("确定删除该账户？")
                    .setMessage("该账户下有${accountView?.billNum}条账单记录，仅删除账户将保留账单记录。")
                    .setPositiveButton("全部删除") { _, _ ->
                        thread {
                            viewModel.accountDao.deleteAccount(args.accountId)
                            viewModel.billDao.deleteBillsOfAccount(args.accountId)
                        }
                        Log.d("action", "全部删除")
                            findNavController().navigate(EditAssetFragmentDirections.addSuccessAction())
                        }
                        .setNeutralButton("仅删除账户") { _, _ ->
                            thread { viewModel.accountDao.deleteAccount(args.accountId) }
                            Log.d("action", "仅删除账户")
                            findNavController().navigate(EditAssetFragmentDirections.addSuccessAction())
                        }
                        .setNegativeButton("取消", null)
                        .show()
            }
        }
        okBtn.setOnClickListener {
            if (accountNameText.text.toString() != "") {

                val oldAccountView = viewModel.accountView.value
                if (oldAccountView == null) { // 新账户
                    val newAccount = collectData(
                        Account(
                            args.accountType, "",
                            Visible.ENABLED, 0, 0.0, ""
                        )
                    )
                    Log.d("data", newAccount.toString())
                    thread {
                        val newAccountId = viewModel.accountDao.insertAccount(newAccount)
                        adjustBalance(newAccountId, 0.00)?.let {
                            viewModel.billDao.insertBill(it)
                        }
                    }
                    ActivityUtil.hideSoftKeyBoard(requireActivity())
                    findNavController().navigate(EditAssetFragmentDirections.addSuccessAction())
                } else { // 旧账户
                    val newAccount = collectData(oldAccountView.toAccount())
                    Log.d("data", newAccount.toString() + newAccount.accountId)
                    thread {
                        viewModel.accountDao.updateAccount(newAccount)
                        adjustBalance(newAccount.accountId, oldAccountView.balance)?.let {
                            viewModel.billDao.insertBill(it)
                        }
                    }
                    ActivityUtil.hideSoftKeyBoard(requireActivity())
                    findNavController().navigateUp()
                }
            } else {
                Toast.makeText(requireContext(), "账户名不能为空", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun draw(pName: String?, cName: String, icon: String, custom: Boolean, hint: String) {
        setIcon("ic_fund_${icon}_white")

        accountNameText.setText(pName ?: cName)
        if (!custom) {
            accountNameText.isEnabled = false
            accountNameText.setTextColor(requireContext().getColor(R.color.material_on_background_emphasis_medium))
        }
        if (pName != null) {
            accountField.suffixText = pName
        }
        remarkTextField.helperText = "$hint（可选）"
    }

    private fun setNewAccountInterface() {
        (requireActivity() as OverviewActivity).setActionBarTitle("添加账户")
        okBtn.text = "添加账户"
        deleteBtn.visibility = View.GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        balanceText.filters = arrayOf<InputFilter>(CurrencyFormatInputFilter())
        viewModel = ViewModelProvider(
            this,
            EditAssetViewModelFactory(Application(), args.accountId, args.accountType)
        ).get(
            EditAssetViewModel::class.java
        )
        viewModel.displayData.observe(viewLifecycleOwner) { // 绘制新建账户和已有账户的公共部分
            if (it != null) draw(it.pName, it.cName, it.icon, it.custom, it.hint)
        }
        viewModel.accountView.observe(viewLifecycleOwner) { // 如果返回为 null，说明是新的账户，否则设置已有账户的相关数据
            if (it != null) {
                balanceText.setText(balanceFormatterSimple.format(it.balance))
                remarkText.setText(it.remark)
            } else {
                setNewAccountInterface()
            }
        }
        setBtn()
    }
}