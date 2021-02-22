package cn.coegle18.smallsteps.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.coegle18.smallsteps.Relation
import cn.coegle18.smallsteps.Source
import kotlinx.android.parcel.Parcelize
import java.time.OffsetDateTime

/*
账单实体，
 */
@Entity
@Parcelize
data class Bill(
        val date: OffsetDateTime,
        val category: Long,
        var relation: Relation?,
        var remark: String,
        var outMoney: Double?,
        var outAccount: Long?,
        var inMoney: Double?,
        var inAccount: Long?,
        var expense: Double,
        var income: Double,
        val source: Source,
        var imported: Boolean
) : Parcelable {
        @PrimaryKey(autoGenerate = true)
        var billId: Long = 0
}