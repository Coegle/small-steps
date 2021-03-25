package cn.coegle18.smallsteps.entity

import androidx.room.DatabaseView

@DatabaseView(
    "SELECT c.dateList as date, sum(b.income) as income, sum(b.expense) as expense  " +
            "FROM calendar AS c " +
            "LEFT JOIN " +
            "( " +
            "    SELECT date, expense, income " +
            "    FROM Bill " +
            "    WHERE visible in (\"ENABLED\", \"SYSTEM\") " +
            ") AS b " +
            "ON c.dateList = DATE(b.date) " +
            "WHERE c.dateList <= date(\"now\") AND " +
            "    c.dateList >= (SELECT MIN(DATE(minDate.`date`)) FROM Bill AS minDate) " +
            "GROUP BY c.dateList " +
            "ORDER BY dateList DESC"
)
data class CashFlowView(
    val date: String,
    val income: Double,
    val expense: Double
)