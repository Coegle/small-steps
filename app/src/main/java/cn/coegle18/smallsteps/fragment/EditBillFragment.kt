package cn.coegle18.smallsteps.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.TradeType
import cn.coegle18.smallsteps.adapter.EditBillAdapter
import com.google.android.material.tabs.TabLayoutMediator

import kotlinx.android.synthetic.main.fragment_edit_bill.*


class EditBillFragment : Fragment() {

    private val args: EditBillFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_edit_bill, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pagerAdapter = EditBillAdapter(this, args.billInfo, args.newAccountId)

        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = TradeType.values()[position].caption

        }.attach()
        args.billInfo?.let {
            val index = it.displayTradeType.ordinal
            viewPager.doOnPreDraw {
                tabLayout.getTabAt(index)?.select()
                viewPager.setCurrentItem(index, true)
                Log.d("data", viewPager.currentItem.toString())
            }
        }
    }

}