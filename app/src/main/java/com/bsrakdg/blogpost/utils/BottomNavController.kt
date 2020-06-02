package com.bsrakdg.blogpost.utils

import android.app.Activity
import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.utils.BottomNavController.OnNavReselectedListener
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Class credit: Allan Veloso
 * https://stackoverflow.com/a/54505502
 * @property navigationBackStack: Backstack for the bottom navigation
 */

class BottomNavController(
    val context: Context,
    @IdRes val containerId: Int,
    @IdRes val appStartDestinationId: Int,
    val graphChangedListener: OnNavGraphChanged?, // optional
    val navGraphProvider: NavGraphProvider // required
) {

    private val TAG: String = "BottomNavController"
    lateinit var activity: Activity
    lateinit var fragmentManager: FragmentManager
    lateinit var navItemChangeListener: OnNavItemChanged
    private val navBackStack = BackStack.of(appStartDestinationId)

    init {
        if (context is Activity) {
            activity = context
            fragmentManager = (activity as FragmentActivity).supportFragmentManager
        }
    }

    fun onNavItemSelected(itemId: Int = navBackStack.last()): Boolean {
        // Replace fragment representing a navigation item
        val fragment =
            fragmentManager.findFragmentByTag(itemId.toString()) ?: NavHostFragment.create(
                navGraphProvider.getNavGraphId(itemId)
            )

        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(containerId, fragment, itemId.toString())
            .addToBackStack(null)
            .commit()

        // Add to backstack
        navBackStack.moveLast(itemId) // {1} moveThan(2) -> {1, 2}


        // update checked icon
        navItemChangeListener.onItemChanged(itemId)

        // Communicate with Activity
        graphChangedListener?.onGraphChanged() // notify repository from activity

        return true
    }

    private class BackStack : ArrayList<Int>() { //called backstack
        companion object {
            fun of(vararg elements: Int): BackStack {
                val b = BackStack()
                b.addAll(elements.toTypedArray())
                return b
            }
        }

        fun removeLast() = removeAt(size - 1)

        fun moveLast(item: Int) { // 1, 2, 3, 4  -> moveLast(3)
            remove(item) // remove(3) -> 1, 2, 4
            add(item) // add(3) -> 1, 2, 4, 3
        }
    }


    // for setting the checked icon in the bottom nav
    interface OnNavItemChanged {
        fun onItemChanged(itemId: Int)
    }

    fun setOnNavItemChanged(listener: (ItemId: Int) -> Unit) {
        this.navItemChangeListener = object : OnNavItemChanged {
            override fun onItemChanged(itemId: Int) {
                listener.invoke(itemId) // inner interface
            }

        }
    }

    // get id of each graph
    // example: R.navigation.nav.blog
    interface NavGraphProvider {

        @NavigationRes
        fun getNavGraphId(itemId: Int): Int
    }

    // Execute when nav graph changes
    // example: go from Home to Create tab
    interface OnNavGraphChanged {
        fun onGraphChanged()
    }

    // example: click Home duplicate
    interface OnNavReselectedListener {
        fun onReselectedNavItem(navController: NavController, fragment: Fragment)
    }
}

fun BottomNavigationView.setUpNavigation(
    bottomNavController: BottomNavController,
    onNavReselectedListener: OnNavReselectedListener
) {

    setOnNavigationItemSelectedListener {
        bottomNavController.onNavItemSelected(it.itemId)
    }

    setOnNavigationItemReselectedListener {
        bottomNavController
            .fragmentManager
            .findFragmentById(bottomNavController.containerId)!!
            .childFragmentManager
            .fragments[0]?.let { fragment ->

            onNavReselectedListener.onReselectedNavItem(
                bottomNavController.activity.findNavController(bottomNavController.containerId),
                fragment
            )

        }
    }
}
