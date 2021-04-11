package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.PrimaryAccountType
import cn.coegle18.smallsteps.entity.AccountSection
import cn.coegle18.smallsteps.entity.AccountView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement


class AssetsViewModel(application: Application) : AndroidViewModel(application) {

    private val accountDao = AppDatabase.getDatabase(application).accountDao()
    val allVisibleAccountList: LiveData<List<AccountView>> = accountDao.queryAccountViewList()
    var accountSection: List<AccountSection> = emptyList<AccountSection>().toMutableList()
    val currencyMoney = accountDao.queryTotalBalanceOfPrimaryAccountType(PrimaryAccountType.MONEY)
    val fundMoney = accountDao.queryTotalBalanceOfPrimaryAccountType(PrimaryAccountType.INVESTMENT)
    val lendMoney = accountDao.queryTotalBalanceOfPrimaryAccountType(PrimaryAccountType.RECEIVABLE)
    val liabilityMoney = accountDao.queryTotalBalanceOfPrimaryAccountType(PrimaryAccountType.PAYABLE)
    val assetsMoneyList = MediatorLiveData<Array<AASeriesElement>>()

    init {
        assetsMoneyList.addSource(currencyMoney) {
            assetsMoneyList.value = generateAssetsList(currencyMoney.value, fundMoney.value, lendMoney.value, liabilityMoney.value)
        }
        assetsMoneyList.addSource(fundMoney) {
            assetsMoneyList.value = generateAssetsList(currencyMoney.value, fundMoney.value, lendMoney.value, liabilityMoney.value)
        }
        assetsMoneyList.addSource(lendMoney) {
            assetsMoneyList.value = generateAssetsList(currencyMoney.value, fundMoney.value, lendMoney.value, liabilityMoney.value)
        }
        assetsMoneyList.addSource(liabilityMoney) {
            assetsMoneyList.value = generateAssetsList(currencyMoney.value, fundMoney.value, lendMoney.value, liabilityMoney.value)
        }
    }

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

    // 生成资产组成数据
    private fun generateAssetsList(currency: Double?, fund: Double?, lend: Double?, liability: Double?): Array<AASeriesElement> {
        if (currency == null || fund == null || lend == null || liability == null) return arrayOf()
        val currencyList = AASeriesElement()
                .data(arrayOf(currency))
                .name("现金")
                .color("#27AE60")
        val fundList = AASeriesElement()
                .data(arrayOf(fund))
                .name("投资")
                .color("#2F80ED")
        val lendList = AASeriesElement()
                .data(arrayOf(lend))
                .name("借出")
                .color("#EB5757")
        val liabilityList = AASeriesElement()
                .data(arrayOf(liability))
                .name("负债")
                .color("#828282")
        return arrayOf(liabilityList, lendList, fundList, currencyList)
    }
}