package cn.coegle18.smallsteps.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cn.coegle18.smallsteps.Visible
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

    // 查询指定分类的子分类（0 即查询母分类）
    @Query("Select * from Category where parentId = :parentId")
    fun querySubCategory(parentId: Int): List<Category>
}