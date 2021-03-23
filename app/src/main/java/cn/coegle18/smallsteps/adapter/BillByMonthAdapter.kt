package cn.coegle18.smallsteps.adapter

import android.view.View
import cn.coegle18.smallsteps.MainAccountType
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.Visible
import cn.coegle18.smallsteps.entity.BillView
import cn.coegle18.smallsteps.util.Util
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.util.*

class BillByMonthAdapter : BaseNodeAdapter() {
    init {
        addNodeProvider(MonthRootNodeProvider())
        addNodeProvider(MonthSubNodeProvider())
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is MonthRootNode -> 0
            is MonthSubNode -> 1
            else -> -1
        }
    }

}

// 月份
class MonthRootNodeProvider() : BaseNodeProvider() {
    override val itemViewType: Int = 0
    override val layoutId: Int = R.layout.item_month
    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val data = (item as MonthRootNode).node
        helper.apply {
            setText(R.id.monthText, data.month + " 月")
            setText(R.id.yearText, data.year + " 年")
            setText(R.id.balanceText, "￥" + (data.outMoney?.let { Util.balanceFormatter.format(it) }
                    ?: "0.00"))
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        getAdapter()?.expandOrCollapse(position)
    }
}

// 账单
class MonthSubNodeProvider() : BaseNodeProvider() {
    override val itemViewType: Int = 1
    override val layoutId: Int = R.layout.item_bill
    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val data = (item as MonthSubNode).node
        if (data != null) {
            helper.apply {
                val categoryName = data.categoryPName + if (data.categoryCName != null) {
                    " - ${data.categoryCName}"
                } else {
                    ""
                }
                setText(R.id.categoryText, categoryName)
                setCategoryIcon(helper, data)
                setText(R.id.dateText, "${data.date.monthValue}月${data.date.dayOfMonth}日")
                setText(R.id.remarkText, data.remark)
                setGone(R.id.splitText, data.splitFlag == null)

                // 退款Text
                if (data.refundFlag != null && data.displayTradeType == TradeType.EXPENSE) {
                    setGone(R.id.refundText, false)
                    setText(R.id.refundText, "(已退￥${data.inMoney})")
                } else {
                    setGone(R.id.refundText, true)
                }
                // 可报销 Flag
                if (data.reimbursementFlag != null && data.displayTradeType == TradeType.EXPENSE) {
                    setGone(R.id.reimburseText, false)
                    setText(R.id.reimburseText, "已报销")
                } else if (data.inAccountMainAccountType != null && data.inAccountMainAccountType == MainAccountType.REIMBURSEMENT) {
                    setGone(R.id.reimburseText, false)
                    setText(R.id.reimburseText, "可报销")
                } else {
                    setGone(R.id.reimburseText, true)
                }

                when {
                    data.visible == Visible.SYSTEM -> { // 调整账户余额
                        setGone(R.id.moneyText, true)
                        setBackgroundResource(R.id.categoryImage, R.drawable.ic_bg_brown)
                        setGone(R.id.accountText, true)
                    }
                    data.tradeType == TradeType.INCOME -> {
                        setGone(R.id.moneyText, false)
                        setText(R.id.moneyText, "￥${Util.balanceFormatter.format(data.inMoney)}")
                        setBackgroundResource(R.id.categoryImage, R.drawable.ic_bg_green)
                        setTextColor(R.id.moneyText, context.getColor(R.color.green))
                        setGone(R.id.accountText, true)
                    }
                    data.tradeType == TradeType.EXPENSE -> {
                        setGone(R.id.moneyText, false)
                        setText(R.id.moneyText, "￥${Util.balanceFormatter.format(data.outMoney)}")
                        setBackgroundResource(R.id.categoryImage, R.drawable.ic_bg_red)
                        setTextColor(R.id.moneyText, context.getColor(R.color.red))
                        setGone(R.id.accountText, true)
                    }
                    data.tradeType == TradeType.TRANSFER -> {
                        setGone(R.id.moneyText, false)
                        setText(R.id.moneyText, "￥${Util.balanceFormatter.format(data.outMoney)}")
                        setBackgroundResource(R.id.categoryImage, R.drawable.ic_bg_brown)
                        setTextColor(
                            R.id.moneyText,
                            context.getColor(R.color.material_on_background_emphasis_high_type)
                        )
                        setText(R.id.accountText, "${data.outAccountName} -> ${data.inAccountName}")
                    }
                }
            }
        }
    }

    private fun setCategoryIcon(holder: BaseViewHolder, data: BillView) {
        val resId = if (data.visible == Visible.SYSTEM) {
            context.resources.getIdentifier(
                "ic_category_${data.categoryPIcon}",
                "drawable",
                context.packageName
            )
        } else {
            context.resources.getIdentifier(
                "ic_category_${data.tradeType.name.toLowerCase(Locale.ROOT)}_${data.categoryCIcon ?: data.categoryPIcon}",
                "drawable", context.packageName
            )
        }
        holder.setImageResource(R.id.categoryImage, resId)
    }
}

class MonthRootNode(val node: MonthHeader, override val childNode: MutableList<BaseNode>) : BaseExpandNode()
class MonthSubNode(val node: BillView?) : BaseNode() {
    override val childNode: MutableList<BaseNode>? = null // 没有子节点
}

data class MonthHeader(
        val year: String,
        val month: String,
        val outMoney: Double?
)