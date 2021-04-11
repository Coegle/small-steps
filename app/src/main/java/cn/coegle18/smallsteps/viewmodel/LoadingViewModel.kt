package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.entity.AccountView
import cn.coegle18.smallsteps.entity.CategoryView
import cn.coegle18.smallsteps.entity.NJUSTBill
import cn.coegle18.smallsteps.entity.SimpleBill
import cn.coegle18.smallsteps.util.SmartCastUtil

class LoadingViewModel(application: Application, private val accountId: Long) : AndroidViewModel(application) {
    val accountDao = AppDatabase.getDatabase(application).accountDao()
    private val categoryDao = AppDatabase.getDatabase(application).categoryDao()
    val billDao = AppDatabase.getDatabase(application).billDao()
    val accountList = accountDao.queryAccountViewList()
    private val categoryList = categoryDao.queryFullCategoryViewList()
    val scrapedDataList = MutableLiveData<List<NJUSTBill>>()
    val simpleBillList = MediatorLiveData<List<SimpleBill>>()

    init {
        simpleBillList.addSource(accountList) {
            simpleBillList.value = smartCastToSimpleBillList(accountList.value, categoryList.value, scrapedDataList.value)
        }
        simpleBillList.addSource(categoryList) {
            simpleBillList.value = smartCastToSimpleBillList(accountList.value, categoryList.value, scrapedDataList.value)
        }
        simpleBillList.addSource(scrapedDataList) {
            simpleBillList.value = smartCastToSimpleBillList(accountList.value, categoryList.value, scrapedDataList.value)
        }
    }

    private fun smartCastToSimpleBillList(accountList: List<AccountView>?, categoryList: List<CategoryView>?, njustBillList: List<NJUSTBill>?): List<SimpleBill> {
        if (accountList == null || categoryList == null || njustBillList == null) return emptyList()
        val simpleBillList = mutableListOf<SimpleBill>()
        for (bill in njustBillList) {
            simpleBillList.add(SmartCastUtil.toSimpleBill(bill, accountList, categoryList, accountId))
        }
        return simpleBillList
    }
}

class LoadingViewModelFactory(
        private val application: Application,
        private val accountId: Long,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoadingViewModel(application, accountId) as T
    }
}