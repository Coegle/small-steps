package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.*
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.Constants
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.Visible
import cn.coegle18.smallsteps.adapter.CategoryWithDefaultSelection
import cn.coegle18.smallsteps.entity.CategoryView
import java.time.OffsetDateTime
import java.util.*

class ExpenseBillViewModel(application: Application) : AndroidViewModel(application) {
    private val categoryDao = AppDatabase.getDatabase(application).categoryDao()
    val accountDao = AppDatabase.getDatabase(application).accountDao()
    val billDao = AppDatabase.getDatabase(application).billDao()

    val categoryList = categoryDao.queryPCategoryView(mutableListOf(Visible.ENABLED), TradeType.EXPENSE)
    val displayList = MediatorLiveData<List<CategoryWithDefaultSelection>>()

    // 分类
    var selectedCategoryId = MutableLiveData(Constants.defaultCategoryMap[TradeType.EXPENSE]!!)
    var categoryView =
        Transformations.switchMap(selectedCategoryId) { categoryDao.querySingleCategoryView(it) }

    init {
        displayList.addSource(categoryList) {
            displayList.value = setDisplayList(categoryList, categoryView)
        }
        displayList.addSource(categoryView) {
            displayList.value = setDisplayList(categoryList, categoryView)
        }
    }

    // 账户
    var accountId = MutableLiveData(Constants.defaultAccountId)
    var accountView = Transformations.switchMap(accountId) { accountDao.queryAccountView(it) }

    // 关联的账户
    var relatedAccountId = MutableLiveData(0L)
    var relatedAccount =
        Transformations.switchMap(relatedAccountId) { accountDao.queryAccountView(it) }

    // 时间
    var newDateTime: OffsetDateTime = OffsetDateTime.ofInstant(Calendar.getInstance().toInstant(), TimeZone.getDefault().toZoneId())

    private fun setDisplayList(categoryListLD: LiveData<List<CategoryView>>, selectedCategoryLD: LiveData<CategoryView>): List<CategoryWithDefaultSelection> {
        val categoryList = categoryListLD.value
        val selectedCategory = selectedCategoryLD.value
        if (categoryList == null || selectedCategory == null) return emptyList()
        return categoryList.map {
            if (it.id == selectedCategory.id || it.id == selectedCategory.pId) CategoryWithDefaultSelection(selectedCategory, true)
            else CategoryWithDefaultSelection(it, false)
        }
    }
}