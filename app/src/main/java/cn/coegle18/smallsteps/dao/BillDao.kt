package cn.coegle18.smallsteps.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cn.coegle18.smallsteps.adapter.MonthHeader
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

    @Query("Select * from BillView where (inAccountId in (:accountList) or outAccountId in (:accountList)) and imported = 1")
    fun queryBillList(accountList: List<Long>): LiveData<List<BillView>>

    @Query("Update bill set imported = 0 where billId = :id")
    fun deleteBill(id: Long)

    @Query("Update bill set imported = 0 where inAccount = :accountId or outAccount = :accountId")
    fun deleteBillsOfAccount(accountId: Long)

    @Query("SELECT STRFTime(\"%Y\", c.dateList) as year, STRFTime(\"%m\", c.dateList) as month, SUM(b.expense) as outMoney FROM calendar AS c LEFT JOIN (SELECT date, expense FROM Bill WHERE imported = 1 and (inAccount = :accountId Or outAccount = :accountId) ) AS b ON c.dateList = DATE(b.date) WHERE c.dateList <= date(\"now\") AND c.dateList >= (SELECT MIN(DATE(minDate.`date`)) FROM Bill AS minDate where (inAccount = :accountId Or outAccount = :accountId)) GROUP BY STRFTime(\"%Y-%m\", c.dateList) ORDER BY dateList DESC")
    fun queryExpenseByMonth(accountId: Long): LiveData<List<MonthHeader>>

    @Query("Select STRFTime(\"%m\", c.dateList) as month from calendar as c where dateList >= Date(\"2018-01-01\") and dateList <= Date(\"2020-01-01\") ")
    fun queryMonth(): List<String>
}