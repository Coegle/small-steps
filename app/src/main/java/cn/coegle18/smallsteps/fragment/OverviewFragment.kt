package cn.coegle18.smallsteps.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import cn.coegle18.smallsteps.Constants
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.util.Util
import cn.coegle18.smallsteps.viewmodel.OverviewViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.db.williamchart.view.DonutChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import kotlinx.android.synthetic.main.fragment_overview.*
import kotlinx.android.synthetic.main.item_card_budget.*

const val BUDGET = "budget"

class OverviewFragment : Fragment() {

    private lateinit var viewModel: OverviewViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_overview, container, false).also { view ->
            view?.findViewById<DonutChartView>(R.id.donutChart)
                    ?.show(listOf())
        }
    }

    private fun setTrendCard() {
        val chart = TrendCard.findViewById<AAChartView>(R.id.chartView)
        val sourceBtn = TrendCard.findViewById<Button>(R.id.sourceBtn)
        val chartModel = AAChartModel().chartType(AAChartType.Spline)

        viewModel.trendBtnText.observe(viewLifecycleOwner) {
            sourceBtn.text = it
        }
        sourceBtn.setOnClickListener {
            viewModel.trendPlusOne()
        }

        viewModel.trendList.observe(viewLifecycleOwner) { list ->
            val data = list.map { it.balance }
            val xAxis = list.map { it.date.split("-")[2] + "日" }

            val ser = AASeriesElement().data(data.toTypedArray())
                    .name("总额")
                    .lineWidth(3.0f)
                    .color("#6200EE")

            chartModel.series(arrayOf(ser))
                    .categories(xAxis.toTypedArray())
                    .yAxisTitle("")
                    .yAxisGridLineWidth(0.1f)
                    .tooltipValueSuffix("元")
                    .xAxisVisible(false)
                    .legendEnabled(false)
                    .markerRadius(0f)

            chart.aa_drawChartWithChartModel(chartModel)
        }
    }

    // 每日收支卡片设置
    private fun setCashFlowCard() {
        val chart = dailyCard.findViewById<AAChartView>(R.id.chartView)
        val sourceBtn = dailyCard.findViewById<Button>(R.id.sourceBtn)
        val chartModel = AAChartModel().chartType(AAChartType.Column)
        val chartTitleText = dailyCard.findViewById<TextView>(R.id.chartTitleText)

        chartTitleText.text = "每日收支"

        sourceBtn.setOnClickListener {
            viewModel.cashFlowPlusOne()
        }

        viewModel.cashFlowBtnText.observe(viewLifecycleOwner) {
            sourceBtn.text = it
        }

        viewModel.cashFlowList.observe(viewLifecycleOwner) { list ->
            val xAxis = list.map { it.date.split("-")[2] + "日" }

            val expenseSer = AASeriesElement()
                    .data(list.map { it.expense }.toTypedArray())
                    .name("支出")
                    .color("#EB5757")
            val incomeSer = AASeriesElement()
                    .data(list.map { it.income }.toTypedArray())
                    .name("收入")
                    .color("#27AE60")

            chartModel.series(arrayOf(expenseSer, incomeSer))
                    .categories(xAxis.toTypedArray())
                    .yAxisTitle("")
                    .yAxisGridLineWidth(0.1f)
                    .tooltipValueSuffix("元")
                    .xAxisVisible(false)
                    .legendEnabled(false)
                    .markerRadius(0f)
                    .borderRadius(3f)

            chart.aa_drawChartWithChartModel(chartModel)
        }
    }

    // 预算卡片设置
    private fun setBudgetCard() {
        changeBudgetText.setOnClickListener {
            val pres = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE)
            val budget = pres.getInt(BUDGET, Constants.defaultBudget)

            MaterialDialog(requireContext()).show {
                title(text = "当前预算为￥${budget}")
                input(inputType = InputType.TYPE_CLASS_NUMBER) { _, text ->
                    val newBudget = text.toString().toInt()
                    viewModel.budget.value = newBudget
                    pres.edit {
                        putInt(BUDGET, newBudget)
                    }
                }
                positiveButton(R.string.save)
                negativeButton(R.string.cancel)
            }
        }

        viewModel.leftExpense.observe(viewLifecycleOwner) {
            if (it != null) budgetLeftText.text = "预算剩余￥${Util.balanceFormatterSimple.format(it)}"
        }
        viewModel.percent.observe(viewLifecycleOwner) {
            if (it != null) {
                val data = if (it > 100) 100.0 else it
                donutChart.animate(listOf(data.toFloat()))
                percentText.text = "${it.toInt()}%"
            }
        }
        donutChart.donutColors = intArrayOf(
                Color.parseColor("#6200EE"),
        )
        donutChart.animation.duration = 1000L
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OverviewViewModel::class.java)
        // 点击添加账单浮动按钮
        addBillFab.setOnClickListener {
            val action = OverviewFragmentDirections.addBillAction(null, Constants.defaultAccountId)
            Navigation.findNavController(it).navigate(action)
        }
        setTrendCard()
        setCashFlowCard()
        setBudgetCard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                addBillFab.hide()
            } else {
                addBillFab.show()
            }
        }
    }
}