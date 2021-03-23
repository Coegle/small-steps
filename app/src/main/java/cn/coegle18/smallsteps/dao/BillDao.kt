package cn.coegle18.smallsteps.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cn.coegle18.smallsteps.Visible
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

    @Query("Select * from BillView where visible in (:visible) order by date DESC")
    fun simpleQuery(visible: List<Visible> = listOf(Visible.ENABLED)): LiveData<List<BillView>>

    // 按时间顺序查询指定时间段内的账单
    @Query("Select * from BillView where date between :from and :to ORDER BY datetime(date)")
    fun queryBillBetweenDates(
        from: OffsetDateTime,
        to: OffsetDateTime = OffsetDateTime.now()
    ): List<BillView>

    @Query("Select * from BillView where (inAccountId in (:accountList) or outAccountId in (:accountList)) and visible in (:visible) ORDER BY Datetime(date) DESC")
    fun queryBillList(
        accountList: List<Long>,
        visible: List<Visible> = listOf(Visible.ENABLED, Visible.SYSTEM)
    ): LiveData<List<BillView>>

    @Query("Update bill set visible = :visible where billId = :id")
    fun deleteBill(id: Long, visible: Visible = Visible.DISABLED)

    @Query("Update bill set visible = :visible where inAccount = :accountId or outAccount = :accountId")
    fun deleteBillsOfAccount(accountId: Long, visible: Visible = Visible.DISABLED)

    @Query("SELECT STRFTime(\"%Y\", c.dateList) as year, STRFTime(\"%m\", c.dateList) as month, SUM(b.expense) as outMoney FROM calendar AS c LEFT JOIN (SELECT date, expense FROM Bill WHERE visible in (:visible) and (inAccount = :accountId Or outAccount = :accountId) ) AS b ON c.dateList = DATE(b.date) WHERE c.dateList <= date(\"now\") AND c.dateList >= (SELECT MIN(DATE(minDate.`date`)) FROM Bill AS minDate where (inAccount = :accountId Or outAccount = :accountId)) GROUP BY STRFTime(\"%Y-%m\", c.dateList) ORDER BY dateList DESC")
    fun queryExpenseByMonth(
        accountId: Long,
        visible: List<Visible> = listOf(Visible.ENABLED, Visible.SYSTEM)
    ): LiveData<List<MonthHeader>>
}