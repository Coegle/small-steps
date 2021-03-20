package cn.coegle18.smallsteps.adapter

import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.entity.AccountType
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class AddAccountAdapter(resourceId: Int, data: MutableList<AccountType>) : BaseQuickAdapter<AccountType, BaseViewHolder>(resourceId, data) {

    override fun convert(holder: BaseViewHolder, item: AccountType) {
        holder.setText(R.id.titleText, item.name)
        if (item.autoImport) {
            holder.setText(R.id.caption, "支持账单导入")
        } else {
            holder.setGone(R.id.caption, true)
        }
        if (item.finalType) {
            holder.setGone(R.id.rightIcon, true)
        }
        holder.setBackgroundResource(R.id.leftIcon, R.drawable.ic_bg_blue)
        holder.setImageResource(R.id.leftIcon, context.resources.getIdentifier("ic_fund_${item.icon}_white", "drawable", context.packageName))
    }


}