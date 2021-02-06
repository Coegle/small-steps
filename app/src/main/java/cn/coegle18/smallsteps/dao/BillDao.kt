package cn.coegle18.smallsteps.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cn.coegle18.smallsteps.entity.Bill
import java.time.OffsetDateTime

@Dao
interface BillDao {
    @Insert
    fun insertBill(bill: Bill): Long

    @Update
    fun updateBill(bill: Bill)

    // 按时间顺序查询指定时间段内的账单
    @Query("Select * from Bill where date between :from and :to ORDER BY datetime(date)")
    fun queryBillBetweenDates(from: OffsetDateTime, to: OffsetDateTime = OffsetDateTime.now()): List<Bill>
}