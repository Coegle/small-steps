package cn.coegle18.smallsteps.entity

import androidx.room.DatabaseView
import cn.coegle18.smallsteps.DisplayTradeType
import cn.coegle18.smallsteps.Relation
import cn.coegle18.smallsteps.Source
import cn.coegle18.smallsteps.TradeType
import java.time.OffsetDateTime

@DatabaseView(
        "select distinct " +
                "p.categoryId categoryPId, " +
                "p.name categoryPName, " +
                "p.icon categoryPIcon, " +
                "c.categoryId categoryCId, " +
                "c.name categoryCName, " +
                "c.icon categoryCIcon, " +
                "ina.accountId inAccountId, " +
                "ina.name inAccountName, " +
                "outA.accountId outAccountId, " +
                "outA.name outAccountName, " +
                "p.tradeType tradeType, " +
                "p.displayTradeType displayTradeType, " +
                "b.remark remark, " +
                "outMoney, " +
                "inMoney, " +
                "imported, " +
                "date, " +
                "source, " +
                "expense, " +
                "income, " +
                "billId, " +
                "sm.relation splitFlag, " +
                "rfr.relation refundFlag, " +
                "rbr.relation reimbursementFlag " +
                "from bill as b " +
                "left join category as c on c.categoryId = b.category " +
                "left join category as p on p.categoryId = c.parentId " +
                "left join account as ina on ina.accountId = inAccount " +
                "left join account as outA on outA.accountId = outAccount " +
                "left join relationOfBills as sm on (billId = sm.mainBill or billId = sm.relatedBill) and sm.relation = \"SPLIT\" " +
                "left join relationOfBills as rfr on billId = rfr.relatedBill and rfr.relation = \"REFUND\" " +
                "left join relationOfBills as rbr on billId = rbr.relatedBill and rbr.relation = \"reimbursement\""
)
data class BillList(
        val categoryPId: Long, // 用于筛选
        val categoryPName: String, // 用于显示
        val categoryPIcon: String, // 用于显示
        val categoryCId: Long?, // 用于报表页面的筛选
        val categoryCName: String?, // 用于显示
        val categoryCIcon: String?, // 用于显示
        val inAccountId: Long?,
        val inAccountName: String?,
        val outAccountId: Long?,
        val outAccountName: String?,
        val tradeType: TradeType,
        val displayTradeType: DisplayTradeType,
        val remark: String,
        val outMoney: Int,
        val inMoney: Int,
        val imported: Boolean,
        val date: OffsetDateTime,
        val source: Source,
        val expense: Double,
        val income: Double,
        val billId: Long,
        val splitFlag: Relation?,
        val refundFlag: Relation?,
        val reimbursementFlag: Relation?
)