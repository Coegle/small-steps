package cn.coegle18.smallsteps.adapter

import androidx.core.content.ContextCompat
import cn.coegle18.smallsteps.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class BottomSheetAdapter(layoutResId: Int) : BaseQuickAdapter<SheetItem, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: SheetItem) {
        holder.apply {
            setText(R.id.titleText, item.caption)
            setGone(R.id.caption, true)
            setGone(R.id.rightIcon, true)
            setGone(R.id.leftIcon, true)
            if (!item.clickable) {
                setTextColor(R.id.titleText, ContextCompat.getColor(context, R.color.material_on_background_disabled))
            }
        }
        holder.itemView.isEnabled = item.clickable
    }
}

data class SheetItem(
        var caption: String,
        var clickable: Boolean
)
