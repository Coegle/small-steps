package cn.coegle18.smallsteps.fragment

import android.content.Context
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import cn.coegle18.smallsteps.*
import cn.coegle18.smallsteps.activity.OverviewActivity
import cn.coegle18.smallsteps.adapter.CategoryIconAdapter
import cn.coegle18.smallsteps.adapter.IconSetting
import cn.coegle18.smallsteps.dao.CategoryDao
import cn.coegle18.smallsteps.entity.Category
import cn.coegle18.smallsteps.util.ActivityUtil
import kotlinx.android.synthetic.main.fragment_edit_category.*
import java.util.*
import kotlin.concurrent.thread
import kotlin.properties.Delegates

class EditCategoryFragment : Fragment() {

    private val args: EditCategoryFragmentArgs by navArgs()
    private lateinit var categoryDao: CategoryDao
    private var isNewPCategory by Delegates.notNull<Boolean>()
    private var isNewCCategory by Delegates.notNull<Boolean>()
    private var isOldPCategory by Delegates.notNull<Boolean>()
    private var isOldCCategory by Delegates.notNull<Boolean>()
    private val iconList: MutableList<IconSetting> = mutableListOf()

    // todo 该默认图标在 income 中没有
    private var icon: String = "ic_category_expense_63"

    private fun setIcon(resName: String) {
        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_icon_constraint) as LayerDrawable
        val resId = requireContext().resources.getIdentifier(resName, "drawable", requireContext().packageName)
        val res = ContextCompat.getDrawable(requireContext(), resId)
        if (res != null) {
            icon.setDrawableByLayerId(R.id.firstLayer, res)
            textField.startIconDrawable = icon
        }
    }

    private fun fillAndUploadData(name: String, icon: String) {
        val tradeType = args.displayTradeType
        // todo order 该怎么填
        when {
            isNewPCategory -> {
                val newValue = Category(tradeType, args.displayTradeType, name, null, 0, Visible.ENABLED, 1.0, Editable.ENABLED, icon)
                thread { categoryDao.insertCategory(newValue) }
            }
            isNewCCategory -> {
                val newValue = Category(tradeType, args.displayTradeType, name, null, args.pCategory!!.categoryId, Visible.ENABLED, 1.0, Editable.ENABLED, icon)
                thread { categoryDao.insertCategory(newValue) }
            }
            isOldPCategory -> {
                val newValue = args.pCategory!!
                newValue.name = name
                newValue.icon = icon
                thread { categoryDao.updateCategory(newValue) }
            }
            isOldCCategory -> {
                val newValue = args.cCategory!!
                newValue.name = name
                newValue.icon = icon
                thread { categoryDao.updateCategory(newValue) }
            }
        }
    }

    private fun getCandidateIconList() {
        when (args.displayTradeType) {
            TradeType.INCOME -> {
                repeat(Constants.incomeIconNum) {
                    iconList.add(IconSetting("ic_bg_green", "ic_category_income_${it + 1}"))
                }
            }
            TradeType.EXPENSE -> {
                repeat(Constants.expenseIconNum) {
                    iconList.add(IconSetting("ic_bg_red", "ic_category_expense_${it + 1}"))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 向 Activity 告知该 Fragment 参与选项菜单的填充
        setHasOptionsMenu(true)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        categoryDao = AppDatabase.getDatabase(context).categoryDao()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isNewPCategory = args.isNewCategory && args.pCategory == null
        isNewCCategory = args.isNewCategory && args.pCategory != null && args.cCategory == null
        isOldPCategory = !args.isNewCategory && args.pCategory != null && args.cCategory == null
        isOldCCategory = !args.isNewCategory && args.pCategory != null && args.cCategory != null

        if (isNewCCategory || isNewPCategory) (requireActivity() as OverviewActivity).setActionBarTitle("添加分类")
        // 填充分类名称和图标
        when {
            isOldPCategory -> {
                val category = args.pCategory
                titleText.setText(category!!.name)
                icon = "ic_category_${category.displayTradeType.name.toLowerCase(Locale.ROOT)}_${category.icon}"
            }
            isOldCCategory -> {
                val category = args.cCategory
                titleText.setText(category!!.name)
                icon = "ic_category_${category.displayTradeType.name.toLowerCase(Locale.ROOT)}_${category.icon}"
            }
        }
        setIcon(icon)
        // 获取待选图标列表
        getCandidateIconList()

        val mAdapter = CategoryIconAdapter(R.layout.item_icon, iconList)
        iconRecyclerView.apply {
            layoutManager = GridLayoutManager(this.context, 5)
            adapter = mAdapter
        }
        mAdapter.setOnItemClickListener { adapter, _, position ->
            icon = (adapter.getItem(position) as IconSetting).icon
            setIcon(icon)
        }
    }

    // 重新绘制菜单栏
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.assets_fragment_menu, menu)
        menu[0].apply {
            title = "确定"
            setIcon(R.drawable.ic_baseline_done_24)
        }
    }

    // 相应菜单项目点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.rightIcon -> {
                Log.d("Click", "rightIcon")
                val name = titleText.text.toString()
                if (name != "" && name.length <= 4) {
                    fillAndUploadData(name, icon.split('_')[3])
                    ActivityUtil.hideSoftKeyBoard(requireActivity())
                    findNavController().navigateUp()
                } else {
                    if (name == "") Toast.makeText(requireContext(), "分类名不能为空", Toast.LENGTH_SHORT).show()
                    else if (name.length > 4) Toast.makeText(requireContext(), "分类名不能超过四个字符", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}