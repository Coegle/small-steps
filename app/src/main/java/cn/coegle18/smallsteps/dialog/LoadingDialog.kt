package cn.coegle18.smallsteps.dialog

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.States
import cn.coegle18.smallsteps.entity.NJUSTBill
import cn.coegle18.smallsteps.util.BILL
import cn.coegle18.smallsteps.util.NJUSTScraper
import cn.coegle18.smallsteps.viewmodel.LoadingViewModel
import cn.coegle18.smallsteps.viewmodel.LoadingViewModelFactory
import kotlinx.android.synthetic.main.dialog_loading.*
import java.lang.ref.WeakReference
import java.time.OffsetDateTime
import kotlin.concurrent.thread

const val RETURN_KEY = "fromLoading"
const val RETURN_VAL_ERROR_PASS = "errorPassword"
const val LATEST_TIME = "latestTime"

class LoadingDialog : DialogFragment() {
    private val outerClass = WeakReference(this)
    private val mMyHandler = MyHandler(outerClass)
    private val args: LoadingDialogArgs by navArgs()
    private val scrapedDataList: MutableList<NJUSTBill> = mutableListOf()
    private lateinit var viewModel: LoadingViewModel
    private lateinit var pres: SharedPreferences
    private var oldLatestTime: OffsetDateTime? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isCancelable = false
    }

    private fun setNewBalance(
            newOutMoney: Double?, newInMoney: Double?,
            outAccountId: Long?, inAccountId: Long?
    ) {
        val inDifference = (newInMoney ?: 0.00)
        val outDifference = (newOutMoney ?: 0.00)
        if (inDifference != 0.00) inAccountId?.let {
            viewModel.accountDao.updateAccountBalance(it, inDifference)
        }
        if (outDifference != 0.00) outAccountId?.let {
            viewModel.accountDao.updateAccountBalance(it, -outDifference)
        }
    }

    @ExperimentalStdlibApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, LoadingViewModelFactory(Application(), args.accountId)).get(LoadingViewModel::class.java)
        viewModel.simpleBillList.observe(viewLifecycleOwner) {
            Log.d("data, billList", it.size.toString())
            if (it.isNotEmpty()) {
                val billList = it.map { simpleBill ->
                    simpleBill.toBill()
                }
                viewModel.simpleBillList.removeSource(viewModel.accountList)
                thread {
                    for (bill in billList) {
                        viewModel.billDao.insertBill(bill)
                        setNewBalance(bill.outMoney, bill.inMoney,
                                bill.outAccount, bill.inAccount)
                    }
                }
            }

        }
        pres = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE)
        oldLatestTime = pres.getString(LATEST_TIME + args.accountId.toString(), null)?.run {
            OffsetDateTime.parse(this)
        }

        NJUSTScraper(args.userId, args.password, mMyHandler, oldLatestTime).getLoginParam()
    }

    fun setState(state: States, bill: NJUSTBill?, newLatestTime: String?) {
        titleText.text = state.caption
        when (state.state) {
            1 -> { // 成功
                btn.visibility = View.GONE
                if (state == States.GET_BILLS_SUCCESS) {
                    bill?.let {
                        scrapedDataList.add(bill)
                    }
                }
            }
            0 -> { // 进行中
                btn.visibility = View.GONE
            }
            -1 -> { // 失败
                btn.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                if (state == States.LOGIN_FAILED_ERROR_PASS) {
                    captionText.text = "用户名或者密码错误"
                    btn.text = "修改用户名和密码"
                    btn.setOnClickListener {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set(RETURN_KEY, RETURN_VAL_ERROR_PASS)
                    }
                } else {
                    btn.text = "确定"
                    btn.setOnClickListener {
                        findNavController().navigateUp()
                    }
                }
            }
            2 -> { // 结束
                Log.d("state", "结束")
                btn.visibility = View.VISIBLE
                btn.text = "确定"
                captionText.visibility = View.GONE
                progressBar.visibility = View.GONE
                viewModel.scrapedDataList.value = scrapedDataList
                btn.setOnClickListener {
                    findNavController().navigateUp()
                }
            }
            3 -> { // 最新账单的时间
                Log.d("new Date", newLatestTime.toString())
                newLatestTime?.let {
                    pres.edit {
                        putString(LATEST_TIME + args.accountId.toString(), newLatestTime)
                    }
                }

            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_loading, container, false)
    }

    class MyHandler(private val outerClass: WeakReference<LoadingDialog>) : Handler() {

        override fun handleMessage(msg: Message) {
            Log.d("Message", msg.toString())
            val state = States.values()[msg.what]
            val bill = msg.data.getParcelable(BILL) as NJUSTBill?
            val newLatestTime = msg.data.getString(LATEST_TIME)
            outerClass.get()?.setState(state, bill, newLatestTime)
        }
    }
}