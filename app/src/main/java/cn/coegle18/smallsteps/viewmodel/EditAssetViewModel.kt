package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.entity.AccountTypeView
import cn.coegle18.smallsteps.entity.AccountView

class EditAssetViewModel(application: Application, accountId: Long, accountTypeId: Long) :
    AndroidViewModel(application) {
    val accountDao = AppDatabase.getDatabase(application).accountDao()
    val billDao = AppDatabase.getDatabase(application).billDao()

    val accountView = accountDao.queryAccountView(accountId)
    private val accountTypeView = accountDao.queryAccountTypeView(accountTypeId)

    val displayData = MediatorLiveData<AccountTypeView>()

    init {
        displayData.addSource(accountView) {
            displayData.value = group(accountView.value, accountTypeView.value)
        }
        displayData.addSource(accountTypeView) {
            displayData.value = group(accountView.value, accountTypeView.value)
        }
    }

    fun group(accountView: AccountView?, accountTypeView: AccountTypeView?): AccountTypeView? {
        if (accountTypeView != null && accountView?.custom == true) { // 旧账户
            accountTypeView.cName = accountView.name
        }
        return accountTypeView
    }
}

class EditAssetViewModelFactory(
    private val application: Application,
    private val accountId: Long,
    private val accountTypeId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditAssetViewModel(application, accountId, accountTypeId) as T
    }
}