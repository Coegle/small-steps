package cn.coegle18.smallsteps.dao

import androidx.room.*
import cn.coegle18.smallsteps.Visible
import cn.coegle18.smallsteps.entity.Bill
import cn.coegle18.smallsteps.entity.BillOfCategory
import cn.coegle18.smallsteps.entity.Category

@Dao
interface CategoryDao {
    @Update
    fun updateCategory(category: Category)

    @Insert
    fun insertCategory(category: Category): Long

    // 查询指定状态的分类
    @Query("Select * from Category where visible = :visible")
    fun queryCategory(visible: Visible): List<Category>

    // 查询指定分类的子分类（0 即查询所有母分类）
    @Query("Select * from Category where parentId = :parentId")
    fun querySubCategory(parentId: Long): List<Category>

    // 查询指定子分类的账单
    @Transaction
    @Query("Select * from Category where categoryId = :category")
    fun queryBillsOfSubCategory(category: Long): BillOfCategory

    // 查询指定母分类的账单
    fun queryBillsOfCategory(parentCategory: Long): List<Bill> {
        val categoryList = querySubCategory(parentCategory)
        val bills = emptyList<Bill>().toMutableList()
        for (category in categoryList) {
            bills += queryBillsOfSubCategory(category.categoryId).bills
        }
        return bills
    }
}