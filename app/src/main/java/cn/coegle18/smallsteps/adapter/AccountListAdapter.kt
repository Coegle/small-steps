package cn.coegle18.smallsteps.adapter

import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.entity.AccountView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class AccountListAdapter(layoutResId: Int = R.layout.item_account) : BaseQuickAdapter<AccountView, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: AccountView) {
        holder.apply {
            setText(R.id.titleText, item.name)
            if (item.accountTypePName != null) {
                setText(R.id.caption, item.accountTypePName)
            } else {
                setGone(R.id.caption, true)
            }
            setImageResource(R.id.accountIcon, context.resources.getIdentifier("ic_fund_${item.icon}_white", "drawable", context.packageName))
            setGone(R.id.balanceText, true)
        }

    }
}