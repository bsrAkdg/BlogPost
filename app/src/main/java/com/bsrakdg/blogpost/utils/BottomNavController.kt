package com.bsrakdg.blogpost.utils

import android.app.Activity
import android.content.Context
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.bsrakdg.blogpost.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.parcel.Parcelize

/**
 * Class credit: Allan Veloso
 * https://stackoverflow.com/a/54505502
 * @property navigationBackStack: Backstack for the bottom navigation
 */
const val BOTTOM_NAV_BACKSTACK_KEY = "com.bsrakdg.blogpost.utils.BottomNavController.BackStack"

class BottomNavController(
    val context: Context,
    @IdRes val containerId: Int,
    @IdRes val appStartDestinationId: Int,
    val graphChangeListener: OnNavigationGraphChanged?, // should be optional
    val navGraphProvider: NavGraphProvider // required
) {
    lateinit var navigationBackStack: BackStack
    lateinit var activity: Activity
    lateinit var fragmentManager: FragmentManager
    lateinit var navItemChangeListener: OnNavigationItemChanged

    init {
        if (context is Activity) {
            activity = context
            fragmentManager = (activity as FragmentActivity).supportFragmentManager
        }
    }

    fun setupBottomNavigationBackStack(previousBackStack: BackStack?) {
        navigationBackStack = previousBackStack?.let {
            it
        } ?: BackStack.of(appStartDestinationId)
    }

    fun onNavigationItemSelected(itemId: Int = navigationBackStack.last()): Boolean {

        // Replace fragment representing a navigation item
        val fragment = fragmentManager.findFragmentByTag(itemId.toString())
            ?: NavHostFragment.create(navGraphProvider.getNavGraphId(itemId))
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

        // Add to back stack
        navigationBackStack.moveLast(itemId) // {1} moveThan(2) -> {1, 2}

        // Update checked icon
        navItemChangeListener.onItemChanged(itemId)

        // communicate with Activity
        graphChangeListener?.onGraphChange() // notify repository from activity

        return true
    }

    fun onBackPressed() {
        val childFragmentManager = fragmentManager.findFragmentById(containerId)!!
            .childFragmentManager
        when {
            // We should always try to go back on the child fragment manager stack before going to
            // the navigation stack. It's important to use the child fragment manager instead of the
            // NavController because if the user change tabs super fast commit of the
            // supportFragmentManager may mess up with the NavController child fragment manager back
            // stack

            childFragmentManager.popBackStackImmediate() -> {
            }
            // Fragment back stack is empty so try to go back on the navigation stack
            navigationBackStack.size > 1 -> {
                // Remove last item from back stack
                navigationBackStack.removeLast()

                // Update the container with new fragment
                onNavigationItemSelected()

            }
            // If the stack has only one and it's not the navigation home we should
            // ensure that the application always leave from startDestination
            navigationBackStack.last() != appStartDestinationId -> {
                navigationBackStack.removeLast()
                navigationBackStack.add(0, appStartDestinationId)
                onNavigationItemSelected()
            }
            // Navigation stack is empty, so finish the activity
            else -> activity.finish()
        }
    }

    @Parcelize
    class BackStack : ArrayList<Int>(), Parcelable {
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

    // For setting the checked icon in the bottom nav
    interface OnNavigationItemChanged {
        fun onItemChanged(itemId: Int)
    }

    // Get id of each graph
    // ex: R.navigation.nav_blog
    // ex: R.navigation.nav_create_blog
    interface NavGraphProvider {
        @NavigationRes
        fun getNavGraphId(itemId: Int): Int
    }

    // Execute when Navigation Graph changes.
    // ex: Select a new item on the bottom navigation
    // ex: Home -> Account
    interface OnNavigationGraphChanged{
        fun onGraphChange()
    }

    interface OnNavigationReselectedListener{

        fun onReselectNavItem(navController: NavController, fragment: Fragment)
    }

    fun setOnItemNavigationChanged(listener: (itemId: Int) -> Unit) {
        this.navItemChangeListener = object : OnNavigationItemChanged {
            override fun onItemChanged(itemId: Int) {
                listener.invoke(itemId)
            }
        }
    }
}

// Convenience extension to set up the navigation
fun BottomNavigationView.setUpNavigation(
    bottomNavController: BottomNavController,
    onReselectListener: BottomNavController.OnNavigationReselectedListener
) {

    setOnNavigationItemSelectedListener {
        bottomNavController.onNavigationItemSelected(it.itemId)

    }

    setOnNavigationItemReselectedListener {
        bottomNavController
            .fragmentManager
            .findFragmentById(bottomNavController.containerId)!!
            .childFragmentManager
            .fragments[0]?.let { fragment ->

            onReselectListener.onReselectNavItem(
                bottomNavController.activity.findNavController(bottomNavController.containerId),
                fragment
            )
        }
    }

    bottomNavController.setOnItemNavigationChanged { itemId ->
        menu.findItem(itemId).isChecked = true
    }
}
