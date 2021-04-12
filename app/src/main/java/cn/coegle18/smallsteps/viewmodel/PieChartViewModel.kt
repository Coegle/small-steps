package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.TradeType

class PieChartViewModel(application: Application) : AndroidViewModel(application) {
    private val billDao = AppDatabase.getDatabase(application).billDao()
    val pieChartDataList = billDao.queryPPieChart(TradeType.EXPENSE)
}