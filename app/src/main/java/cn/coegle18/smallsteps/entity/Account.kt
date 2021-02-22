package cn.coegle18.smallsteps.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Account(
    val accountType: Long,
    val name: String,
    var visible: Boolean,
    var billNum: Long,
    var balance: Double,
    var remark: String
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var accountId: Long = 0
}
