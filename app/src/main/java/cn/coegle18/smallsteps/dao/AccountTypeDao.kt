package cn.coegle18.smallsteps.dao

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
    @Query("Select * from AccountType where primaryAccountType = :primaryAccountType")
    fun queryPrimaryAccountTypeInstance(primaryAccountType: PrimaryAccountType): AccountOfAccountType

    // 查询某 PrimaryAccountType 下的可创建的账户类型
    @Query("Select * from AccountType where primaryAccountType = :primaryAccountType")
    fun queryPrimaryAccountType(primaryAccountType: PrimaryAccountType): List<AccountType>

    // 查询某最终账户类型
    @Query("Select * from AccountType where parentId = :pId")
    fun queryFinalAccountType(pId: Long): List<AccountType>
}