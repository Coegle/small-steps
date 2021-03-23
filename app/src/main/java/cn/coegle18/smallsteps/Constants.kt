package cn.coegle18.smallsteps

class Constants {
    companion object {
        const val expenseIconNum = 96
        const val incomeIconNum = 9
        const val defaultAccountId = 21452965L
        val defaultAccountMap = mapOf(
            Pair(MainAccountType.BORROW, 15970700L), // 我的应付（借入）
            Pair(MainAccountType.LEND, 15970701L), // 我的应收（借出）
            Pair(MainAccountType.REIMBURSEMENT, 15970702L), // 我的报销
            Pair(MainAccountType.INVESTMENT, 15970703L), // 我的投资
            Pair(MainAccountType.REFUND, 15970704L) // 我的退款
        )
        val defaultCategoryMap = mapOf(
            Pair(TradeType.EXPENSE, 2001L),
            Pair(TradeType.INCOME, 1001L)
        )
        val defaultInitAccountCategoryMap = mapOf(
            Pair(TradeType.EXPENSE, 2100L),
            Pair(TradeType.INCOME, 1100L)
        )
        const val defaultTransferCategoryId = 3001L
    }
}