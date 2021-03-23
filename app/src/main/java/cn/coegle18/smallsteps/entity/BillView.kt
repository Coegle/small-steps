package cn.coegle18.smallsteps.entity

import android.os.Parcelable
import androidx.room.DatabaseView
import cn.coegle18.smallsteps.*
import kotlinx.android.parcel.Parcelize
import java.time.OffsetDateTime

@DatabaseView(
        "select distinct " +
                "c.pId categoryPId, " +
                "c.pName categoryPName, " +
                "c.pIcon categoryPIcon, " +
                "c.cId categoryCId, " +
                "c.cName categoryCName, " +
                "c.cIcon categoryCIcon, " +
                "inA.accountId inAccountId, " +
                "inA.name inAccountName, " +
                "inA.mainAccountType inAccountMainAccountType, " +
                "inA.icon inAccountIcon, " +
                "outA.accountId outAccountId, " +
                "outA.name outAccountName," +
                "outA.mainAccountType outAccountMainAccountType, " +
                "outA.icon outAccountIcon, " +
                "c.tradeType tradeType, " +
                "c.displayTradeType displayTradeType, " +
                "b.remark remark, " +
                "outMoney, " +
                "inMoney, " +
                "b.visible, " +
                "date, " +
                "source, " +
                "expense, " +
                "income, " +
                "billId, " +
                "sm.relation splitFlag, " +
                "rfr.relation refundFlag, " +
                "rbr.relation reimbursementFlag " +
                "from bill as b " +
                "left join categoryView as c on c.id = b.category " +
                "left join accountView as inA on inA.accountId = inAccount " +
                "left join accountView as outA on outA.accountId = outAccount " +
                "left join relationOfBills as sm on (billId = sm.mainBill or billId = sm.relatedBill) and sm.relation = \"SPLIT\"" +
                "left outer join relationOfBills as rfr on (billId = rfr.mainBill or billId = rfr.relatedBill) and rfr.relation = \"REFUND\" " +
                "left outer join relationOfBills as rbr on (billId = rbr.mainBill or billId = rbr.relatedBill) and rbr.relation = \"REIMBURSEMENT\""
)
@Parcelize
data class BillView(
        val categoryPId: Long, // 用于筛选
        val categoryPName: String, // 用于显示
        val categoryPIcon: String, // 用于显示
        val categoryCId: Long?, // 用于报表页面的筛选
        val categoryCName: String?, // 用于显示
        val categoryCIcon: String?, // 用于显示
        val inAccountId: Long?,
        val inAccountName: String?,
        val inAccountMainAccountType: MainAccountType?,
        val inAccountIcon: String?,
        val outAccountId: Long?,
        val outAccountName: String?,
        val outAccountMainAccountType: MainAccountType?,
        val outAccountIcon: String?,
        val tradeType: TradeType,
        val displayTradeType: TradeType,
        val remark: String,
        val outMoney: Double?,
        val inMoney: Double?,
        val visible: Visible,
        val date: OffsetDateTime,
        val source: Source,
        val expense: Double,
        val income: Double,
        val billId: Long,
        val splitFlag: Relation?,
        val refundFlag: Relation?,
        val reimbursementFlag: Relation?
) : Parcelable {
        fun toBill(): Bill {
                val bill = Bill(
                        date,
                        categoryCId ?: categoryPId,
                        null,
                        remark,
                        outMoney,
                        outAccountId,
                        inMoney,
                        inAccountId,
                        expense,
                        income,
                        source,
                        visible
                )
                bill.billId = billId
                return bill
        }
}