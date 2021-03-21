package cn.coegle18.smallsteps.entity

import com.chad.library.adapter.base.entity.SectionEntity

data class AccountSection(
    override val isHeader: Boolean,
    val data: Any, /*在 Account 列表中用于分区*/
) : SectionEntity
