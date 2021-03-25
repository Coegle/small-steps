package cn.coegle18.smallsteps.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import cn.coegle18.smallsteps.entity.CashFlowView
import cn.coegle18.smallsteps.entity.DailyBalance

@Dao
interface ChartDao {
    @Query("SELECT a.date, sum(b.income) - sum(b.expense) AS balance FROM CashFlowView a, CashFlowView b WHERE a.date >= b.date GROUP BY a.date HAVING a.date >= date(\"now\", :beginDate) AND a.date <= date(\"now\")")
    fun queryTrend(beginDate: String): LiveData<List<DailyBalance>>

    //
    @Query("SELECT * From CashFlowView c Where c.date >= date(\"now\", :beginDate) AND c.date <= date(\"now\") ORDER BY date ASC")
    fun queryCashFlow(beginDate: String): LiveData<List<CashFlowView>>

    // 获取该月的消费金额
    @Query("SELECT sum(expense) From CashFlowView c Where c.date >= date(\"now\", \"start of month\") AND c.date <= date(\"now\") ORDER BY date ASC")
    fun queryExpenseThisMonth(): LiveData<Double>
}