package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.Visible
import cn.coegle18.smallsteps.adapter.RootNode
import cn.coegle18.smallsteps.adapter.SubNode
import cn.coegle18.smallsteps.entity.Category
import com.chad.library.adapter.base.entity.node.BaseNode

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val categoryDao = AppDatabase.getDatabase(application).categoryDao()
    val displayTradeType: MutableLiveData<TradeType> = MutableLiveData(TradeType.EXPENSE)
    private var categoryList = Transformations.switchMap(displayTradeType) {
        categoryDao.queryCategoryOfDisplayTradeType(it, mutableListOf(Visible.ENABLED, Visible.DEACTIVATED))
    }
    var displayDataList = Transformations.switchMap(categoryList) { group(it) }

    private fun group(dataList: List<Category>): MutableLiveData<MutableList<BaseNode>> {
        val ret: MutableLiveData<MutableList<BaseNode>> = MutableLiveData(mutableListOf())
        val pCategories = dataList.filter { it.parentId == 0L }
        for (pCategory in pCategories) {
            val subNodeList: MutableList<BaseNode> = mutableListOf()
            val cCategories = dataList.filter { it.parentId == pCategory.categoryId }
            for (cCategory in cCategories) {
                subNodeList.add(SubNode(cCategory))
            }
            if (pCategory.tradeType != TradeType.TRANSFER) subNodeList.add(SubNode(null))
            val entity = RootNode(subNodeList, pCategory)
            entity.isExpanded = false
            ret.value?.add(entity)
        }
        Log.d("viewModel", "displayDataList: ${ret.value.toString()}")
        return ret
    }

    fun changeTradeType(newTradeType: TradeType) {
        displayTradeType.value = newTradeType
        Log.d("viewModel", "displayTradeType: ${displayTradeType.value}")
        Log.d("viewModel", "categoryList: ${categoryList.value}")
    }

}