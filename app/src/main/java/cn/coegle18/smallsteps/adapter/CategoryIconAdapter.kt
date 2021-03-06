package cn.coegle18.smallsteps.adapter

import cn.coegle18.smallsteps.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class CategoryIconAdapter(layoutResId: Int, data: MutableList<IconSetting>) : BaseQuickAdapter<IconSetting, BaseViewHolder>(layoutResId, data) {
    override fun convert(holder: BaseViewHolder, item: IconSetting) {
        holder.setImageResource(R.id.iconImg, context.resources.getIdentifier(item.icon, "drawable", context.packageName))
        holder.setBackgroundResource(R.id.iconImg, context.resources.getIdentifier(item.background, "drawable", context.packageName))
    }
}

class IconSetting(val background: String, val icon: String)