package cn.coegle18.smallsteps.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.Visible
import cn.coegle18.smallsteps.entity.BillOfCategory
import cn.coegle18.smallsteps.entity.Category
import cn.coegle18.smallsteps.entity.CategoryView

@Dao
interface CategoryDao {
    @Update
    fun updateCategory(category: Category)

    @Insert
    fun insertCategory(category: Category): Long

    // 查询指定状态的母分类
    @Query("Select * from Category where visible in (:visible) and displayTradeType = :displayTradeType and parentId = 0")
    fun queryPCategory(visible: List<Visible>, displayTradeType: TradeType): LiveData<List<Category>>

    // 查询指定状态的母分类视图
    @Query("Select * from CategoryView where visible in (:visible) and displayTradeType = :displayTradeType and id = pId")
    fun queryPCategoryView(visible: List<Visible>, displayTradeType: TradeType): LiveData<List<CategoryView>>

    // 查询指定状态的所有分类视图
    @Query("Select * from CategoryView where visible in (:visible) and displayTradeType in(:displayTradeType)")
    fun queryFullCategoryViewList(visible: List<Visible> = listOf(Visible.ENABLED, Visible.SYSTEM), displayTradeType: List<TradeType> = listOf(TradeType.INCOME, TradeType.EXPENSE)): LiveData<List<CategoryView>>

    // 查询指定状态的子分类
    @Query("Select * from Category where visible in (:visible) and parentId = :parentId")
    fun querySubCategory(visible: List<Visible>, parentId: Long): LiveData<List<Category>>

    // 查询指定状态的子分类视图
    @Query("Select * from CategoryView where visible in (:visible) and pId = :parentId and id != :parentId")
    fun querySubCategoryView(visible: List<Visible>, parentId: Long): LiveData<List<CategoryView>>

    // 查询指定状态的子分类视图(包含母分类)
    @Query("Select * from CategoryView where visible in (:visible) and pId = :parentId")
    fun querySubCategoryViewWithParent(visible: List<Visible>, parentId: Long): LiveData<List<CategoryView>>

    // 查询指定子分类的账单
    @Transaction
    @Query("Select * from Category where categoryId = :category")
    fun queryBillsOfSubCategory(category: Long): BillOfCategory

    // 查询指定显示交易类型的账单
    @Transaction
    @Query("Select * from Category where displayTradeType = :displayTradeType")
    fun queryBillsOfDisplayTradeType(displayTradeType: TradeType): BillOfCategory

    // 查询指定显示交易类型的分类
    @Query("Select * from category where displayTradeType = :displayTradeType and visible in (:visible)")
    fun queryCategoryOfDisplayTradeType(displayTradeType: TradeType, visible: List<Visible>): LiveData<List<Category>>

    // 修改某母分类下的所有子分类的可见状态
    @Query("Update Category Set visible = :newVisible where parentId =:pId and visible = :oldVisible")
    fun updateVisibilityOfSubCategory(pId: Long, oldVisible: Visible, newVisible: Visible)

    // 查询指定的分类
    @Query("Select * from CategoryView where id = :id")
    fun querySingleCategoryView(id: Long): LiveData<CategoryView>
}