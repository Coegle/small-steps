package cn.coegle18.smallsteps.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cn.coegle18.smallsteps.PrimaryAccountType
import cn.coegle18.smallsteps.fragment.SubAddAssetsFragment

class AddAssetsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4
    override fun createFragment(position: Int): Fragment {
        return SubAddAssetsFragment.newInstance(PrimaryAccountType.values()[position])
    }
}