package cn.coegle18.smallsteps.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import cn.coegle18.smallsteps.MainAccountType
import cn.coegle18.smallsteps.Visible
import cn.coegle18.smallsteps.entity.Account
import cn.coegle18.smallsteps.entity.AccountView
import cn.coegle18.smallsteps.entity.BillInAccount
import cn.coegle18.smallsteps.entity.BillOutAccount

@Dao
interface AccountDao {
    @Insert
    fun insertAccount(account: Account): Long

    @Update
    fun updateAccount(newAccount: Account)

    // 查询指定账户的所有流入账单
    @Transaction
    @Query("Select * from Account where accountId = :inAccount")
    fun queryInBills(inAccount: Long): BillInAccount

    // 查询指定账户的所有流出账单
    @Transaction
    @Query("Select * from Account where accountId = :outAccount")
    fun queryOutBills(outAccount: Long): BillOutAccount

    // 查询 AccountList 视图中所有可见账户
    // 查询指定账户
    @Query("Select * from AccountView where accountId =:accountId")
    fun queryAccountView(accountId: Long): LiveData<AccountView>

    @Query("Select * from AccountView where visible = :visible")
    fun queryAccountViewList(visible: Visible = Visible.ENABLED): LiveData<List<AccountView>>

    // 查询指定 MainAccountType 和可见性的账户列表
    @Query("Select * from AccountView where mainAccountType in (:accountTypeList) and visible in (:visible)")
    fun queryAccountViewList(accountTypeList: List<MainAccountType>, visible: List<Visible> = listOf(Visible.ENABLED, Visible.DEACTIVATED)): LiveData<List<AccountView>>

    // 删除账户（将可见性设置为 DISABLED）
    @Query("update Account set visible = :visible where accountId = :accountId")
    fun deleteAccount(accountId: Long, visible: Visible = Visible.DISABLED)
}