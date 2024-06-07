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
import com.pmdm.adogtale.ui.topbar.card_swipe.CardSwipeTopbar.CardSwipeTobarOption
import com.squareup.picasso.Picasso

class ChatListTobar(private val context: AppCompatActivity) {

    private val profileActions: ProfileActions = ProfileActions();

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

        context.findViewById<ImageView>(R.id.chat_list_topbar_settings).setOnClickListener {

        }
        context.findViewById<ImageView>(R.id.chat_list_topbar_card_swipe).setOnClickListener {
            val intent = Intent(this.context, CardSwipeActivity::class.java)
            context.startActivity(intent)
        }
        context.findViewById<ImageView>(R.id.chat_list_topbar_matches).setOnClickListener {
            val intent = Intent(this.context, MatchesListActivity::class.java)
            this.context.startActivity(intent);
        }
        context.findViewById<ImageView>(R.id.chat_list_topbar_profile).setOnClickListener {

        }
    }

    private fun downloadProfileImageToTopbar() {
        val profileTopbarMenuOption = context.findViewById<ImageView>(R.id.chat_list_topbar_profile)

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

    fun showBadge(menuOption: ChatListTopbarOption){
        when(menuOption){
            ChatListTopbarOption.SETTINGS -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_settings_badge).setVisibility(VISIBLE)
            }
            ChatListTopbarOption.CARD_SWIPE -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_card_swipe_badge).setVisibility(VISIBLE)
            }
            ChatListTopbarOption.LOGO -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_logo_badge).setVisibility(VISIBLE)
            }
            ChatListTopbarOption.MATCHES -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_matches_badge).setVisibility(VISIBLE)
            }
            ChatListTopbarOption.PROFILE -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_profile_badge).setVisibility(VISIBLE)
            }
        }
    }

    fun hiddeBadge(menuOption: ChatListTopbarOption){
        when(menuOption){
            ChatListTopbarOption.SETTINGS -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_settings_badge).setVisibility(INVISIBLE)
            }
            ChatListTopbarOption.CARD_SWIPE -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_card_swipe_badge).setVisibility(INVISIBLE)
            }
            ChatListTopbarOption.LOGO -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_logo_badge).setVisibility(INVISIBLE)
            }
            ChatListTopbarOption.MATCHES -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_matches_badge).setVisibility(INVISIBLE)
            }
            ChatListTopbarOption.PROFILE -> {
                context.findViewById<ImageView>(R.id.chat_list_topbar_profile_badge).setVisibility(INVISIBLE)
            }
        }
    }

    enum class ChatListTopbarOption{
        SETTINGS,
        CARD_SWIPE,
        LOGO,
        MATCHES,
        PROFILE
    }

}