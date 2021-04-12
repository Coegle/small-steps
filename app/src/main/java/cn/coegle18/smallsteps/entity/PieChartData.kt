package cn.coegle18.smallsteps.entity

import cn.coegle18.smallsteps.TradeType

data class PieChartData(
        val id: Long,
        val name: String,
        val icon: String,
        val tradeType: TradeType,
        val expense: Double,
        val expPer: Double,
        val income: Double,
        val incPer: Double,
        val billNum: Long,
)
