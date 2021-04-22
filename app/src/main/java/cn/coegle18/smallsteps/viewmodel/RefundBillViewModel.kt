package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cn.coegle18.smallsteps.AppDatabase

class RefundBillViewModel(application: Application, balance: String) : AndroidViewModel(application) {
    private val billDao = AppDatabase.getDatabase(application).billDao()
    val billList = billDao.queryRefundBillList(balance)
}

class RefundBillViewModelFactory(private val application: Application, private val balance: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RefundBillViewModel(application, balance) as T
    }
}