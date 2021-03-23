package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.entity.AccountType

class AddAssetsSecondViewModel(application: Application, pAccountType: AccountType) : AndroidViewModel(application) {
    private val accountTypeDao = AppDatabase.getDatabase(application).accountTypeDao()
    val accountTypeList: LiveData<List<AccountType>> = accountTypeDao.queryFinalAccountType(pAccountType.accountTypeId)

}

class AddAssetsSecondViewModelFactory(private val application: Application, private val pAccountType: AccountType) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddAssetsSecondViewModel(application, pAccountType) as T
    }
}