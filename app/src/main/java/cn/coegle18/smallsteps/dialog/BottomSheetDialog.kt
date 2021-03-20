package cn.coegle18.smallsteps.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.Editable
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.Visible
import cn.coegle18.smallsteps.adapter.BottomSheetAdapter
import cn.coegle18.smallsteps.adapter.SheetItem
import cn.coegle18.smallsteps.dao.CategoryDao
import cn.coegle18.smallsteps.entity.Category
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_sheet.*
import kotlin.concurrent.thread

class BottomSheetDialog : BottomSheetDialogFragment() {

    private val args: BottomSheetDialogArgs by navArgs()
    private lateinit var categoryDao: CategoryDao
    private var currentCategory: Category? = null
    private val actionList = mutableListOf(SheetItem("编辑分类", true), SheetItem("停用分类", true), SheetItem("删除分类", true))
    private val mAdapter = BottomSheetAdapter(R.layout.item_add_asset)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        categoryDao = AppDatabase.getDatabase(context).categoryDao()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentCategory = if (args.isSubCategory) args.cCategory else args.pCategory

        mAdapter.setList(getData())
        sheetRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mAdapter
        }
        mAdapter.getViewByPosition(0, R.layout.item_add_asset)?.isEnabled
        mAdapter.setOnItemClickListener { _, _, position ->
            when (position) {
                0 -> {
                    args.let {
                        val action = BottomSheetDialogDirections.editCategoryAction(it.pCategory, it.cCategory, it.displayTradeType, false)
                        findNavController().navigate(action)
                    }
                }
                1 -> {
                    if (actionList[1].caption.contains("停用")) {
                        thread {
                            currentCategory?.let {
                                if (!args.isSubCategory) {
                                    categoryDao.updateVisibilityOfSubCategory(it.categoryId, it.visible, Visible.DEACTIVATED)
                                }
                                it.visible = Visible.DEACTIVATED
                                categoryDao.updateCategory(it)
                            }
                        }
                    } else if (actionList[1].caption.contains("启用")) {
                        thread {
                            currentCategory?.let {
                                if (!args.isSubCategory) {
                                    categoryDao.updateVisibilityOfSubCategory(it.categoryId, it.visible, Visible.ENABLED)
                                }
                                it.visible = Visible.ENABLED
                                categoryDao.updateCategory(it)
                            }
                        }
                    }
                    dismiss()
                }
                2 -> { // 删除
                    thread {

                        currentCategory?.let {
                            if (!args.isSubCategory) {
                                categoryDao.updateVisibilityOfSubCategory(it.categoryId, it.visible, Visible.DISABLED)
                            }
                            it.visible = Visible.DISABLED
                            categoryDao.updateCategory(it)
                        }
                    }
                    dismiss()
                }
            }
        }
    }

    private fun getData(): MutableList<SheetItem> {
        if (currentCategory?.editable == Editable.DISABLED) {
            actionList[0].clickable = false
            actionList[2].clickable = false
        }
        if (currentCategory?.visible == Visible.DEACTIVATED) actionList[1].caption = "启用分类"
        return actionList
    }
}