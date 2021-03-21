package cn.coegle18.smallsteps.adapter

import cn.coegle18.smallsteps.MainAccountType
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.entity.BillView
import cn.coegle18.smallsteps.util.Util
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.util.*

class BillAdapter(layoutResId: Int) : BaseQuickAdapter<BillView, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: BillView) {
        holder.apply {
            val categoryName = item.categoryPName + if (item.categoryCName != null) {
                " - ${item.categoryCName}"
            } else {
                ""
            }
            setText(R.id.categoryText, categoryName)
            setCategoryIcon(holder, item)
            setText(R.id.dateText, "${item.date.monthValue}月${item.date.dayOfMonth}日")
            setText(R.id.remarkText, item.remark)
            setGone(R.id.splitText, item.splitFlag == null)

            // 退款Text
            if (item.refundFlag != null && item.displayTradeType == TradeType.EXPENSE) {
                setGone(R.id.refundText, false)
                setText(R.id.refundText, "(已退￥${item.inMoney})")
            } else {
                setGone(R.id.refundText, true)
            }
            // 可报销 Flag
            if (item.reimbursementFlag != null && item.displayTradeType == TradeType.EXPENSE) {
                setGone(R.id.reimburseText, false)
                setText(R.id.reimburseText, "已报销")
            } else if (item.inAccountMainAccountType != null && item.inAccountMainAccountType == MainAccountType.REIMBURSEMENT) {
                setGone(R.id.reimburseText, false)
                setText(R.id.reimburseText, "可报销")
            } else {
                setGone(R.id.reimburseText, true)
            }

            when (item.tradeType) {
                TradeType.INCOME -> {
                    setText(R.id.moneyText, "￥${Util.balanceFormatter.format(item.inMoney)}")
                    setBackgroundResource(R.id.categoryImage, R.drawable.ic_bg_green)
                    setTextColor(R.id.moneyText, context.getColor(R.color.green))
                    setText(R.id.accountText, item.inAccountName)
                }
                TradeType.EXPENSE -> {
                    setText(R.id.moneyText, "￥${Util.balanceFormatter.format(item.outMoney)}")
                    setBackgroundResource(R.id.categoryImage, R.drawable.ic_bg_red)
                    setTextColor(R.id.moneyText, context.getColor(R.color.red))
                    setText(R.id.accountText, item.outAccountName)
                }
                TradeType.TRANSFER -> {
                    setText(R.id.moneyText, "￥${Util.balanceFormatter.format(item.outMoney)}")
                    setBackgroundResource(R.id.categoryImage, R.drawable.ic_bg_brown)
                    setTextColor(
                        R.id.moneyText,
                        context.getColor(R.color.material_on_background_emphasis_high_type)
                    )
                    setText(R.id.accountText, "${item.outAccountName} -> ${item.inAccountName}")
                }
            }
        }
    }

    private fun setCategoryIcon(holder: BaseViewHolder, data: BillView) {
        holder.setImageResource(
            R.id.categoryImage, context.resources.getIdentifier(
                "ic_category_${data.tradeType.name.toLowerCase(Locale.ROOT)}_${
                    if (data.categoryCId != null) {
                        data.categoryCIcon
                    } else {
                        data.categoryPIcon
                    }
                }", "drawable", context.packageName
            )
        )
    }
}