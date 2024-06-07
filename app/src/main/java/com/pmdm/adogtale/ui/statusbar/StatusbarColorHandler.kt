package com.pmdm.adogtale.ui.statusbar

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class StatusbarColorHandler {

    fun setStatusbarBackgroundColor(activity: AppCompatActivity, color: Int){
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        activity.window.statusBarColor = color
    }

}