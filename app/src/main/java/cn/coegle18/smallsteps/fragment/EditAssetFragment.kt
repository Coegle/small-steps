package cn.coegle18.smallsteps.fragment

import android.content.Context
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.Visible
import cn.coegle18.smallsteps.activity.OverviewActivity
import cn.coegle18.smallsteps.dao.AccountDao
import cn.coegle18.smallsteps.dao.BillDao
import cn.coegle18.smallsteps.entity.Account
import cn.coegle18.smallsteps.util.ActivityUtil
import cn.coegle18.smallsteps.util.CurrencyFormatInputFilter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_edit_asset.*
import kotlinx.android.synthetic.main.item_btn_two.*
import kotlin.concurrent.thread

/*

 */
class EditAssetFragment : Fragment() {

    private val args: EditAssetFragmentArgs by navArgs()
    private lateinit var accountDao: AccountDao
    private lateinit var billDao: BillDao

    private fun setIcon(resName: String) {
        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_icon_constraint) as LayerDrawable
        val resId = requireContext().resources.getIdentifier(resName, "drawable", requireContext().packageName)
        val res = ContextCompat.getDrawable(requireContext(), resId)
        if (res != null) {
            icon.setDrawableByLayerId(R.id.firstLayer, res)
            accountField.startIconDrawable = icon
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        accountDao = AppDatabase.getDatabase(context).accountDao()
        billDao = AppDatabase.getDatabase(context).billDao()
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
        if (args.accountInfo.accountId == 0L) {
            deleteBtn.visibility = View.GONE
            okBtn.text = "添加账户"
        } else {
            deleteBtn.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle("确定删除该账户？")
                        .setMessage("该账户下有${args.accountInfo.billNum}条账单记录，仅删除账户将保留账单记录。")
                        .setPositiveButton("全部删除") { _, _ ->
                            thread {
                                accountDao.deleteAccount(args.accountInfo.accountId)
                                billDao.deleteBillsOfAccount(args.accountInfo.accountId)
                            }
                            Log.d("action", "全部删除")
                            findNavController().navigate(EditAssetFragmentDirections.addSuccessAction())
                        }
                        .setNeutralButton("仅删除账户") { _, _ ->
                            thread { accountDao.deleteAccount(args.accountInfo.accountId) }
                            Log.d("action", "仅删除账户")
                            findNavController().navigate(EditAssetFragmentDirections.addSuccessAction())
                        }
                        .setNegativeButton("取消", null)
                        .show()
            }
        }
        okBtn.setOnClickListener {
            if (accountNameText.text.toString() != "") {
                val oldAccount = Account(args.accountInfo.accountTypeId, "",
                        Visible.ENABLED, 0, 0.0, "")
                if (args.accountInfo.accountId == 0L) {
                    val newAccount = collectData(oldAccount)
                    Log.d("data", newAccount.toString())
                    thread { accountDao.insertAccount(newAccount) }
                    ActivityUtil.hideSoftKeyBoard(requireActivity())
                    findNavController().navigate(EditAssetFragmentDirections.addSuccessAction())
                } else {
                    oldAccount.billNum = args.accountInfo.billNum
                    oldAccount.accountId = args.accountInfo.accountId
                    val newAccount = collectData(oldAccount)
                    Log.d("data", newAccount.toString() + newAccount.accountId)
                    thread { accountDao.updateAccount(newAccount) }
                    ActivityUtil.hideSoftKeyBoard(requireActivity())
                    findNavController().navigateUp()
                }
            } else {
                Toast.makeText(requireContext(), "账户名不能为空", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args.accountInfo.apply {

            setIcon("ic_fund_${icon}_white")

            accountNameText.setText(name)
            if (!args.accountInfo.custom) {
                accountNameText.isEnabled = false
                accountNameText.setTextColor(requireContext().getColor(R.color.material_on_background_emphasis_medium))
            }

            if (args.accountInfo.accountTypePName != null) {
                accountField.suffixText = accountTypePName
            }

            remarkTextField.helperText = "$hint（可选）"
        }

        balanceText.filters = arrayOf<InputFilter>(CurrencyFormatInputFilter())

        // 根据参数确定是创建新的账户还是修改账户信息
        if (args.accountInfo.accountId != 0L) {
            args.accountInfo.apply {
                balanceText.setText(balance.toString())
                remarkText.setText(remark)
            }
        } else {
            (requireActivity() as OverviewActivity).setActionBarTitle("添加账户")
        }

        setBtn()
    }
}