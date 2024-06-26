package com.pmdm.adogtale.ui.topbar.card_swipe

import android.content.Intent
import android.util.Log
import android.view.View.VISIBLE
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.pmdm.adogtale.R
import com.pmdm.adogtale.chat.ChatListActivity
import com.pmdm.adogtale.controller.ProfileActions
import com.pmdm.adogtale.matches.MatchesListActivity
import com.pmdm.adogtale.ui.EditProfileActivity
import com.pmdm.adogtale.ui.topbar.hamburger_menu.ExtraMenu
import com.squareup.picasso.Picasso


class CardSwipeTopbar(private val context: AppCompatActivity) {

    private val profileActions: ProfileActions = ProfileActions();
    private lateinit var extraMenu: ExtraMenu;

    fun configureTopbar() {
        // Toolbar reference
        val toolbar = context.findViewById<Toolbar>(R.id.toolbar2)
        context.setSupportActionBar(toolbar)
        setTopbarListeners()
        context.supportActionBar?.setDisplayShowTitleEnabled(false)
        downloadProfileImageToTopbar()
    }

    private fun setTopbarListeners() {

        val settingsOption = context.findViewById<ImageView>(R.id.card_swipe_topbar_settings)

        extraMenu = ExtraMenu(context, settingsOption)
        extraMenu.configureExtraMenu()

        settingsOption.setOnClickListener {
            extraMenu.show()
        }

        context.findViewById<ImageView>(R.id.card_swipe_topbar_chats).setOnClickListener {
            val intent = Intent(this.context, ChatListActivity::class.java)
            this.context.startActivity(intent);
        }

        val matchesButton = context.findViewById<ImageView>(R.id.card_swipe_topbar_matches)

        matchesButton.setOnClickListener {
            val intent = Intent(this.context, MatchesListActivity::class.java)
            this.context.startActivity(intent);
        }
        context.findViewById<ImageView>(R.id.card_swipe_topbar_profile).setOnClickListener {
            val intent = Intent(this.context, EditProfileActivity::class.java)
            this.context.startActivity(intent)
        }
    }

    private fun downloadProfileImageToTopbar() {
        val profileTopbarMenuOption =
            context.findViewById<ImageView>(R.id.card_swipe_topbar_profile)

        profileActions.getCurrentProfile { profile ->

            Log.i("CardSwipeActivity", "toolbar pic1: " + profile.pic1)

            if (profile.pic1.isBlank()) {
                return@getCurrentProfile
            }

            Picasso.get()
                .load(profile.pic1)
                .into(profileTopbarMenuOption)
        }
    }

    fun showBadge(menuOption: CardSwipeTobarOption) {
        when (menuOption) {
            CardSwipeTobarOption.SETTINGS -> {
                context.findViewById<ImageView>(R.id.card_swipe_topbar_settings_badge)
                    .setVisibility(VISIBLE)
            }

            CardSwipeTobarOption.CHAT -> {
                context.findViewById<ImageView>(R.id.card_swipe_topbar_chats_badge)
                    .setVisibility(VISIBLE)
            }

            CardSwipeTobarOption.LOGO -> {
                context.findViewById<ImageView>(R.id.card_swipe_topbar_logo_badge)
                    .setVisibility(VISIBLE)
            }

            CardSwipeTobarOption.MATCHES -> {
                context.findViewById<ImageView>(R.id.card_swipe_topbar_matches_badge)
                    .setVisibility(VISIBLE)
            }

            CardSwipeTobarOption.PROFILE -> {
                context.findViewById<ImageView>(R.id.card_swipe_topbar_profile_badge)
                    .setVisibility(VISIBLE)
            }
        }
    }

    enum class CardSwipeTobarOption {
        SETTINGS,
        CHAT,
        LOGO,
        MATCHES,
        PROFILE
    }

}