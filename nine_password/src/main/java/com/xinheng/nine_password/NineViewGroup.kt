package com.xinheng.nine_password

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import java.lang.StringBuilder

/**
 * Created by XinHeng on 2019/01/29.
 * describe：九宫格的容器
 */
class NineViewGroup @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {
    /**
     * 水平间的间隔
     */
    private var paddingH = 60
    /**
     * 垂直间的间隔
     */
    private var paddingV = 60
    /**
     * 是否有第一个选中
     */
    private var firstSelect = true
    private val ERROR_STATUE = 2
    private val LINKING_STATUE = 1
    private val DEFAULT_STATUE = 0
    /**
     * 是否显示线条
     */
    var showLine = false
    /**
     * 连线最小有效数字
     */
    var minEffectiveSize = 4
    /**
     * 当前状态
     * 0->最初状态 DEFAULT_STATUE
     * 1->正在连线中 LINKING_STATUE
     * 2->错误状态 ERROR_STATUE
     */
    private var nowStatue = DEFAULT_STATUE
    /**
     * 一次密码设置完成标志
     */
    private var complete = false
    /**
     * 线条宽度
     */
    private var lineWidth = 5
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var buffer = StringBuilder()
    private var points = ArrayList<NineChildParent<*>>(9)
    private var childViews = ArrayList<NineChildParent<*>>(9)
    /**
     * 小格子的宽高
     */
    private var childSlide: Int = 30
    private var lineColor = Color.parseColor("#33b5e5")
    private var errorLineColor = Color.RED
    var onNineViewGroupListener: OnNineViewGroupListener? = null
        set(value) {
            field = value
            value?.let {
                setChildMode(it)
            }
        }
    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    init {
        //使能调用onDraw()方法
        setWillNotDraw(false)
        var array = context.obtainStyledAttributes(attrs, R.styleable.NineViewGroup, defStyleAttr, 0)
        (0..array.indexCount).forEach {
            var index = array.getIndex(it)
            when (index) {
                R.styleable.NineViewGroup_nine_child_size -> childSlide = array.getDimensionPixelSize(index, childSlide)
                R.styleable.NineViewGroup_nine_line_color -> lineColor = array.getColor(index, lineColor)
                R.styleable.NineViewGroup_nine_error_line_color -> errorLineColor = array.getColor(index, errorLineColor)
                R.styleable.NineViewGroup_nine_effective_size -> minEffectiveSize = array.getInt(index, minEffectiveSize)
                R.styleable.NineViewGroup_nine_padding_h -> paddingH = array.getDimensionPixelSize(index, paddingH)
                R.styleable.NineViewGroup_nine_padding_v -> paddingV = array.getDimensionPixelSize(index, paddingV)
                R.styleable.NineViewGroup_nine_show_line -> showLine = array.getBoolean(index, showLine)
                R.styleable.NineViewGroup_nine_line_width -> lineWidth = array.getDimensionPixelSize(index, lineWidth)
            }
        }
        array.recycle()
        paint.strokeWidth = lineWidth.toFloat()
    }

    private fun setChildMode(onNineViewGroupListener: OnNineViewGroupListener) {
        removeAllViews()
        childViews.clear()
        (0..8).forEach {
            var mode = onNineViewGroupListener.getChildMode()
            mode.NINE_CHILD_INF.index = it + 1
            mode.setDefaultStatue()
            addView(mode.view, getLp())
            childViews.add(mode)
        }
    }

    private fun getLp(): LayoutParams {
        return LayoutParams(childSlide, childSlide)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = childSlide * 3 + paddingLeft + paddingRight + paddingH * 2
        var height = childSlide * 3 + paddingTop + paddingBottom + paddingV * 2
        setMeasuredDimension(width, height)
        //又忘了计算子view的大小了。。。
        measureChildren(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var childView: View
        var top: Int = paddingTop
        var left: Int = paddingLeft
        var right: Int
        var bottom: Int
        if (childCount > 0) {
            (0 until childCount).forEach {
                childView = getChildAt(it)
                right = left + childView.measuredWidth
                bottom = top + childView.measuredHeight
                //Log.e("TAG", "onLayout: $left $top $right $bottom")
                var nineChildInf = (childViews[it]).NINE_CHILD_INF
                nineChildInf.setContent(it + 1, (left + right) / 2f, (top + bottom) / 2f)
                //Log.e("TAG", "onLayout: child=$nineChildInf")
                childView.layout(left, top, right, bottom)
                if ((it + 1) % 3 == 0) {
                    left = paddingLeft
                    top = bottom + paddingV
                } else {
                    left = right + paddingH
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (childCount == 0 || complete) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //记录落点
                lastX = event.x
                lastY = event.y
                downUpdateChild(lastX, lastY)
            }
            MotionEvent.ACTION_MOVE -> {
                lastX = event.x
                lastY = event.y
                moveUpdateChild(lastX, lastY)
            }
            MotionEvent.ACTION_UP -> {
                complete = true
                //统计
                upUpdateChild()
            }
        }
        return true
    }

    private fun downUpdateChild(x: Float, y: Float) {
        firstSelect = childContains(x, y)
    }

    private fun moveUpdateChild(x: Float, y: Float) {
        if (firstSelect) {
            moveUpdateLineAndChildView(x, y)
        } else {
            downUpdateChild(x, y)
        }
    }

    private fun moveUpdateLineAndChildView(x: Float, y: Float) {
        if (points.size != childCount)
            childContains(x, y)
        invalidate()
    }

    private fun upUpdateChild() {
        var effective = points.size >= minEffectiveSize
        onNineViewGroupListener?.complete(effective, buffer.toString())
    }

    /**
     * 错误状态展示
     */
    fun showErrorStatue() {
        nowStatue = ERROR_STATUE
        points.forEach {
            it.setErrorStatue()
        }
        invalidate()
        resetStatueDelayed(500)
    }

    /**
     * 恢复初始状态
     */
    private fun resetStatue() {
        points.clear()
        firstSelect = false
        lastX = 0f
        lastY = 0f
        buffer.clear()
        nowStatue = DEFAULT_STATUE
        (0 until childCount).forEach {
            var nineChildParent = childViews[it]
            nineChildParent.setDefaultStatue()
            nineChildParent.NINE_CHILD_INF.isLight = false
        }
        invalidate()
        complete = false
    }

    fun resetStatueDelayed(time: Int) {
        postDelayed({ resetStatue() }, time.toLong())
    }

    private fun childContains(x: Float, y: Float): Boolean {
        (0 until childCount).forEach {
            var childAt = getChildAt(it)
            if (x >= childAt.left && x < childAt.right && y >= childAt.top && y < childAt.bottom) {
                return if (!childViews[it].NINE_CHILD_INF.isLight) {
                    if (points.size > 0) {
                        checkMiddleChild(points[points.size - 1], childViews[it])?.run {
                            if (!NINE_CHILD_INF.isLight) {
                                buffer.append(NINE_CHILD_INF.index)
                                changeLightStatue(this)
                            }
                        }
                    }
                    buffer.append(it + 1)
                    //TODO 改变子view的UI状态
                    changeLightStatue(childViews[it])
                    true
                } else {
                    false
                }
            }
        }
        return false
    }

    private fun changeLightStatue(childParent: NineChildParent<*>) {
        childParent.NINE_CHILD_INF.isLight = true
        childParent.setLightStatue()
        points.add(childParent)//记录
    }

    private fun checkMiddleChild(nineChildParent: NineChildParent<*>, nineChildParent1: NineChildParent<*>): NineChildParent<*>? {
        var index = nineChildParent.NINE_CHILD_INF.index
        var index1 = nineChildParent1.NINE_CHILD_INF.index
        var sum = index + index1
        if (sum == 10) {
            return childViews[4]
        } else if (index % 2 != 0 && index1 % 2 != 0) {
            if ((sum == 4 || sum == 16) || (sum == 8 && (index == 1 || index1 == 1))||(sum == 12 && (index == 3 || index1 == 3)))
                return childViews[sum / 2 - 1]
        }
        return null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!showLine) {
            return
        }
        paint.color = when (nowStatue) {
            ERROR_STATUE -> errorLineColor
            else -> lineColor
        }
        if (points.size > 1) {
            (1 until points.size).forEach {
                var pointXYStart = points[it - 1].NINE_CHILD_INF
                var pointXYEnd = points[it].NINE_CHILD_INF
                canvas.drawLine(pointXYStart.centerX, pointXYStart.centerY, pointXYEnd.centerX, pointXYEnd.centerY, paint)
            }
        }
        if (lastX > 0 && points.size > 0) {
            var pointXY = points[points.size - 1].NINE_CHILD_INF
            canvas.drawLine(pointXY.centerX, pointXY.centerY, lastX, lastY, paint)
        }
    }

    interface OnNineViewGroupListener {
        /**
         * 子view
         */
        fun getChildMode(): NineChildParent<*>

        /**
         * 密码设置结束
         * @param effective 是否有效
         * @param password 密码
         */
        fun complete(effective: Boolean, password: String)
    }
}