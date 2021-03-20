package cn.coegle18.smallsteps.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.entity.BillView
import cn.coegle18.smallsteps.fragment.ExpenseBillFragment
import cn.coegle18.smallsteps.fragment.IncomeBillFragment
import cn.coegle18.smallsteps.fragment.TransBillFragment

class EditBillAdapter(fragment: Fragment, private val billInfo: BillView?, private val accountId: Long) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3
    override fun createFragment(position: Int): Fragment {

        return when (TradeType.values()[position]) {
            TradeType.EXPENSE -> {
                ExpenseBillFragment.newInstance(billInfo, accountId)
            }
            TradeType.INCOME -> {
                IncomeBillFragment.newInstance(billInfo, accountId)
            }
            TradeType.TRANSFER -> {
                TransBillFragment.newInstance(billInfo, accountId)
            }
        }
    }

}