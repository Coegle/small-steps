package cn.coegle18.smallsteps.activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import cn.coegle18.smallsteps.AppDatabase
import cn.coegle18.smallsteps.R
import cn.coegle18.smallsteps.util.CSVHelper
import kotlinx.android.synthetic.main.activity_overview.*
import kotlinx.android.synthetic.main.drawer_left.*
import kotlin.concurrent.thread

class OverviewActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)
        val navController = findNavController(R.id.nav_host_fragment)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.billsFragment || destination.id == R.id.overviewFragment || destination.id == R.id.assetsFragment || destination.id == R.id.pieChartFragment) {
                // 仅在这些 Fragment 显示底部导航栏
                nav_view.visibility = View.VISIBLE
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                // toolbar.visibility = View.GONE
                nav_view.visibility = View.GONE
                // 禁用折叠栏
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
        //
        // 为底部导航栏设置导航
        nav_view.setupWithNavController(navController)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.overviewFragment,
                R.id.assetsFragment,
                R.id.billsFragment,
                R.id.pieChartFragment
            ), drawer_layout
        )
        toolbar.setupWithNavController(navController, appBarConfiguration)
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        // 左侧抽屉导航栏监听

        drawer.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_category_management -> {
                    navController.navigate(R.id.categoryFragment)
                    true
                }
                R.id.navigation_export -> {
                    thread {
                        Looper.prepare()
                        val list =
                            AppDatabase.getDatabase(applicationContext).billDao().simpleQuery()
                        Log.d("list", list.subList(0, 1).toString())
                        if (CSVHelper.saveTextFile(applicationContext, list)) {
                            toast("成功导出至 Download 文件夹")
                        } else {
                            toast("导出失败")
                        }
                        Looper.loop()
                    }
                    drawer_layout.closeDrawers()
                    true
                }
                else -> true
            }
        }
    }

    fun setActionBarTitle(title: String) {
        toolbar.title = title
    }

    override fun onDestroy() {
        super.onDestroy()
        AppDatabase.getDatabase(applicationContext).close()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun vibratePhone(time: Long = 50) {
        val vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

fun Activity.toast(s: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, s, duration).show()
}