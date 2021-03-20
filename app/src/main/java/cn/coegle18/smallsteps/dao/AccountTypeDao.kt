package cn.coegle18.smallsteps.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import cn.coegle18.smallsteps.PrimaryAccountType
import cn.coegle18.smallsteps.entity.AccountOfAccountType
import cn.coegle18.smallsteps.entity.AccountType


@Dao
interface AccountTypeDao {
    // 查询某 PrimaryAccountType 下的账户实例
    @Transaction
    @Query("Select * from AccountType, Account where primaryAccountType = :primaryAccountType and Account.visible = :visible")
    fun queryPrimaryAccountTypeInstance(primaryAccountType: PrimaryAccountType, visible: Boolean): AccountOfAccountType

    // 查询某 PrimaryAccountType 下的一级视图
    @Query("Select * from AccountType where primaryAccountType = :primaryAccountType and parentId = 0 and visible = 1")
    fun queryPrimaryAccountType(primaryAccountType: PrimaryAccountType): LiveData<List<AccountType>>

    // 查询某最终账户类型（二级视图）
    @Query("Select * from AccountType where parentId = :pId and visible = 1")
    fun queryFinalAccountType(pId: Long): LiveData<List<AccountType>>

    // 查询所有 PrimaryAccountType 及其账户实例
    @Transaction
    @Query("Select * from AccountType")
    fun queryPrimaryAccountTypeInUse(): LiveData<List<AccountOfAccountType>>
}