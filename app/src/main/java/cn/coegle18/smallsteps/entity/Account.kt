package cn.coegle18.smallsteps.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Account(val accountType: Int,
                   val name: String,
                   var visible: Boolean,
                   var billNum: Int,
                   var balance: Double,
                   var remark: String) {
    @PrimaryKey(autoGenerate = true)
    var accountId: Long = 0
}
