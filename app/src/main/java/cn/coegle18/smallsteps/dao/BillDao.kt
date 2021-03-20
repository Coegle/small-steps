package cn.coegle18.smallsteps.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cn.coegle18.smallsteps.entity.Bill
import cn.coegle18.smallsteps.entity.BillView
import java.time.OffsetDateTime

@Dao
interface BillDao {
    @Insert
    fun insertBill(bill: Bill): Long

    @Update
    fun updateBill(bill: Bill)

    @Query("Select * from BillView where imported = 1 order by date DESC")
    fun simpleQuery(): LiveData<List<BillView>>

    // 按时间顺序查询指定时间段内的账单
    @Query("Select * from BillView where date between :from and :to ORDER BY datetime(date)")
    fun queryBillBetweenDates(from: OffsetDateTime, to: OffsetDateTime = OffsetDateTime.now()): List<BillView>

    @Query("Select * from Bill where inAccount in (:accountList) or outAccount in (:accountList)")
    fun queryBillList(accountList: List<Long>): List<Bill>

    @Query("Update bill set imported = 0 where billId = :id")
    fun deleteBill(id: Long)

    @Query("Update bill set imported = 0 where inAccount = :accountId or outAccount = :accountId")
    fun deleteBillsOfAccount(accountId: Long)

}