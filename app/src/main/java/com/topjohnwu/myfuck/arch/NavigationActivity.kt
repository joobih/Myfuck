package com.topjohnwu.myfuck.arch

import android.view.KeyEvent
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment

abstract class NavigationActivity<Binding : ViewDataBinding> : UIActivity<Binding>() {

    abstract val navHostId: Int

    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(navHostId) as NavHostFragment
    }

    protected val currentFragment get() =
        navHostFragment.childFragmentManager.fragments.getOrNull(0) as? BaseFragment<*>

    val navigation: NavController get() = navHostFragment.navController

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return currentFragment?.onKeyEvent(event) == true || super.dispatchKeyEvent(event)
    }

    override fun onBackPressed() {
        if (currentFragment?.onBackPressed()?.not() == true) {
            super.onBackPressed()
        }
    }

    fun NavDirections.navigate() {
        navigation.navigate(this)
    }
}
