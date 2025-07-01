package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 分类数据访问对象
 * 提供分类相关的数据库操作
 */
@Dao
interface CategoryDao {

    /**
     * 获取所有分类
     */
    @Query("SELECT * FROM categories ORDER BY type, name")
    fun getAllCategories(): Flow<List<Category>>

    /**
     * 根据类型获取分类
     */
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name")
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>

    /**
     * 根据ID获取分类
     */
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    /**
     * 插入分类
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    /**
     * 批量插入分类
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    /**
     * 更新分类
     */
    @Update
    suspend fun updateCategory(category: Category)

    /**
     * 删除分类
     */
    @Delete
    suspend fun deleteCategory(category: Category)

    /**
     * 检查是否存在默认分类
     */
    @Query("SELECT COUNT(*) FROM categories WHERE isDefault = 1")
    suspend fun getDefaultCategoriesCount(): Int

    /**
     * 搜索分类
     */
    @Query("SELECT * FROM categories WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun searchCategories(query: String): Flow<List<Category>>
}
