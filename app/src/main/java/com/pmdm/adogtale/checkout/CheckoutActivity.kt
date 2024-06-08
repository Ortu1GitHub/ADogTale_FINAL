package com.pmdm.adogtale.checkout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pmdm.adogtale.R
import com.pmdm.adogtale.ui.CardSwipeActivity
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.CompletableFuture

private const val PAYMENT_HOSTNAME = "https://whale-app-j3uwe.ondigitalocean.app/"

class CheckoutActivity : AppCompatActivity() {
    lateinit var paymentSheet: PaymentSheet
    lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    lateinit var paymentIntentClientSecret: String
    private var backBtn: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        backBtn = findViewById(R.id.back_btn)

        prepareDataToCheckout().thenAccept {
            val checkoutButton = findViewById<Button>(R.id.checkout_button_pagar)

            checkoutButton.setOnClickListener {
                presentPaymentSheet()
            }

            Log.i("CheckoutActivity", "after prepareDataToCheckout")
        }

        paymentSheet = PaymentSheet(this, this::onPaymentSheetResult)
        Log.i("CheckoutActivity", "paymentSheet")

        backBtn?.setOnClickListener { v: View? -> onBackPressed() }

    }

    fun prepareDataToCheckout(): CompletableFuture<Void> {
        val client = OkHttpClient()
        val url = PAYMENT_HOSTNAME
        val request = Request.Builder().url(url).build()
        val responseFuture = CompletableFuture.runAsync {

            try {

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("CheckoutActivity", "Error getting API distance response");
                    Log.e("CheckoutActivity", response.toString())

                    return@runAsync
                }

                val bodyString = response.body?.string()
                val responseJson = JSONObject(bodyString)

                paymentIntentClientSecret = responseJson.getString("paymentIntent")
                customerConfig = PaymentSheet.CustomerConfiguration(
                    responseJson.getString("customer"),
                    responseJson.getString("ephemeralKey")
                )
                val publishableKey = responseJson.getString("publishableKey")
                PaymentConfiguration.init(this, publishableKey)

                Log.i("CheckoutActivity", response.toString())
            } catch (e: Exception) {
                Log.i("CheckoutActivity", "error: " + e.message)
            }
        }
        return responseFuture;
    }

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(
                    baseContext,
                    "Payment Canceled! Please, Try again",
                    Toast.LENGTH_SHORT
                ).show()
                Log.i("CheckoutActivity", "Canceled")
            }

            is PaymentSheetResult.Failed -> {
                Toast.makeText(baseContext, "Payment Failed! Please, Try again", Toast.LENGTH_SHORT)
                    .show()
                Log.i("CheckoutActivity", "Error: ${paymentSheetResult.error}")
            }

            is PaymentSheetResult.Completed -> {
                Log.i("CheckoutActivity", "Completed")
                prepareDataToCheckout()
                Toast.makeText(baseContext, "Payment done!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, CardSwipeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    fun presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = "A Dog Tale",
                customer = customerConfig,
                allowsDelayedPaymentMethods = true
            )
        )
    }

}