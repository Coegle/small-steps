package cn.coegle18.smallsteps.entity

import android.os.Parcelable
import androidx.room.DatabaseView
import cn.coegle18.smallsteps.BaseAccountType
import cn.coegle18.smallsteps.MainAccountType
import cn.coegle18.smallsteps.PrimaryAccountType
import kotlinx.android.parcel.Parcelize

@DatabaseView("select " +
        "p.name accountTypePName," +
        "c.baseAccountType," +
        "c.primaryAccountType," +
        "c.mainAccountType," +
        "c.name accountTypeCName," +
        "c.icon," +
        "c.autoImport," +
        "c.custom," +
        "c.hint," +
        "acnt.accountType accountTypeId," +
        "accountId," +
        "acnt.name," +
        "acnt.visible," +
        "billNum," +
        "balance," +
        "acnt.remark " +
        "from account acnt, accountType c " +
        "left outer join accountType p on c.parentId = p.accountTypeId " +
        "where accountType = c.accountTypeId;")
@Parcelize
data class AccountView(
        val accountTypePName: String?,
        val baseAccountType: BaseAccountType,
        val primaryAccountType: PrimaryAccountType,
        val mainAccountType: MainAccountType,
        val accountTypeCName: String,
        val icon: String,
        val autoImport: Boolean,
        val custom: Boolean,
        val hint: String,
        val accountTypeId: Long,
        val accountId: Long,
        val name: String,
        val visible: Boolean,
        val billNum: Long,
        val balance: Double,
        val remark: String
) : Parcelable
