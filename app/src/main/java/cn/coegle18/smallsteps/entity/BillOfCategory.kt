package cn.coegle18.smallsteps.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BillOfCategory(
        @Embedded val category: Category,
        @Relation(
                parentColumn = "categoryId",
                entityColumn = "category"
        )
        val bills: List<Bill>
)