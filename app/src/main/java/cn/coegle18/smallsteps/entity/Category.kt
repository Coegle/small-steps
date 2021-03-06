package cn.coegle18.smallsteps.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.coegle18.smallsteps.*
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Category(val tradeType: TradeType,
                    val displayTradeType: DisplayTradeType,
                    var name: String,
                    val relatedAccountType: MainAccountType?,
                    var parentId: Long,
                    var visible: Visible,
                    var order: Double,
                    var editable: Editable,
                    var icon: String
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var categoryId: Long = 0
}