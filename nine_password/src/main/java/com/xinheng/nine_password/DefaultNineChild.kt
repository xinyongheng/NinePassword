package com.xinheng.nine_password

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.LevelListDrawable
import android.support.v4.content.ContextCompat
import android.view.View

/**
 * Created by XinHeng on 2019/02/27.
 * describe：9宫格子view
 */
class DefaultNineChild(view: View) : NineChildParent<View>(view) {

    override fun setErrorStatue() {
        view.background = ContextCompat.getDrawable(context, R.drawable.error)!!
    }

    override fun setLightStatue() {
        view.background = ContextCompat.getDrawable(context, R.drawable.selected)!!
    }

    override fun setDefaultStatue() {
        view.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.GRAY)
        }
    }
}
