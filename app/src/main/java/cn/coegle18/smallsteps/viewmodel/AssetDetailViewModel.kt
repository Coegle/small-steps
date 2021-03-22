package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.adapter.MonthHeader
import cn.coegle18.smallsteps.adapter.MonthRootNode
import cn.coegle18.smallsteps.adapter.MonthSubNode
import cn.coegle18.smallsteps.entity.BillView
import com.chad.library.adapter.base.entity.node.BaseNode
import java.time.format.DateTimeFormatter


class AssetDetailViewModel(application: Application, accountId: Long) : AndroidViewModel(application) {
    private val billDao = AppDatabase.getDatabase(application).billDao()

    private val monthSummaryList = billDao.queryExpenseByMonth(accountId)
    private val billList = billDao.queryBillList(listOf(accountId))

    val displayDataList = MediatorLiveData<List<BaseNode>>()

    init {
        displayDataList.addSource(billList) {
            displayDataList.value = getDisplayDataList(monthSummaryList, billList)
        }
        displayDataList.addSource(monthSummaryList) {
            displayDataList.value = getDisplayDataList(monthSummaryList, billList)
        }
    }


    private fun getDisplayDataList(
            monthSummaryListLD: LiveData<List<MonthHeader>>,
            billListLD: LiveData<List<BillView>>
    ): List<BaseNode> {
        val headerList = monthSummaryListLD.value
        val billList = billListLD.value
        if (billList == null || headerList == null) return emptyList()
        Log.d("data headerList", headerList.toString())
        val retList = mutableListOf<BaseNode>()
        val map = billList.groupBy {
            it.date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        }
        for (header in headerList) {
            val monthString = "${header.year}-${header.month}"
            Log.d("data, header", header.toString())
            Log.d("data, child", map[monthString]?.map { MonthSubNode(it) }.toString())
            retList.add(MonthRootNode(
                    header,
                    ((map[monthString]?.map { MonthSubNode(it) }
                            ?: mutableListOf()) as MutableList<BaseNode>)))
        }
        Log.d("data return", retList.toString())
        return retList
    }
}

class DetailViewModelFactory(private val application: Application, private val accountId: Long) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AssetDetailViewModel(application, accountId) as T
    }
}