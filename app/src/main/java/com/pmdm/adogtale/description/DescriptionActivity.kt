package com.pmdm.adogtale.description

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pmdm.adogtale.R
import com.pmdm.adogtale.controller.ProfileActions
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import java.util.stream.Collectors
import java.util.stream.Stream

class DescriptionActivity : AppCompatActivity() {

    private val profileActions: ProfileActions = ProfileActions();
    private var backBtn: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        backBtn = findViewById(R.id.back_btn)

        val payload = intent.getStringExtra("targetEmail") as String

        profileActions.getProfileFromEmail(payload){ profileToShow ->

            val images =
                Stream.of(
                    profileToShow.pic1,
                    profileToShow.pic2,
                    profileToShow.pic3,
                    profileToShow.pic4
                )
                    .filter{ image -> !image.isBlank() }
                    .collect(Collectors.toList())

            setupScreen(images, profileToShow.name, profileToShow.shortDescription)
        }

        backBtn?.setOnClickListener { v: View? -> onBackPressed() }

    }

    fun setupScreen(images: List<String>, buddyName: String, buddyShortDescription: String){

        val carousel: ImageCarousel = findViewById(R.id.description_image_carousel)

        val carouselItems =
            images.stream()
                .map{ image -> CarouselItem( imageUrl = image ) }
                .collect(Collectors.toList())

        carousel.setData(carouselItems)

        val title = findViewById<TextView>(R.id.description_title)
        title.setText(buddyName)

        val text = findViewById<TextView>(R.id.description_text)
        text.setText(buddyShortDescription)
    }


}