package com.pmdm.adogtale.ui.topbar.hamburger_menu

import android.content.Intent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.pmdm.adogtale.R
import com.pmdm.adogtale.ui.OptionsActivity
import com.pmdm.adogtale.utils.FirebaseUtil

class ExtraMenu(private val context: AppCompatActivity, private val trigger: ImageView) {
    private var firebaseUtil: FirebaseUtil = FirebaseUtil()
    private lateinit var nativeMenu: PopupMenu

    fun configureExtraMenu() {
        nativeMenu = PopupMenu(context, trigger)

        nativeMenu.menuInflater.inflate(R.menu.topbar_extra_menu, nativeMenu.menu)
        nativeMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.topbar_extra_menu_edit_profile -> {
                    val intent = Intent(context, OptionsActivity::class.java)
                    context.startActivity(intent)
                    true
                }

                R.id.topbar_extra_menu_logout -> {
                    firebaseUtil.logout(context)
                    true
                }

                else -> false
            }
        }

    }

    fun show() {
        nativeMenu.show()
    }

}