package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import cn.coegle18.smallsteps.*
import cn.coegle18.smallsteps.adapter.CategoryWithDefaultSelection
import cn.coegle18.smallsteps.entity.Bill
import cn.coegle18.smallsteps.entity.BillView
import cn.coegle18.smallsteps.entity.CategoryView
import cn.coegle18.smallsteps.entity.RelationOfBills
import cn.coegle18.smallsteps.util.Util
import java.time.OffsetDateTime
import java.util.*
import kotlin.concurrent.thread

class IncomeBillViewModel(application: Application) : AndroidViewModel(application) {
    private val categoryDao = AppDatabase.getDatabase(application).categoryDao()
    val accountDao = AppDatabase.getDatabase(application).accountDao()
    val billDao = AppDatabase.getDatabase(application).billDao()

    val categoryList = categoryDao.queryPCategoryView(mutableListOf(Visible.ENABLED), TradeType.INCOME)
    val displayList = MediatorLiveData<List<CategoryWithDefaultSelection>>()

    // 分类
    var selectedCategoryId = MutableLiveData(Constants.defaultCategoryMap[TradeType.INCOME]!!)
    var categoryView = Transformations.switchMap(selectedCategoryId) { categoryDao.querySingleCategoryView(it) }

    //    val relation = Transformations.switchMap(categoryView) {
//        Log.d("relation", "relation: $it")
//        val relation = when (it.relatedAccountType) {
//            MainAccountType.REIMBURSEMENT -> Relation.REIMBURSEMENT
//            MainAccountType.REFUND-> Relation.REFUND
//            else -> null
//        }
//        MutableLiveData<Relation?>(relation)
//    }
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
    var relatedAccount = Transformations.switchMap(relatedAccountId) { accountDao.queryAccountView(it) }

    // 时间
    var newDateTime: OffsetDateTime = OffsetDateTime.ofInstant(Calendar.getInstance().toInstant(), TimeZone.getDefault().toZoneId())

    // 关联账单
    val mainBillId = MutableLiveData(0L)
    val oldRelatedBill = Transformations.switchMap(mainBillId) {
        billDao.queryRelatedBill(it)
    }
    val newRelatedBillId = MutableLiveData(0L)
    val relatedBillView = Transformations.switchMap(newRelatedBillId) {
        billDao.queryBill(it)
    }

    fun addBill(newBill: Bill) {
        thread {
            val relation = when (categoryView.value?.relatedAccountType) {
                MainAccountType.REIMBURSEMENT -> Relation.REIMBURSEMENT
                MainAccountType.REFUND -> Relation.REFUND
                else -> null
            }
            // 添加账单
            newBill.billId = billDao.insertBill(newBill)
            Util.adjustBalance(null, newBill, accountDao)
            if (relatedBillView.value != null && relation != null) { // 有关联账单
                Log.d("state", "有关联账单")
                addRelation(newBill, relatedBillView.value!!.toBill(), relation)
            }
        }
    }

    fun deleteBill(bill: Bill) {
        thread {
            billDao.deleteBill(bill.billId)
            Util.adjustBalance(bill, null, accountDao)
        }
    }

    private fun deleteRelation(mainBillId: Long, relatedBill: Bill) {
        Log.d("relation", "需要删除关联")
        Log.d("relation", "mainBill, $mainBillId")
        Log.d("relation", "mainBill, $relatedBill")
        billDao.deleteRelation(mainBillId, relatedBill.billId)
        // 更新被关联的账单
        val newRelatedBill = Util.changeDirection(relatedBill, null, null, TradeType.EXPENSE, true)
        billDao.updateBill(newRelatedBill)
        // 调整因为被关联账单产生的变化
        Util.adjustBalance(relatedBill, newRelatedBill, accountDao)
    }

    private fun addRelation(mainBill: Bill, relatedBill: Bill, relation: Relation) {
        Log.d("relation", "需要添加关联")
        Log.d("relation", "mainBill, $mainBill")
        Log.d("relation", "relatedBill, $relatedBill, ID: ${relatedBill.billId}")
        // 更新被关联的账单
        val newRelatedBill = Util.changeDirection(relatedBill, mainBill.outMoney, mainBill.outAccount, TradeType.TRANSFER, true)
        Log.d("relation", "newRelatedBill: $newRelatedBill,ID: ${newRelatedBill.billId}")
        billDao.updateBill(newRelatedBill)
        // 添加关系
        val relationOfBills = RelationOfBills(relation, mainBill.billId, relatedBill.billId)
        billDao.insertRelation(relationOfBills)
        // 调整余额
        Log.d("**********", relatedBill.toString())
        Util.adjustBalance(relatedBill, newRelatedBill, accountDao)
    }

    fun modifyBill(newBill: Bill, oldBill: BillView) {
        thread {
            billDao.updateBill(newBill)
            Util.adjustBalance(oldBill.toBill(), newBill, accountDao)
            // 账单之间的关系
            val newRelation = when (categoryView.value?.relatedAccountType) {
                MainAccountType.REIMBURSEMENT -> Relation.REIMBURSEMENT
                MainAccountType.REFUND -> Relation.REFUND
                else -> null
            }
            if (oldRelatedBill.value != null) { // 之前有
                if (relatedBillView.value == null) { // 需要删除
                    deleteRelation(newBill.billId, oldRelatedBill.value!!.toBill())
                } else if (oldRelatedBill.value?.billId != newRelatedBillId.value) { // 修改被关联的账单
                    // 删除旧的关系
                    deleteRelation(newBill.billId, oldRelatedBill.value!!.toBill())
                    // 添加新的关系
                    addRelation(newBill, relatedBillView.value!!.toBill(), newRelation!!)
                }
            } else if (relatedBillView.value != null) { // 新增
                addRelation(newBill, relatedBillView.value!!.toBill(), newRelation!!)
            }
        }
    }


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