package cn.coegle18.smallsteps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.Constants
import java.time.OffsetDateTime
import java.util.*

class TransBillViewModel(application: Application) : AndroidViewModel(application) {
    private val accountDao = AppDatabase.getDatabase(application).accountDao()
    val billDao = AppDatabase.getDatabase(application).billDao()

    val categoryId = Constants.defaultTransferCategoryId

    // 时间
    var newDateTime: OffsetDateTime = OffsetDateTime.ofInstant(Calendar.getInstance().toInstant(), TimeZone.getDefault().toZoneId())

    // 账户
    var inAccountId = MutableLiveData(Constants.defaultAccountId)
    var inAccountView = Transformations.switchMap(inAccountId) { accountDao.queryAccountView(it) }

    var outAccountId = MutableLiveData(Constants.defaultAccountId)
    var outAccountView = Transformations.switchMap(outAccountId) { accountDao.queryAccountView(it) }

    // 金额
    var money = MutableLiveData(0.0)
}