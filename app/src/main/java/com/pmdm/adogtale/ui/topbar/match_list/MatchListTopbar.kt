package com.pmdm.adogtale.ui.topbar.match_list

import android.content.Intent
import android.util.Log
import android.view.View.VISIBLE
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.pmdm.adogtale.R
import com.pmdm.adogtale.chat.ChatListActivity
import com.pmdm.adogtale.controller.ProfileActions
import com.pmdm.adogtale.ui.CardSwipeActivity
import com.pmdm.adogtale.ui.EditProfileActivity
import com.pmdm.adogtale.ui.topbar.hamburger_menu.ExtraMenu
import com.squareup.picasso.Picasso

class MatchListTopbar(private val context: AppCompatActivity) {

    private val profileActions: ProfileActions = ProfileActions()
    private lateinit var extraMenu: ExtraMenu

    fun configureTopbar() {
        // Toolbar reference
        val toolbar = context.findViewById<Toolbar>(R.id.topbar_matches)
        context.setSupportActionBar(toolbar)
        setTopbarListeners()
        context.supportActionBar?.setDisplayShowTitleEnabled(false)
        downloadProfileImageToTopbar()
    }

    private fun setTopbarListeners() {
        val settingsOption = context.findViewById<ImageView>(R.id.matches_list_topbar_settings)

        extraMenu = ExtraMenu(context, settingsOption)
        extraMenu.configureExtraMenu()

        settingsOption.setOnClickListener {
            extraMenu.show()
        }

        context.findViewById<ImageView>(R.id.matches_list_topbar_chats).setOnClickListener {
            val intent = Intent(this.context, ChatListActivity::class.java)
            this.context.startActivity(intent)
        }

        context.findViewById<ImageView>(R.id.matches_list_topbar_card_swipe).setOnClickListener {
            val intent = Intent(this.context, CardSwipeActivity::class.java)
            context.startActivity(intent)
        }

        context.findViewById<ImageView>(R.id.matches_list_topbar_profile).setOnClickListener {
            val intent = Intent(this.context, EditProfileActivity::class.java)
            this.context.startActivity(intent)
        }
    }

    private fun downloadProfileImageToTopbar() {
        val profileTopbarMenuOption =
            context.findViewById<ImageView>(R.id.matches_list_topbar_profile)

        profileActions.getCurrentProfile { profile ->
            Log.i("MatchListActivity", "toolbar pic1: " + profile.pic1)

            if (profile.pic1.isBlank()) {
                return@getCurrentProfile
            }

            Picasso.get()
                .load(profile.pic1)
                .into(profileTopbarMenuOption)
        }
    }

    fun showBadge(menuOption: MatchListTopbarOption) {
        when (menuOption) {
            MatchListTopbarOption.SETTINGS -> {
                context.findViewById<ImageView>(R.id.matches_list_topbar_settings_badge).visibility =
                    VISIBLE
            }

            MatchListTopbarOption.CHAT -> {
                context.findViewById<ImageView>(R.id.matches_list_topbar_chats_badge).visibility =
                    VISIBLE
            }

            MatchListTopbarOption.LOGO -> {
                context.findViewById<ImageView>(R.id.matches_list_topbar_logo_badge).visibility =
                    VISIBLE
            }

            MatchListTopbarOption.CARD_SWIPE -> {
                context.findViewById<ImageView>(R.id.matches_list_topbar_card_swipe_badge).visibility =
                    VISIBLE
            }

            MatchListTopbarOption.PROFILE -> {
                context.findViewById<ImageView>(R.id.matches_list_topbar_profile_badge).visibility =
                    VISIBLE
            }
        }
    }

    enum class MatchListTopbarOption {
        SETTINGS,
        CHAT,
        LOGO,
        CARD_SWIPE,
        PROFILE
    }
}
