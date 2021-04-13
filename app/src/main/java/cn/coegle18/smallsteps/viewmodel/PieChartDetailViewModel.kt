package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cn.coegle18.smallsteps.AppDatabase

class PieChartDetailViewModel(application: Application, categoryId: Long, orderByDateParam: Boolean) : AndroidViewModel(application) {
    private val billDao = AppDatabase.getDatabase(application).billDao()
    var orderByDate = MutableLiveData(orderByDateParam)
    val billList = Transformations.switchMap(orderByDate) {
        if (it) {
            billDao.queryBillListByDate(categoryId)
        } else {
            billDao.queryBillListByMoney(categoryId)
        }
    }
}

class PieChartDetailViewModelFactory(private val application: Application, private val categoryId: Long, private val orderByDateParam: Boolean = true) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PieChartDetailViewModel(application, categoryId, orderByDateParam) as T
    }
}