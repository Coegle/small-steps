package cn.coegle18.smallsteps.adapter

import android.view.View
import cn.coegle18.smallsteps.MainAccountType
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.Visible
import cn.coegle18.smallsteps.entity.Category
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.util.*

class CategoryAdapter : BaseNodeAdapter(), DraggableModule {
    init {
        addFullSpanNodeProvider(RootNodeProvider())
        addNodeProvider(SubNodeProvider())

    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is RootNode -> 0
            is SubNode -> 1
            else -> -1
        }
    }
}

class RootNodeProvider : BaseNodeProvider() {
    override val layoutId: Int = R.layout.item_category
    override val itemViewType: Int = 0
    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val data = (item as RootNode).node

        helper.apply {
            setText(R.id.titleText, data.name)

            if (data.relatedAccountType == null || data.relatedAccountType == MainAccountType.REFUND) {
                setGone(R.id.caption, true)
            } else {
                setGone(R.id.caption, false)
                setText(R.id.caption, "可关联${data.relatedAccountType.caption}账户")
            }
            setImageResource(R.id.iconImg,
                    context.resources.getIdentifier("ic_category_${data.tradeType.name.toLowerCase(Locale.ROOT)}_${data.icon}", "drawable", context.packageName))
            if (data.visible == Visible.ENABLED) {
                setBackgroundResource(R.id.iconImg,
                        when (data.tradeType) {
                            TradeType.EXPENSE -> R.drawable.ic_bg_red
                            TradeType.INCOME -> R.drawable.ic_bg_green
                            TradeType.TRANSFER -> R.drawable.ic_bg_brown
                        })
            } else {
                setBackgroundResource(R.id.iconImg, R.drawable.ic_bg_grey)
            }
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        super.onClick(helper, view, data, position)
        getAdapter()!!.expandOrCollapse(position)
    }
}

class SubNodeProvider() : BaseNodeProvider() {
    override val layoutId: Int = R.layout.item_sub_category
    override val itemViewType: Int = 1
    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val data = (item as SubNode).node
        helper.apply {
            if (data != null) {
                setText(R.id.subCategoryText, data.name)
                setImageResource(R.id.subCategoryIcon, context.resources.getIdentifier("ic_category_${data.tradeType.name.toLowerCase(Locale.ROOT)}_${data.icon}", "drawable", context.packageName))
                if (data.visible == Visible.ENABLED) {
                    setBackgroundResource(R.id.subCategoryIcon,
                            when (data.tradeType) {
                                TradeType.EXPENSE -> R.drawable.ic_bg_red
                                TradeType.INCOME -> R.drawable.ic_bg_green
                                TradeType.TRANSFER -> R.drawable.ic_bg_brown
                            })
                } else {
                    setBackgroundResource(R.id.subCategoryIcon, R.drawable.ic_bg_grey)
                }
            } else {
                setText(R.id.subCategoryText, "添加子类")
                setImageResource(R.id.subCategoryIcon, context.resources.getIdentifier("ic_category_add_sub_btn", "drawable", context.packageName))
                setBackgroundResource(R.id.subCategoryIcon, 0)
            }

        }
    }
}

class RootNode(override val childNode: MutableList<BaseNode>, val node: Category) : BaseExpandNode()

class SubNode(val node: Category?) : BaseNode() {
    override val childNode: MutableList<BaseNode>? = null // 没有子节点
}