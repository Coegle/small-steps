package cn.coegle18.smallsteps.adapter

import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.entity.CategoryView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.util.*

class CategoryGridAdapter(layoutResId: Int = R.layout.item_sub_category) :
    BaseQuickAdapter<CategoryWithDefaultSelection, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CategoryWithDefaultSelection) {
        val data = item.category
        holder.apply {
            setText(R.id.subCategoryText, data.cName ?: data.pName)
            setImageResource(
                R.id.subCategoryIcon,
                context.resources.getIdentifier(
                    "ic_category_${
                        data.tradeType.name.toLowerCase(Locale.ROOT)
                    }_${data.cIcon ?: data.pIcon}", "drawable", context.packageName
                )
            )
            if (item.isSelected) {
                setBackgroundResource(
                    R.id.subCategoryIcon,
                    when (data.tradeType) {
                        TradeType.EXPENSE -> R.drawable.ic_bg_red
                        TradeType.INCOME -> R.drawable.ic_bg_green
                        TradeType.TRANSFER -> R.drawable.ic_bg_brown
                    }
                )
            } else {
                setBackgroundResource(R.id.subCategoryIcon, R.drawable.ic_bg_grey)
            }
        }
    }
}

data class CategoryWithDefaultSelection(val category: CategoryView, val isSelected: Boolean)