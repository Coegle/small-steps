package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cn.coegle18.smallsteps.AppDatabase

class BillsViewModel(application: Application) : AndroidViewModel(application) {
    private val billDao = AppDatabase.getDatabase(application).billDao()
    val billList = billDao.simpleQuery()
}