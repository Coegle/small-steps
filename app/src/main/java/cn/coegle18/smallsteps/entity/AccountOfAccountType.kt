package cn.coegle18.smallsteps.entity

import androidx.room.Embedded
import androidx.room.Relation

data class AccountOfAccountType(
        @Embedded val accountType: AccountType,
        @Relation(
                parentColumn = "accountTypeId",
                entityColumn = "accountType"
        )
        val accounts: List<Account>
)