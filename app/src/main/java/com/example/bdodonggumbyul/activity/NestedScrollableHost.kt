package com.example.bdodonggumbyul.activity

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Layout to wrap a scrollable component inside a ViewPager2. Provided as a solution to the problem
 * where pages of ViewPager2 have nested scrollable elements that scroll in the same direction as
 * ViewPager2. The scrollable element needs to be the immediate and only child of this host layout.
 *
 * This solution has limitations when using multiple levels of nested scrollable elements
 * (e.g. a horizontal RecyclerView in a vertical RecyclerView in a horizontal ViewPager2).
 */
class NestedScrollableHost : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private var touchSlop = 0
    private var initialX = 0f
    private var initialY = 0f
    private val parentDrawerLayout: DrawerLayout?
        get() {
            var v: View? = parent as? View
            while (v != null && v !is DrawerLayout) {
                v = v.parent as? View
            }
            return v as? DrawerLayout
        }

    private val child: View? get() = if (childCount > 0) getChildAt(0) else null

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

//    private fun canChildScroll(orientation: Int, delta: Float): Boolean {
//        val direction = -delta.sign.toInt()
//        return when (orientation) {
//            0 -> child?.canScrollHorizontally(direction) ?: false
//            1 -> child?.canScrollVertically(direction) ?: false
//            else -> throw IllegalArgumentException()
//        }
//    }
//
//    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
//        handleInterceptTouchEvent(e)
//        return super.onInterceptTouchEvent(e)
//    }
//
//    private fun handleInterceptTouchEvent(e: MotionEvent) {
//        val drawerElevation = parentDrawerLayout?.drawerElevation ?: return
//
//        // Early return if child can't scroll in same direction as parent
//        if (!canChildScroll(, -1f) && !canChildScroll(drawerElevation, 1f)) {
//            return
//        }
//
//        if (e.action == MotionEvent.ACTION_DOWN) {
//            initialX = e.x
//            initialY = e.y
//            parent.requestDisallowInterceptTouchEvent(true)
//        } else if (e.action == MotionEvent.ACTION_MOVE) {
//            val dx = e.x - initialX
//            val dy = e.y - initialY
//            val isVpHorizontal = drawerElevation == View.VISIBLE
//
//            // assuming ViewPager2 touch-slop is 2x touch-slop of child
//            val scaledDx = dx.absoluteValue * if (isVpHorizontal) .5f else 1f
//            val scaledDy = dy.absoluteValue * if (isVpHorizontal) 1f else .5f
//
//            if (scaledDx > touchSlop || scaledDy > touchSlop) {
//                if (isVpHorizontal == (scaledDy > scaledDx)) {
//                    // Gesture is perpendicular, allow all parents to intercept
//                    parent.requestDisallowInterceptTouchEvent(false)
//                } else {
//                    // Gesture is parallel, query child if movement in that direction is possible
//                    if (canChildScroll(drawerElevation, if (isVpHorizontal) dx else dy)) {
//                        // Child can scroll, disallow all parents to intercept
//                        parent.requestDisallowInterceptTouchEvent(true)
//                    } else {
//                        // Child cannot scroll, allow all parents to intercept
//                        parent.requestDisallowInterceptTouchEvent(false)
//                    }
//                }
//            }
//        }
//    }
}