package cn.coegle18.smallsteps.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.adapter.AccountAdapter
import cn.coegle18.smallsteps.entity.AccountView
import cn.coegle18.smallsteps.util.Util
import cn.coegle18.smallsteps.viewmodel.AssetsViewModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartAnimationType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartStackingType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import kotlinx.android.synthetic.main.fragment_assets.*

class AssetsFragment : Fragment() {

    private lateinit var viewModel: AssetsViewModel
    private lateinit var mAdapter: AccountAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_assets, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AssetsViewModel::class.java)

        mAdapter = AccountAdapter(R.layout.item_account, R.layout.header_view, mutableListOf())
        accountRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mAdapter
        }

        mAdapter.setOnItemClickListener { _, _, position ->
            val accountSection = viewModel.accountSection[position]
            if (!accountSection.isHeader) {
                val action = AssetsFragmentDirections.showAssetDetailAction(accountSection.data as AccountView)
                findNavController().navigate(action)
            }
        }

        // ????????????
        viewModel.allVisibleAccountList.observe(viewLifecycleOwner, {
            Log.d("LiveData", "allAccountsOfAccountType, $it")
            viewModel.group(it)
            mAdapter.setList(viewModel.accountSection)
        })
        setAssetsCard()

        val headline = layoutInflater.inflate(R.layout.item_headline6, accountRecyclerView, false)
        mAdapter.addHeaderView(headline)
        // ?????? item_headline6 ????????????
        val headlineText = headline.findViewById<TextView>(R.id.headlineText)
        headlineText.text = "????????????"
        val button = headline.findViewById<Button>(R.id.button)
        button.visibility = View.GONE


    }

    // ????????????????????????
    private fun setCardCompose(includeView: View, text: String, colorId: Int): TextView {
        val titleText = includeView.findViewById<TextView>(R.id.titleText)
        val dot = includeView.findViewById<View>(R.id.dot)
        dot.backgroundTintList = ContextCompat.getColorStateList(requireContext(), colorId)
        titleText.text = text
        return includeView.findViewById(R.id.moneyText)
    }

    private fun setAssetsCard() {
        val headerCard = layoutInflater.inflate(R.layout.item_card_assets, accountRecyclerView, false)
        mAdapter.addHeaderView(headerCard)

        val currency = headerCard.findViewById<View>(R.id.currency)
        val currencyMoney = setCardCompose(currency, "??????", R.color.green)
        viewModel.currencyMoney.observe(viewLifecycleOwner) {
            currencyMoney.text = "???" + Util.balanceFormatter.format(it ?: 0.00)
        }

        val fund = headerCard.findViewById<View>(R.id.fund)
        val fundMoney = setCardCompose(fund, "??????", R.color.blue)
        viewModel.fundMoney.observe(viewLifecycleOwner) {
            fundMoney.text = "???" + Util.balanceFormatter.format(it ?: 0.00)
        }

        val lend = headerCard.findViewById<View>(R.id.lend)
        val lendMoney = setCardCompose(lend, "??????", R.color.red)
        viewModel.lendMoney.observe(viewLifecycleOwner) {
            lendMoney.text = "???" + Util.balanceFormatter.format(it ?: 0.00)
        }

        val liability = headerCard.findViewById<View>(R.id.liability)
        val liabilityMoney = setCardCompose(liability, "??????", R.color.grey)
        viewModel.liabilityMoney.observe(viewLifecycleOwner) {
            liabilityMoney.text = "???" + Util.balanceFormatter.format(it ?: 0.00)
        }

        val chartView = headerCard.findViewById<com.github.aachartmodel.aainfographics.aachartcreator.AAChartView>(R.id.chartView)
        val chartModel = AAChartModel().chartType(AAChartType.Column)
        chartModel.stacking(AAChartStackingType.Percent)
                .animationType(AAChartAnimationType.EaseInQuad)
                .xAxisVisible(false)
                .yAxisVisible(false)
                .legendEnabled(false)
                .yAxisTitle("")
                .tooltipEnabled(false)
                .inverted(true)
                .borderRadius(5f)
        viewModel.assetsMoneyList.observe(viewLifecycleOwner) {
            chartModel.series(it)
            chartView.aa_drawChartWithChartModel(chartModel)

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ??? Activity ????????? Fragment ???????????????????????????
        setHasOptionsMenu(true)
    }

    // ?????????????????????
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.assets_fragment_menu, menu)
    }

    // ????????????????????????
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.rightIcon -> {
                val action = AssetsFragmentDirections.selectNewAssetAction()
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}