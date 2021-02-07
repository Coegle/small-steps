package cn.coegle18.smallsteps.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.coegle18.smallsteps.BaseAccountType
import cn.coegle18.smallsteps.MainAccountType
import cn.coegle18.smallsteps.PrimaryAccountType

@Entity
data class AccountType(val finalType: Boolean,
                       val baseAccountType: BaseAccountType,
                       val primaryAccountType: PrimaryAccountType,
                       val name: String,
                       val mainAccountType: MainAccountType,
                       val parentId: Long,
                       val visible: Boolean,
                       val custom: Boolean,
                       val autoImport: Boolean,
                       val hint: String,
                       val icon: String
) {
    @PrimaryKey(autoGenerate = true)
    var accountTypeId: Long = 0
}











