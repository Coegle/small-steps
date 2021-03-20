package cn.coegle18.smallsteps.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.coegle18.smallsteps.PrimaryAccountType
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.adapter.AddAssetsAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_add_assets.*

class AddAssetsFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_assets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pagerAdapter = AddAssetsAdapter(this)
        pager.adapter = pagerAdapter
        TabLayoutMediator(tab_layout, pager) { tab, position ->
            tab.text = PrimaryAccountType.values()[position].caption
        }.attach()
    }

    companion object {
        fun newInstance() = AssetDetailFragment()
    }
}