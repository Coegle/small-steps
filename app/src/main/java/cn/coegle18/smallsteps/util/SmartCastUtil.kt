package cn.coegle18.smallsteps.util

import cn.coegle18.smallsteps.*
import cn.coegle18.smallsteps.entity.AccountView
import cn.coegle18.smallsteps.entity.CategoryView
import cn.coegle18.smallsteps.entity.NJUSTBill
import cn.coegle18.smallsteps.entity.SimpleBill
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class SmartCastUtil {
    companion object {
        fun toSimpleBill(njustBill: NJUSTBill, accountList: List<AccountView>, categoryList: List<CategoryView>, accountId: Long): SimpleBill {
            val tradeType = when {
                njustBill.tradeTypeStr.contains("补贴") -> TradeType.INCOME
                njustBill.tradeTypeStr.contains("圈存") -> TradeType.TRANSFER
                else -> TradeType.EXPENSE
            }
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val date = LocalDateTime.parse(njustBill.dateStr, formatter).atZone(TimeZone.getDefault().toZoneId()).toOffsetDateTime()
            val hour = date.hour
            val remark = njustBill.remark.replace(" · 窗口机号:", "·")
            return when (tradeType) {
                TradeType.INCOME -> {
                    val inCategoryList = categoryList.filter { it.displayTradeType == TradeType.INCOME }
                    val keyWords = listOf("补", "暖", "饭")
                    val cCategory = inCategoryList.find {
                        var res = false
                        for (keyword in keyWords) {
                            if (it.cName?.contains(keyword) == true) res = true
                        }
                        res
                    }
                    val pCategory = inCategoryList.find {
                        var res = false
                        for (keyword in keyWords) {
                            if (it.pName.contains(keyword)) res = true
                        }
                        res
                    }?.id
                    if (cCategory != null) {
                        SimpleBill(date, cCategory.pId, cCategory.cId, tradeType, njustBill.money, accountId, null, remark, Source.AUTO_IMPORTED)
                    } else {
                        SimpleBill(date, pCategory
                                ?: Constants.defaultInitAccountCategoryMap[TradeType.INCOME]!!, null, tradeType, njustBill.money, accountId, null, remark, Source.AUTO_IMPORTED)
                    }
                }
                TradeType.EXPENSE -> {
                    val outCategoryList = categoryList.filter { it.displayTradeType == TradeType.EXPENSE }
                    val keyWords = if (remark.contains("园") || remark.contains("食堂")) {
                        when {
                            hour < 10 && njustBill.money < 8 -> {
                                listOf("早")
                            }
                            hour > 16 -> {
                                listOf("晚", "餐")
                            }
                            else -> {
                                listOf("中", "午", "餐")
                            }
                        }
                    } else if (remark.contains("超市")) {
                        listOf("零", "饮", "日", "市")
                    } else {
                        listOf("其")
                    }
                    val cCategory = outCategoryList.find {
                        var res = false
                        for (keyword in keyWords) {
                            if (it.cName?.contains(keyword) == true) res = true
                        }
                        res
                    }
                    val pCategory = outCategoryList.find {
                        var res = false
                        for (keyword in keyWords) {
                            if (it.pName.contains(keyword)) res = true
                        }
                        res
                    }?.pId
                    if (cCategory != null) {
                        SimpleBill(date, cCategory.pId, cCategory.cId, tradeType, njustBill.money, null, accountId, remark, Source.AUTO_IMPORTED)
                    } else
                        SimpleBill(date, pCategory
                                ?: Constants.defaultInitAccountCategoryMap[TradeType.EXPENSE]!!, null, tradeType, njustBill.money, null, accountId, remark, Source.AUTO_IMPORTED)
                }
                TradeType.TRANSFER -> {
                    val outAccountId = accountList.find { it.mainAccountType == MainAccountType.CREDIT_CARD && it.visible == Visible.ENABLED && (it.accountTypeCName == "中国银行" || it.accountTypeCName == "交通银行") }?.accountId
                    SimpleBill(date, Constants.defaultTransferCategoryId, null, tradeType, njustBill.money, accountId, outAccountId
                            ?: Constants.defaultAccountId, remark, Source.AUTO_IMPORTED)
                }
            }
        }
    }
}