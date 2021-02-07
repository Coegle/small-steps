package cn.coegle18.smallsteps

import androidx.room.TypeConverter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class Converter {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    fun toBaseAccountType(value: String) = enumValueOf<BaseAccountType>(value)

    @TypeConverter
    fun fromBaseAccountType(value: BaseAccountType) = value.name

    @TypeConverter
    fun toPrimaryAccountType(value: String) = enumValueOf<PrimaryAccountType>(value)

    @TypeConverter
    fun fromPrimaryAccountType(value: PrimaryAccountType) = value.name

    @TypeConverter
    fun toMainAccountType(value: String) = enumValueOf<MainAccountType>(value)

    @TypeConverter
    fun fromMainAccountType(value: MainAccountType) = value.name

    @TypeConverter
    fun toTradeType(value: String) = enumValueOf<TradeType>(value)

    @TypeConverter
    fun fromTradeType(value: TradeType) = value.name

    @TypeConverter
    fun toDisplayTradeType(value: String) = enumValueOf<DisplayTradeType>(value)

    @TypeConverter
    fun fromDisplayTradeType(value: DisplayTradeType) = value.name

    @TypeConverter
    fun toEditable(value: String) = enumValueOf<Editable>(value)

    @TypeConverter
    fun fromEditable(value: Editable) = value.name

    @TypeConverter
    fun toVisible(value: String) = enumValueOf<Visible>(value)

    @TypeConverter
    fun fromVisible(value: Visible) = value.name

    @TypeConverter
    fun toRelation(value: String?) = value?.let { enumValueOf<Relation>(it) }

    @TypeConverter
    fun fromRelation(value: Relation?) = value?.name

    @TypeConverter
    fun toSource(value: String) = enumValueOf<Source>(value)

    @TypeConverter
    fun fromSource(value: Source) = value.name

    @TypeConverter
    fun toOffsetDateTime(value: String) = formatter.parse(value, OffsetDateTime::from)

    @TypeConverter
    fun fromOffsetDateTime(date: OffsetDateTime): String = date.format(formatter)

}


// 复式记账下的账户类型：AccountType
enum class BaseAccountType {
    CURRENCY, // 流动资产
    FUTURE, // 非流动资产，包括投资和应收
    LIABILITY; // 负债
}

// 在用户添加账户时，作为首要的四类账户类型在一级视图进行展示：AccountType
enum class PrimaryAccountType {
    MONEY, // 资金账户
    INVESTMENT, // 投资账户
    RECEIVABLE, // 应收账户
    PAYABLE // 应付帐户
}

// 最终的账户类型：AccountType Category
enum class MainAccountType {
    DEPOSIT_CARD, // 储蓄卡
    CASH, // 现金
    ALI_PAY, // 支付宝
    SCHOOL_CARD, // 校园卡
    WECHAT_PAY, // 微信支付
    CUSTOM, // 用户自定义类型
    INVESTMENT, // 投资
    LEND, // 借出
    REIMBURSEMENT, // 报销
    REFUND, // 退款
    CREDIT_CARD, // 信用卡
    BORROW; // 借入
}

// 复式记账中的实际交易类型 Category
enum class TradeType {
    INCOME, // 收入
    EXPENSE, // 支出
    TRANSFER; // 转账
}

// 基于流水账，对于用户显示的交易类型 Category
enum class DisplayTradeType {
    INCOME, // 收入
    EXPENSE, // 支出
    NONE, // 无（转账）
}

// 分类是否可以使用 Category
enum class Visible {
    DISABLED, // 隐藏
    ENABLED, // 显示
    DEACTIVATED; // 停用
}

// 分类的编辑权限 Category
enum class Editable {
    NONE, // 分类不可见，没有权限
    DISABLED, // 只能调整顺序，名称和图标不能编辑
    ENABLED; // 可以编辑
}

// 账单之间的关系 Bill RelationOfBills
enum class Relation {
    SPLIT, // 拆分
    REFUND, // 退款
    REIMBURSEMENT; // 报销
}

// 账单的来源 Bill
enum class Source {
    MANUAL, // 手动记一笔
    IMPORTED, // 普通导入
    AUTO_IMPORTED, // 自动导入
    SPLIT; // 拆分得到
}
