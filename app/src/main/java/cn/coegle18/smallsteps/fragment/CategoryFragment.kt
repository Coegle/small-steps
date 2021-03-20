package cn.coegle18.smallsteps.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.adapter.CategoryAdapter
import cn.coegle18.smallsteps.adapter.RootNode
import cn.coegle18.smallsteps.adapter.SubNode
import cn.coegle18.smallsteps.viewmodel.CategoryViewModel
import kotlinx.android.synthetic.main.fragment_add_assets_second_view.*
import kotlinx.android.synthetic.main.toggle_group_two.*


class CategoryFragment : Fragment() {

    companion object {
        fun newInstance() = CategoryFragment()
    }

    private lateinit var viewModel: CategoryViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)


        toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.leftBtn -> {
                    if (isChecked) {
                        Log.d("button", "leftBtn clicked")
                        viewModel.changeTradeType(TradeType.EXPENSE)
                    }

                }
                R.id.rightBtn -> {
                    if (isChecked) {
                        Log.d("button", "rightBtn clicked")
                        viewModel.changeTradeType(TradeType.INCOME)
                    }

                }
            }
        }
        toggleButton.apply {
            isSingleSelection = true
            isSelectionRequired = true
            check(R.id.leftBtn)
        }
        middleBtn.visibility = View.GONE
        leftBtn.text = "支出"
        rightBtn.text = "收入"

        val mAdapter = CategoryAdapter()

        // 拖拽监听
//        val OnItemDragListener = object : OnItemDragListener {
//            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder, pos: Int) {
//                //Log.d(TAG, "drag start")
//                val holder = viewHolder as BaseViewHolder
//                // 折叠子列表
//                mAdapter.collapse(pos)
//                // 开始时，item背景色变化，demo这里使用了一个动画渐变，使得自然
//                val startColor = Color.WHITE
//                val endColor = Color.rgb(245, 245, 245)
//                val v = ValueAnimator.ofArgb(startColor, endColor)
//                v.addUpdateListener { animation -> holder.itemView.setBackgroundColor(animation.animatedValue as Int) }
//                v.duration = 300
//                v.start()
//            }
//
//            override fun onItemDragMoving(source: RecyclerView.ViewHolder, from: Int, target: RecyclerView.ViewHolder, to: Int) {
//                //Log.d(TAG, "move from: " + source.adapterPosition + " to: " + target.adapterPosition)
//            }
//
//            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, pos: Int) {
//                //Log.d(TAG, "drag end")
//                val holder = viewHolder as BaseViewHolder
//                // 结束时，item背景色变化，demo这里使用了一个动画渐变，使得自然
//                val startColor = Color.rgb(245, 245, 245)
//                val endColor = Color.WHITE
//                val v = ValueAnimator.ofArgb(startColor, endColor)
//                v.addUpdateListener { animation -> holder.itemView.setBackgroundColor(animation.animatedValue as Int) }
//                v.duration = 300
//                v.start()
//            }
//        }

        //mAdapter.draggableModule.setOnItemDragListener(listener)
        //mAdapter.draggableModule.toggleViewId = R.id.imageView
        //mAdapter.draggableModule.isDragOnLongPressEnabled = false
        mAdapter.addChildClickViewIds(R.id.imageView)
        mAdapter.addChildClickViewIds(R.id.subCategory)

        //mAdapter.draggableModule.isDragEnabled = true
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.imageView) { // 点击母分类的编辑选项
                Log.d("Click", "子分类：$position")
                val pCategory = (adapter.getItem(position) as RootNode).node
                val action = CategoryFragmentDirections.showMoreEditAction(pCategory, null, pCategory.displayTradeType, isNewCategory = false, isSubCategory = false)
                findNavController().navigate(action)
            }
            if (view.id == R.id.subCategory) { // 点击子分类
                Log.d("Click", "子分类：$position")
                val cCategory = (adapter.getItem(position) as SubNode).node
                val pCategory = (adapter.data[(adapter as CategoryAdapter).findParentNode(position)] as RootNode).node
                if (cCategory != null) {
                    val action = CategoryFragmentDirections.showMoreEditAction(pCategory, cCategory, cCategory.displayTradeType, isNewCategory = false, isSubCategory = true)
                    findNavController().navigate(action)
                } else {
                    val action = CategoryFragmentDirections.addCategoryAction(pCategory, null, pCategory.displayTradeType, true)
                    findNavController().navigate(action)
                }
            }
        }

        recyclerView.apply {
            layoutManager = GridLayoutManager(this.context, 5)
            adapter = mAdapter
        }
        viewModel.displayDataList.observe(viewLifecycleOwner, {
            Log.d("viewModel", "displayDataList changed")
            mAdapter.setList(it)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 向 Activity 告知该 Fragment 参与选项菜单的填充
        setHasOptionsMenu(true)
    }

    // 重新绘制菜单栏
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.assets_fragment_menu, menu)
    }

    // 相应菜单项目点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.rightIcon -> {
                val action = viewModel.displayTradeType.value?.let { CategoryFragmentDirections.addCategoryAction(null, null, it, true) }
                if (action != null) {
                    findNavController().navigate(action)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}