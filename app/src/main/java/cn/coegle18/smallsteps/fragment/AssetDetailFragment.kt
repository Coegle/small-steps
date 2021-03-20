package cn.coegle18.smallsteps.fragment

import android.os.Bundle
import android.view.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.activity.OverviewActivity
import cn.coegle18.smallsteps.viewmodel.AssetDetailViewModel
import kotlinx.android.synthetic.main.fragment_asset_detail.*
import kotlinx.android.synthetic.main.item_btn_two.*

class AssetDetailFragment : Fragment() {

    private lateinit var viewModel: AssetDetailViewModel
    private val args: AssetDetailFragmentArgs by navArgs()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_asset_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AssetDetailViewModel::class.java)
        balanceText.text = "￥${args.accountInfo.balance}"
        (requireActivity() as OverviewActivity).setActionBarTitle(args.accountInfo.name)
        if (args.accountInfo.autoImport) {
            deleteBtn.text = "自动导入"
        } else {
            deleteBtn.visibility = View.GONE
        }
        okBtn.apply {
            text = "记一笔"
            setOnClickListener {
                val action = AssetDetailFragmentDirections.editBillActionzFromAsset(null, args.accountInfo.accountId)
                findNavController().navigate(action)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 向 Activity 告知该 Fragment 参与选项菜单的填充
        setHasOptionsMenu(true)
    }

    // 重新绘制菜单栏
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.assets_fragment_menu, menu)
        menu[0].apply {
            title = "确定"
            setIcon(R.drawable.ic_baseline_edit_24)
        }
    }

    // 相应菜单项目点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.rightIcon -> {
                val action = AssetDetailFragmentDirections.editAssetAction(args.accountInfo)
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}