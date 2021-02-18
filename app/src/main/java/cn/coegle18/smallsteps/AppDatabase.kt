package cn.coegle18.smallsteps

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import cn.coegle18.smallsteps.dao.*
import cn.coegle18.smallsteps.entity.*

@Database(
    version = 1,
    entities = [Account::class, AccountType::class, Category::class, Bill::class, RelationOfBills::class],
    views = [BillList::class]
)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun accountTypeDao(): AccountTypeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun billDao(): BillDao
    abstract fun relationOfBills(): RelationOfBillsDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "app_database"
            )
                .createFromAsset("app_database_v1.db")
                .fallbackToDestructiveMigration()
                .build().apply {
                    instance = this
                }
        }
    }
}