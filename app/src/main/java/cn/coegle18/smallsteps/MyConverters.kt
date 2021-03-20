package cn.coegle18.smallsteps

import android.os.Parcelable
import androidx.room.TypeConverter
import kotlinx.android.parcel.Parcelize
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
    fun toMainAccountType(value: String?) = value?.let { enumValueOf<MainAccountType>(it) }

    @TypeConverter
    fun fromMainAccountType(value: MainAccountType?) = value?.name

    @TypeConverter
    fun toTradeType(value: String) = enumValueOf<TradeType>(value)

    @TypeConverter
    fun fromTradeType(value: TradeType) = value.name

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
@Parcelize
enum class PrimaryAccountType(val caption: String) : Parcelable {
    MONEY("资金"), // 资金账户
    INVESTMENT("投资"), // 投资账户
    RECEIVABLE("应收"), // 应收账户
    PAYABLE("应付") // 应付帐户
}

// 最终的账户类型：AccountType Category
@Parcelize
enum class MainAccountType(val caption: String) : Parcelable {
    DEPOSIT_CARD("储蓄卡"),
    CASH("现金"),
    ALI_PAY("支付宝"),
    SCHOOL_CARD("校园卡"),
    WECHAT_PAY("微信支付"),
    CUSTOM("自定义"),
    INVESTMENT("投资"),
    LEND("借出"),
    REIMBURSEMENT("报销"),
    REFUND("退款"),
    CREDIT_CARD("信用卡"),
    BORROW("借入");
}

// 复式记账中的实际交易类型 Category
@Parcelize
enum class TradeType(val caption: String) : Parcelable {
    EXPENSE("支出"),
    INCOME("收入"),
    TRANSFER("转账");
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
