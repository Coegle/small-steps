package cn.coegle18.smallsteps.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.coegle18.smallsteps.Relation


@Entity
data class RelationOfBills(
        val relation: Relation,
        var mainBill: Int,
        var relatedBill: Int
) {
    @PrimaryKey(autoGenerate = true)
    var relationId: Long = 0
}
