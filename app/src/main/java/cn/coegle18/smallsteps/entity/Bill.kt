package cn.coegle18.smallsteps.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.coegle18.smallsteps.*
import java.time.OffsetDateTime

/*
账单实体，
 */
@Entity
data class Bill(
        val date: OffsetDateTime,
        val categoryId: Int,
        var relation: Relation?,
        var remark: String,
        var outMoney: Double?,
        var outAccountId: Int?,
        var inMoney: Double?,
        var inAccountId: Int?,
        var expense: Double,
        var income: Double,
        val source: Source,
        var imported: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var billId: Long = 0
}