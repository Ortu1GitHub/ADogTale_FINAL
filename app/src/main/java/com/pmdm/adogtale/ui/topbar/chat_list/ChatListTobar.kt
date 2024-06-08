package com.pmdm.adogtale.ui.topbar.chat_list

import android.content.Intent
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.pmdm.adogtale.R
import com.pmdm.adogtale.controller.ProfileActions
import com.pmdm.adogtale.matches.MatchesListActivity
import com.pmdm.adogtale.ui.CardSwipeActivity
import com.pmdm.adogtale.ui.EditProfileActivity
import com.pmdm.adogtale.ui.topbar.card_swipe.CardSwipeTopbar.CardSwipeTobarOption
import com.pmdm.adogtale.ui.topbar.hamburger_menu.ExtraMenu
import com.squareup.picasso.Picasso

class ChatListTobar(private val context: AppCompatActivity) {

    private val profileActions: ProfileActions = ProfileActions()
    private lateinit var extraMenu: ExtraMenu

    fun configureTopbar() {
        // Encuentra la referencia de tu Toolbar
        val toolbar = context.findViewById<Toolbar>(R.id.topbar_chat)

        // Configura la Toolbar como la barra de acción de la actividad
        context.setSupportActionBar(toolbar)

        setTopbarListeners()

        // Opcional: si deseas ocultar el título predeterminado de la barra de acción
        context.supportActionBar?.setDisplayShowTitleEnabled(false)

        downloadProfileImageToTopbar()
    }

    private fun setTopbarListeners() {

        val settingsOption = context.findViewById<ImageView>(R.id.chat_list_topbar_settings)

        extraMenu = ExtraMenu(context, settingsOption)
        extraMenu.configureExtraMenu()

        settingsOption.setOnClickListener {
            extraMenu.show()
        }

        context.findViewById<ImageView>(R.id.chat_list_topbar_card_swipe).setOnClickListener {
            val intent = Intent(this.context, CardSwipeActivity::class.java)
            context.startActivity(intent)
        }

        context.findViewById<ImageView>(R.id.chat_list_topbar_matches).setOnClickListener {
            val intent = Intent(this.context, MatchesListActivity::class.java)
            this.context.startActivity(intent)
        }

        context.findViewById<ImageView>(R.id.chat_list_topbar_profile).setOnClickListener {
            val intent = Intent(this.context, EditProfileActivity::class.java)
            this.context.startActivity(intent)
        }
    }

    private fun downloadProfileImageToTopbar() {
        val profileTopbarMenuOption = context.findViewById<ImageView>(R.id.chat_list_topbar_profile)

        profileActions.getCurrentProfile { profile ->
            Log.i("ChatListActivity", "toolbar pic1: " + profile.pic1)

            if (profile.pic1.isBlank()) {
                return@getCurrentProfile
            }

            Picasso.get()
                .load(profile.pic1)
                .into(profileTopbarMenuOption)
        }
    }

    fun showBadge(menuOption: ChatListTopbarOption) {
        when(menuOption) {
            ChatListTopbarOption.SETTINGS -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_settings_badge).visibility = VISIBLE
            }
            ChatListTopbarOption.CARD_SWIPE -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_card_swipe_badge).visibility = VISIBLE
            }
            ChatListTopbarOption.LOGO -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_logo_badge).visibility = VISIBLE
            }
            ChatListTopbarOption.MATCHES -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_matches_badge).visibility = VISIBLE
            }
            ChatListTopbarOption.PROFILE -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_profile_badge).visibility = VISIBLE
            }
        }
    }

    fun hideBadge(menuOption: ChatListTopbarOption) {
        when(menuOption) {
            ChatListTopbarOption.SETTINGS -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_settings_badge).visibility = INVISIBLE
            }
            ChatListTopbarOption.CARD_SWIPE -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_card_swipe_badge).visibility = INVISIBLE
            }
            ChatListTopbarOption.LOGO -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_logo_badge).visibility = INVISIBLE
            }
            ChatListTopbarOption.MATCHES -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_matches_badge).visibility = INVISIBLE
            }
            ChatListTopbarOption.PROFILE -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_profile_badge).visibility = INVISIBLE
            }
        }
    }

    enum class ChatListTopbarOption {
        SETTINGS,
        CARD_SWIPE,
        LOGO,
        MATCHES,
        PROFILE
    }
}
