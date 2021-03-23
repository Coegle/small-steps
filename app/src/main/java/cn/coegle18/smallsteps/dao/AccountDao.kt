package cn.coegle18.smallsteps.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import cn.coegle18.smallsteps.MainAccountType
import cn.coegle18.smallsteps.Visible
import cn.coegle18.smallsteps.entity.*

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
    fun queryAccountViewList(
        accountTypeList: List<MainAccountType>,
        visible: List<Visible> = listOf(Visible.ENABLED, Visible.SYSTEM)
    ): LiveData<List<AccountView>>

    // 删除账户（将可见性设置为 DISABLED）
    @Query("update Account set visible = :visible where accountId = :accountId")
    fun deleteAccount(accountId: Long, visible: Visible = Visible.DISABLED)

    // 查询 AccountTypeView
    @Query("SELECT p.name as pName, c.name as cName, c.icon, c.custom, c.hint FROM accountType as c LEFT JOIN accountType as p ON c.parentId = p.accountTypeId WHERE c.accountTypeId = :accountTypeId OR (p.accountTypeId = :accountTypeId AND p.finalType = 1);")
    fun queryAccountTypeView(accountTypeId: Long): LiveData<AccountTypeView>

    // 更新指定账户的余额信息
    @Query("Update Account set balance = balance + :difference where accountId = :accountId")
    fun updateAccountBalance(accountId: Long, difference: Double)
}