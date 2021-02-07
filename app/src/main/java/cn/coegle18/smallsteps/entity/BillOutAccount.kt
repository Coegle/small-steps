package cn.coegle18.smallsteps.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BillOutAccount(
        @Embedded val account: Account,
        @Relation(
                parentColumn = "accountId",
                entityColumn = "outAccount"
        )
        val bills: List<Bill>
)