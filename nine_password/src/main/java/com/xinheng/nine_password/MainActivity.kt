package com.xinheng.nine_password

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nine.onNineViewGroupListener = object : NineViewGroup.OnNineViewGroupListener {
            override fun complete(effective: Boolean, password: String) {
                Log.e("TAG", "complete:$effective $password ")
                if (effective) {
                    nine.resetStatueDelayed(1000)
//                    nine.showErrorStatue()
                } else {
                    nine.showErrorStatue()
                }
            }

            override fun getChildMode(): NineChildParent<View> {
                return DefaultNineChild(View(this@MainActivity))
            }
        }
    }
}
