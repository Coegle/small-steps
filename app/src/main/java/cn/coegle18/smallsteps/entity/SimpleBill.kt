package cn.coegle18.smallsteps.entity

import android.os.Parcelable
import cn.coegle18.smallsteps.Source
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.Visible
import kotlinx.android.parcel.Parcelize
import java.time.OffsetDateTime

data class SimpleBill(
        val date: OffsetDateTime,
        val pCategory: Long,
        val cCategory: Long?,
        val tradeType: TradeType,
        val money: Double,
        val inAccount: Long?,
        val outAccount: Long?,
        val remark: String,
        val source: Source
) {
    fun toBill(): Bill {
        return when (tradeType) {
            TradeType.INCOME -> {
                Bill(date, cCategory
                        ?: pCategory, null, remark, null, null, money, inAccount, 0.0, money, source, Visible.ENABLED)
            }
            TradeType.EXPENSE -> {
                Bill(date, cCategory
                        ?: pCategory, null, remark, money, outAccount, null, null, money, 0.0, source, Visible.ENABLED)
            }
            TradeType.TRANSFER -> {
                Bill(date, cCategory
                        ?: pCategory, null, remark, money, outAccount, money, inAccount, 0.0, 0.0, source, Visible.ENABLED)
            }
        }
    }
}

@Parcelize
data class NJUSTBill(
        val dateStr: String,
        val money: Double,
        val tradeTypeStr: String,
        val remark: String
) : Parcelable
