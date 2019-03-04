package com.xinheng.nine_password

import android.view.View

/**
 * Created by XinHeng on 2019/02/27.
 * describe：9宫格子view必须实现此接口
 */
abstract class NineChildParent<T : View>(var view: T) {
    protected open var context = view.context.applicationContext
    val NINE_CHILD_INF = NineChildInf()
    /**
     * 密码错误时的显示
     */
    abstract fun setErrorStatue()

    /**
     * 被选中时的显示
     */
    abstract fun setLightStatue()

    /**
     * 默认显示
     */
    abstract fun setDefaultStatue()

    class NineChildInf {
        /**
         * 当前所在9宫格的位置
         * 从1开始
         */
        var index = 0
        /**
         * 是否被点亮
         */
        var isLight = false
        /**
         * 中心点所在父类容器内的坐标
         */
        var centerX = 0.toFloat()
        var centerY = 0.toFloat()

        fun setContent(index: Int, centerX: Float, centerY: Float) {
            this.index = index
            this.centerX = centerX
            this.centerY = centerY
        }

        constructor()

        fun updateCenterPoint(x: Float, y: Float) {
            this.centerX = x
            this.centerY = y
        }

        fun reset() {
            this.index = 0
            this.centerX = 0f
            this.centerY = 0f
            this.isLight = false
        }

        override fun toString(): String {
            return "NineChildInf(index=$index, isLight=$isLight, centerX=$centerX, centerY=$centerY)"
        }
    }
}
