package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.Constants
import cn.coegle18.smallsteps.fragment.BUDGET

class OverviewViewModel(application: Application) : AndroidViewModel(application) {
    private val chartDao = AppDatabase.getDatabase(application).chartDao()

    var trendClickTime = MutableLiveData(0)
    var cashFlowClickTime = MutableLiveData(0)

    private val totalExpenseThisMonth = chartDao.queryExpenseThisMonth()
    val budget = MutableLiveData(application.getSharedPreferences("data", Context.MODE_PRIVATE).getInt(BUDGET, Constants.defaultBudget))

    val leftExpense = MediatorLiveData<Double>()
    val percent = MediatorLiveData<Double>()

    init {
        leftExpense.apply {
            addSource(totalExpenseThisMonth) {
                leftExpense.value = getLeft(budget.value, totalExpenseThisMonth.value)
            }
            addSource(budget) {
                leftExpense.value = getLeft(budget.value, totalExpenseThisMonth.value)
            }
        }
        percent.apply {
            addSource(totalExpenseThisMonth) {
                percent.value = getPercent(budget.value, totalExpenseThisMonth.value)
            }
            addSource(budget) {
                percent.value = getPercent(budget.value, totalExpenseThisMonth.value)
            }
        }
    }

    private val trendSelectionList = listOf(Pair("-7 days", "最近7天"), Pair("-30 days", "最近30天"), Pair("-365 days", "最近1年"))
    val trendBtnText = Transformations.switchMap(trendClickTime) {
        MutableLiveData(trendSelectionList[it].second)
    }
    val trendList = Transformations.switchMap(trendClickTime) {
        chartDao.queryTrend(trendSelectionList[it].first)
    }

    private val cashFlowSelectionList = listOf(Pair("-7 days", "最近7天"), Pair("start of month", "这个月"))
    val cashFlowBtnText = Transformations.switchMap(cashFlowClickTime) {
        MutableLiveData(cashFlowSelectionList[it].second)
    }
    val cashFlowList = Transformations.switchMap(cashFlowClickTime) {
        chartDao.queryCashFlow(cashFlowSelectionList[it].first)
    }


    fun trendPlusOne() {
        val newValue = trendClickTime.value ?: 0
        trendClickTime.value = (newValue + 1) % 3
    }

    fun cashFlowPlusOne() {
        val newValue = cashFlowClickTime.value ?: 0
        cashFlowClickTime.value = (newValue + 1) % 2
    }

    private fun getLeft(budget: Int?, spend: Double?): Double? {
        if (budget == null || spend == null) return null
        return budget - spend
    }

    private fun getPercent(budget: Int?, spend: Double?): Double? {
        if (budget == null || spend == null) return null
        return spend / budget * 100
    }
}