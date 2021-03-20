package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.PrimaryAccountType
import cn.coegle18.smallsteps.entity.AccountType

class SubAddAssetsViewModel(application: Application, primaryAccountType: PrimaryAccountType) : AndroidViewModel(application) {
    val primaryAccountType: PrimaryAccountType = primaryAccountType
    private val accountTypeDao = AppDatabase.getDatabase(application).accountTypeDao()
    val accountTypeList: LiveData<List<AccountType>> = accountTypeDao.queryPrimaryAccountType(primaryAccountType)

}

class SubAddAssetsViewModelFactory(private val application: Application, val primaryAccountType: PrimaryAccountType) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SubAddAssetsViewModel(application, primaryAccountType) as T
    }
}