package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.MainAccountType

class AccountDialogViewModel(application: Application) : AndroidViewModel(application) {
    private val accountDao = AppDatabase.getDatabase(application).accountDao()
    var accountType = MutableLiveData<List<MainAccountType>>()
    val accountList = Transformations.switchMap(accountType) { accountDao.queryAccountViewList(it) }
}