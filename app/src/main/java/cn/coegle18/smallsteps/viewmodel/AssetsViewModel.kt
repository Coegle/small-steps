package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.PrimaryAccountType
import cn.coegle18.smallsteps.entity.AccountSection
import cn.coegle18.smallsteps.entity.AccountView


class AssetsViewModel(application: Application) : AndroidViewModel(application) {

    private val accountDao = AppDatabase.getDatabase(application).accountDao()
    val allVisibleAccountList: LiveData<List<AccountView>> = accountDao.queryAccountViewList()
    var accountSection: List<AccountSection> = emptyList<AccountSection>().toMutableList()

    // 更新视图
    fun group(list: List<AccountView>) {
        accountSection = emptyList<AccountSection>().toMutableList()
        for (type in PrimaryAccountType.values()) {

            val tmp = list.filter { it.primaryAccountType == type }
            if (tmp.isNotEmpty()) {
                // 添加头部
                accountSection = accountSection + AccountSection(true, (type.caption + "账户") as Any)
                // 添加列表
                accountSection = accountSection + tmp.map { AccountSection(false, it as Any) }
            }

        }
    }
}