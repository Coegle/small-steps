package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.TradeType

class PieChartViewModel(application: Application) : AndroidViewModel(application) {
    private val billDao = AppDatabase.getDatabase(application).billDao()
    var tradeType = MutableLiveData(TradeType.EXPENSE)
    val pieChartDataList = Transformations.switchMap(tradeType) {
        billDao.queryPPieChart(it)
    }
}