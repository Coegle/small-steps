package cn.coegle18.smallsteps.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.coegle18.smallsteps.Editable
import cn.coegle18.smallsteps.*

@Entity
data class Category(val tradeType: TradeType,
                    val displayTradeType: DisplayTradeType,
                    var name: String,
                    val relatedAccountType: MainAccountType?,
                    var parentId: Int,
                    var visible: Visible,
                    var order: Double,
                    var editable: Editable,
                    var icon: String
) {
    @PrimaryKey(autoGenerate = true)
    var categoryId: Long = 0
}