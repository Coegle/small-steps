package cn.coegle18.smallsteps.util

import android.os.Bundle
import android.os.Handler
import android.os.Message
import cn.coegle18.smallsteps.States
import cn.coegle18.smallsteps.dialog.LATEST_TIME
import cn.coegle18.smallsteps.entity.NJUSTBill
import java.time.OffsetDateTime

class MessageUtil(private val handler: Handler) {
    fun sendMessage(state: States, bill: NJUSTBill? = null, newDate: OffsetDateTime? = null) {
        val message = Message()
        message.what = state.ordinal
        bill?.let {
            val bundle = Bundle()
            bundle.putParcelable(BILL, it)
            message.data = bundle
        }
        newDate?.let {
            val bundle = Bundle()
            bundle.putString(LATEST_TIME, it.toString())
            message.data = bundle
        }
        handler.sendMessage(message)
    }
}