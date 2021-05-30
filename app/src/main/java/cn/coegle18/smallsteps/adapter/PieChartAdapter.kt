package cn.coegle18.smallsteps.adapter

import android.util.Log
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.entity.PieChartData
import cn.coegle18.smallsteps.util.Util
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.util.*

class PieChartAdapter : BaseQuickAdapter<PieChartData, BaseViewHolder>(R.layout.item_category_pie_chart) {
    override fun convert(holder: BaseViewHolder, item: PieChartData) {
        holder.apply {
            setText(R.id.titleText, item.name)
            val progressBar = getView<cn.coegle18.smallsteps.HProgressView>(R.id.percentBar)
            if (item.tradeType == TradeType.EXPENSE) {
                setText(R.id.percentText, Util.balanceFormatterSimple.format(item.expPer) + "%")
                setText(R.id.balanceText, "￥" + Util.balanceFormatter.format(item.expense))
                Log.d("percent expense", item.expPer.toFloat().toString())
                setBackgroundResource(R.id.iconImg, R.drawable.ic_bg_red)
                progressBar.setProgress(item.expPer.toFloat())
                progressBar.setProgressColor(context.getColor(R.color.red))
            } else if (item.tradeType == TradeType.INCOME) {
                setText(R.id.percentText, Util.balanceFormatterSimple.format(item.incPer) + "%")
                setText(R.id.balanceText, "￥" + Util.balanceFormatter.format(item.income))
                setBackgroundResource(R.id.iconImg, R.drawable.ic_bg_green)
                progressBar.setProgress(item.incPer.toFloat())
                progressBar.setProgressColor(context.getColor(R.color.green))
            }
            setText(R.id.captionText, "共${item.billNum}笔")
            setImageResource(R.id.iconImg,
                    context.resources.getIdentifier("ic_category_${item.tradeType.name.toLowerCase(Locale.ROOT)}_${item.icon}", "drawable", context.packageName))
        }

    }
}