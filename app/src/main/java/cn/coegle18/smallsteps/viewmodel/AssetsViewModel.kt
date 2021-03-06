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

    // ????????????
    fun group(list: List<AccountView>) {
        accountSection = emptyList<AccountSection>().toMutableList()
        for (type in PrimaryAccountType.values()) {

            val tmp = list.filter { it.primaryAccountType == type }
            if (tmp.isNotEmpty()) {
                // ????????????
                accountSection = accountSection + AccountSection(true, (type.caption + "??????") as Any)
                // ????????????
                accountSection = accountSection + tmp.map { AccountSection(false, it as Any) }
            }

        }
    }

    // ????????????????????????
    private fun generateAssetsList(currency: Double?, fund: Double?, lend: Double?, liability: Double?): Array<AASeriesElement> {
        if (currency == null || fund == null || lend == null || liability == null) return arrayOf()
        val currencyList = AASeriesElement()
                .data(arrayOf(currency))
                .name("??????")
                .color("#27AE60")
        val fundList = AASeriesElement()
                .data(arrayOf(fund))
                .name("??????")
                .color("#2F80ED")
        val lendList = AASeriesElement()
                .data(arrayOf(lend))
                .name("??????")
                .color("#EB5757")
        val liabilityList = AASeriesElement()
                .data(arrayOf(liability))
                .name("??????")
                .color("#828282")
        return arrayOf(liabilityList, lendList, fundList, currencyList)
    }
}