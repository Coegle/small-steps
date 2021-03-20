package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.Visible
import cn.coegle18.smallsteps.entity.CategoryView

class SubCategoryDialogViewModel(application: Application) : AndroidViewModel(application) {
    private val categoryDao = AppDatabase.getDatabase(application).categoryDao()
    var pCategoryView = MutableLiveData<CategoryView>()
    val subCategoryList = Transformations.switchMap(pCategoryView) { categoryDao.querySubCategoryViewWithParent(mutableListOf(Visible.ENABLED), it.id) }
}