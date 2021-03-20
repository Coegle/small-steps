package cn.coegle18.smallsteps.adapter

import androidx.core.content.ContextCompat
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.entity.CategoryView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.util.*

class CategoryListAdapter(layoutResId: Int = R.layout.item_account) : BaseQuickAdapter<CategoryView, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CategoryView) {
        holder.apply {
            setGone(R.id.caption, true)
            setGone(R.id.balanceText, true)
            setText(R.id.titleText, item.cName ?: item.pName)
            val res = ContextCompat.getDrawable(context, context.resources.getIdentifier("ic_category_${item.tradeType.name.toLowerCase(Locale.ROOT)}_${item.cIcon ?: item.pIcon}", "drawable", context.packageName))
            setImageDrawable(R.id.accountIcon, res)
        }
    }
}