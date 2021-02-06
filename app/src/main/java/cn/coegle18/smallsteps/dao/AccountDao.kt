package cn.coegle18.smallsteps.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cn.coegle18.smallsteps.entity.Account

@Dao
interface AccountDao {
    @Insert
    fun insertAccount(account: Account): Long

    @Update
    fun updateAccount(newAccount: Account)

    @Query("Select * from Account where visible = 1")
    fun queryVisibleAccounts(): List<Account>

    @Query("Select * from Account")
    fun queryAllAccounts(): List<Account>


}