package cn.coegle18.smallsteps.adapter

import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.entity.AccountSection
import cn.coegle18.smallsteps.entity.AccountView
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder


class AccountAdapter constructor(
    layoutResId: Int,
    sectionHeadResId: Int,
    data: MutableList<AccountSection>
) : BaseSectionQuickAdapter<AccountSection, BaseViewHolder>(sectionHeadResId, data) {
    init {
        // 设置普通item布局（如果item类型只有一种，使用此方法）
        setNormalLayout(layoutResId)
    }

    override fun convert(holder: BaseViewHolder, item: AccountSection) {
        val account: AccountView = item.data as AccountView
        holder.setText(R.id.titleText, account.name)
        if (account.accountTypePName != null) {
            holder.setText(R.id.caption, account.accountTypePName)
        } else {
            holder.setGone(R.id.caption, true)
        }
        holder.setImageResource(
            R.id.accountIcon,
            context.resources.getIdentifier(
                "ic_fund_${account.icon}_white",
                "drawable",
                context.packageName
            )
        )
        holder.setText(R.id.balanceText, account.balance.toString())
    }

    /**
     * 设置header数据
     */
    override fun convertHeader(helper: BaseViewHolder, item: AccountSection) {
        helper.setText(R.id.headerText, item.data as String)
    }
}