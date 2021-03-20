package cn.coegle18.smallsteps.entity

import android.os.Parcelable
import androidx.room.DatabaseView
import cn.coegle18.smallsteps.Editable
import cn.coegle18.smallsteps.MainAccountType
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.Visible
import kotlinx.android.parcel.Parcelize

@Parcelize
@DatabaseView("select " +
        "c.categoryId id," +
        "p.categoryId pId," +
        "p.name pName," +
        "0 as subCategoryNum," +
        "p.`order` pOrder," +
        "p.icon pIcon," +
        "c.categoryId cId," +
        "c.name cName," +
        "c.`order` cOrder," +
        "c.icon cIcon," +
        "c.tradeType," +
        "c.displayTradeType," +
        "c.relatedAccountType," +
        "c.visible," +
        "c.editable " +
        "from category p " +
        "join category c on c.parentId = p.categoryId " +
        "union select " +
        "p.categoryId, " +
        "p.categoryId, " +
        "p.name, " +
        "count(c.categoryId), " +
        "p.`order`," +
        "p.icon," +
        "null," +
        "null," +
        "null," +
        "null," +
        "p.tradeType," +
        "p.displayTradeType," +
        "p.relatedAccountType," +
        "p.visible," +
        "p.editable " +
        "from category p " +
        "left outer join category c " +
        "on (c.parentId = p.categoryId) and c.visible = \"ENABLED\" " +
        "where p.parentId = 0 group by p.categoryId")
data class CategoryView(
        val id: Long,
        val pId: Long,
        val pName: String,
        val subCategoryNum: Long,
        val pOrder: Double,
        val pIcon: String,
        val cId: Long?,
        val cName: String?,
        val cOrder: Double?,
        val cIcon: String?,
        val tradeType: TradeType,
        val displayTradeType: TradeType,
        val relatedAccountType: MainAccountType?,
        val visible: Visible,
        val editable: Editable
) : Parcelable
