package com.kam.musicplayer.view.customview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.getResourceIdOrThrow
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kam.musicplayer.R
import com.kam.musicplayer.utils.getChildById
import com.kam.musicplayer.utils.getHitViews
import com.kam.musicplayer.utils.isViewChild
import kotlin.math.sign

/**
 * Custom layout that Generates a Bottom Sheet
 * This layout is supposed to act as a Bottom sheet it must be a child of a
 * [CoordinatorLayout] because [BottomSheetBehavior] needs [CoordinatorLayout.LayoutParams]
 * Also this inherits from [LinearLayout] so the required variables for that exist too.
 * This has some attrs defined in [R.styleable.BottomSheetView]
 *
 * This layout's native behavior for dragging is to fade and shrink out the draggable view
 * until it dissappears from view
 */
class BottomSheetView(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    /**
     * This id is used to find the draggable view, when it is attached to this layout
     * @see [mDraggableView]
     */
    private var mDraggableId: Int = 0

    /**
     * This id is used to find the content view, when it is attached to this layout
     * @see [mContentView]
     */
    private var mContentId: Int = 0

    /**
     * Array used to record touch events in order to decide whether this layout
     * should intercept them
     */
    private var mTouchDown = FloatArray(2)

    /**
     * Listener in case the parent activity or fragment needs it
     */
    private var mOnActionListener: OnActionListener? = null

    /**
     * This is where the magic happens
     * This is what handles all the backend for making the bottomsheet expand, collapse, hide
     * and everything in between. And the cool thing is I didn't write it
     * so its not as buggy as the rest of the stuff in this project @see [BottomSheetBehavior]
     */
    private var mBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    /** Callback for the BottomSheetBehavior */
    private var mCallback = CustomCallback()

    /**
     * Ai so lil bit of cool guy code here
     * Basically if a view is put into this box it needs to be not null
     * but the thing this is only added when children are added to the layout
     * and we need a way to check whether its initialized or not that's why we're not setting it
     * to be nullable instead of lateinit. But after this class is initialized
     * any views put into this must be not null. So the first check
     * is just checking that its not null
     *
     * Then the next check checks whether it's a child of this layout using [isViewChild]
     * This is required so that this layout can intercepts touch events on this view
     * which is needed if you want to drag this.
     *
     * Finally it sets the view and the calls [setDraggableView] which handles setting the layout
     * behavior and stuff
     */
    private var mDraggableView: View? = null
        set(value) {
            if (value == null)
                throw IllegalArgumentException("Draggable View cannot be null")

            if (!isViewChild(value))
                throw IllegalArgumentException("Draggable View must be a child of the BottomSheetView")
            field = value
            setDraggableView(field!!)
        }

    /**
     * This is the main content of the Bottom Sheet
     * Not really used yet but could be a cool thing later on
     */
    private var mContentView: View? = null
        set(value) {
            if (value == null)
                throw IllegalArgumentException("Content View cannot be null")

            if (!isViewChild(value))
                throw IllegalArgumentException("Content View must be a child of the BottomSheetView")

            field = value
        }

    /**
     * So here we get the [mDraggableId] and the [mContentId] for this layout from
     * the [AttributeSet] We also set the bacground color, which is white by default. This is important
     * as normally the background is null / transparent meaning if you expand the bottom sheet you will be able
     * to see the stuff behind it, setting the background color fixes this
     */
    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.BottomSheetView, 0, 0).apply {
            try {
                setBackgroundColor(context.getColor(getResourceId(R.styleable.BottomSheetView_background_color, android.R.color.white)))
                mDraggableId = getResourceIdOrThrow(R.styleable.BottomSheetView_viewToDrag)
                mContentId = getResourceIdOrThrow(R.styleable.BottomSheetView_content)
            } finally {
                recycle() // Don't really know what this does but the documentation said to do it
            }
        }
        mBottomSheetBehavior = BottomSheetBehavior(context, attrs)
    }

    /**
     * This is when children are added to this
     * Basically it checks if the views being added are the ones we need for
     * the content and draggable view, or if the ones we need are inside the
     * view being added
     */
    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        if (child != null) {
            val draggableView = child.getChildById(mDraggableId)
            if (draggableView != null) {
                mDraggableView = draggableView
                return
            }

            val contentView = child.getChildById(mContentId)
            if (contentView != null) {
                mContentView = contentView
                return
            }
        }
    }

    /**
     * when this layout is attached to the window / parent
     * it first needs to make sure its parent is a [CoordinatorLayout]
     * then it needs to set its behavior because this is the momment when the [getLayoutParams]
     * are initialized for sure
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (parent !is CoordinatorLayout)
            throw IllegalArgumentException("BottomSheetView must be a  child of CoordinatorLayout")

        (layoutParams as CoordinatorLayout.LayoutParams).behavior = mBottomSheetBehavior

        mBottomSheetBehavior.isHideable = false
        mBottomSheetBehavior.peekHeight = if (mDraggableView != null) mDraggableView!!.height else 100 // default peekHeight
        mOnActionListener?.onSetPeekHeight(mBottomSheetBehavior.peekHeight)
        mBottomSheetBehavior.addBottomSheetCallback(mCallback)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        handleTouchEvent(ev) // this will check if the event should be intercepted
        /** [handleTouchEvent] will tell the base class to whether it should intercept or not */
        return super.onInterceptTouchEvent(ev) /** if [handleTouchEvent] told parent to intercept parent will intercept here */
    }

    /**
     * Decides whether a touch event should be intercepted
     */
    private fun handleTouchEvent(ev: MotionEvent) {
        if (ev.action != MotionEvent.ACTION_MOVE) // If the action is not touch down or ACTION_MOVE then the parent will decide to intercept or not
            return

        // Since it is ACTION_MOVE we need to decide whether to intercept or not
        val hitViews = getHitViews(ev)

        if (hitViews.isEmpty()) { // If no view is touched then intercept (i.e. sheet will drag)
            requestDisallowInterceptTouchEvent(false) // false means do not intercept
            return
        }

        // If the mDraggableView is touched or one of the views touched is marked
        // as draggable then don't intercept
        if (!hitViews.contains(mDraggableView)
                && hitViews.firstOrNull {
                    it.getTag(R.id.tag_bottom_sheet_draggable) != null
                } != null) {
            val dx = (mTouchDown[0] - ev.x).sign.toInt()
            val dy = (mTouchDown[1] - ev.y).sign.toInt()

            // If any of the touched views can scroll then don't intercept
            if (!hitViews.any { it.canScrollHorizontally(dx) || it.canScrollVertically(dy) }) {
                requestDisallowInterceptTouchEvent(false) // false means don't intercept
                return
            }
        }
        // will reach here if mDraggable was touched or
        // and mDraggable was touched BUT none of the views touched
        // can scroll in the directions moved
        requestDisallowInterceptTouchEvent(true) // true means please scroll
    }

    /**
     * once the [mDraggableView] is created this will set the [mBottomSheetBehavior.peekHeight]
     * to the required value and notify the listeners
     */
    private fun setDraggableView(view: View) {
        view.post {
            mBottomSheetBehavior.peekHeight = view.height
            //Log.i("BSV", "${view.id}")
            //Log.i("BSV", "Peek Height ${view::class.java.name} ${view.height}")
            mOnActionListener?.onSetPeekHeight(mBottomSheetBehavior.peekHeight)
        }
    }

    fun setOnActionListener(listener: OnActionListener) {
        mOnActionListener = listener
        if (mDraggableView != null) { /** if the [mDraggableView] was set already then it will notify the new listener */
            listener.onSetPeekHeight(mBottomSheetBehavior.peekHeight)
        }
    }

    /**
     * For a class that wants to listen to events happening
     * in the [BottomSheetView]
     */
    interface OnActionListener {
        fun onStartDragging(openingSheet: Boolean) {}
        fun onSheetClosed() {}
        fun onSheetOpened() {}
        fun onSetPeekHeight(peekHeight: Int) {}
    }

    /**
     * Callback listener handles the cool bottom sheet behavior stuff
     */
    private inner class CustomCallback: BottomSheetBehavior.BottomSheetCallback() {
        /** Needed to keep track of what the sheet was doing currently */
        private var mDraggingState = BottomSheetBehavior.STATE_COLLAPSED

        /**
         * Since the height of the view can change this is updated every time the user starts dragging the view
         * when the sheet is collapsed
         */
        private var mStartHeight = 0

        /**
         * Called when the view is being dragged, and has moved from its previous spot
         * the slideOffset is a float between 0 and 1 that gives the relative position
         * of the draggable from the Bottom of the Screen to the Top
         */
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (mDraggableView != null) {
                val inverse = 1 - slideOffset

                mDraggableView!!.alpha = (inverse)

                val params = mDraggableView!!.layoutParams
                params.height = (mStartHeight * inverse).toInt()
                mDraggableView!!.layoutParams = params
            }
        }

        /**
         * Called whenever the state changes
         * first it checks whether the state has actually changed
         * then if it senses tat the bottom sheet has just finished being dragging
         * it calls [onSheetOpened] or [onSheetClosed] based on the situation
         *
         * If it has just started dragging it checks whether the sheet is being
         * opened or closed then calls [onStartDragging] based on that
         */
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == mDraggingState)
                return

            if (mDraggingState == BottomSheetBehavior.STATE_DRAGGING || mDraggingState == BottomSheetBehavior.STATE_SETTLING) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED)
                    onSheetOpened()
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    onSheetClosed()
            }

            if (newState == BottomSheetBehavior.STATE_DRAGGING)
                onStartDragging(mDraggingState == BottomSheetBehavior.STATE_COLLAPSED)

            mDraggingState = newState
        }

        /**
         * Called when dragging starts
         * sets [mStartHeight] to the [mDraggableView] views current height
         * then also makes sure the [mDraggableView] is visible also notifies
         * the [mOnActionListener]
         */
        private fun onStartDragging(openingSheet: Boolean) {
            if (mDraggableView != null) {
                mDraggableView!!.visibility = View.VISIBLE

                if (openingSheet)
                    mStartHeight = mDraggableView!!.height
            }

            mOnActionListener?.onStartDragging(openingSheet)
        }

        /**
         * Called when dragging has finished and the sheet is collapsed
         * makes sure the [mDraggableView] is visible and notifies
         * the [mOnActionListener]
         */
        private fun onSheetClosed() {
            if (mDraggableView != null)
                mDraggableView!!.visibility = View.VISIBLE

            mOnActionListener?.onSheetClosed()
        }

        /**
         * Called when dragging finishes and the sheet is opened
         * It hides the [mDraggableView] and then notifies the [mOnActionListener]
         */
        private fun onSheetOpened() {
            if (mDraggableView != null)
                mDraggableView!!.visibility = View.GONE

            mOnActionListener?.onSheetOpened()
        }
    }

}