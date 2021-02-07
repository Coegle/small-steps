package cn.coegle18.smallsteps.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BillInAccount(
        @Embedded val account: Account,
        @Relation(
                parentColumn = "accountId",
                entityColumn = "inAccount"
        )
        val accounts: List<Bill>
)