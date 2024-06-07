package com.pmdm.adogtale.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.pmdm.adogtale.R
import com.pmdm.adogtale.chat.ChatListActivity
import com.pmdm.adogtale.controller.CardStackAdapter
import com.pmdm.adogtale.controller.CardStackCallback
import com.pmdm.adogtale.controller.OtherProfileActions
import com.pmdm.adogtale.controller.ProfileActions
import com.pmdm.adogtale.matches.MatchesListActivity
import com.pmdm.adogtale.model.Itemx
import com.pmdm.adogtale.model.Profile
import com.pmdm.adogtale.model.ProfilesMatching
import com.pmdm.adogtale.model.User
import com.pmdm.adogtale.push_notification.DeviceTokenHandler
import com.pmdm.adogtale.ui.topbar.card_swipe.CardSwipeTopbar
import com.pmdm.adogtale.utils.FirebaseUtil
import com.yuyakaido.android.cardstackview.*
import java.util.*
import java.util.concurrent.CompletableFuture
import android.view.Window
import android.view.WindowManager
import com.pmdm.adogtale.push_notification.PushNotificationData
import com.pmdm.adogtale.push_notification.PushNotificationSender
import com.pmdm.adogtale.ui.statusbar.StatusbarColorHandler


class CardSwipeActivity : AppCompatActivity() {
    private val TAG = "CardSwipeActivity"

    private lateinit var adapter: CardStackAdapter
    private lateinit var profileMatching: ProfilesMatching
    private val TAG2 = "ORTU"
    private lateinit var profileActions: ProfileActions
    private lateinit var otherProfileActions: OtherProfileActions


    private lateinit var firebaseAuth: FirebaseAuth
    private var db = FirebaseFirestore.getInstance()

    private var userDogProfile: Profile? = null
    private var otherDogProfile: Profile? = null
    var targetEmail: String? = null
    var isCardFullySwiped = false
    var counter = 0
    val items = mutableListOf<Itemx>()
    var manager: CardStackLayoutManager? = null

    private lateinit var cardSwipeTopbar: CardSwipeTopbar;
    private val deviceTokenHandler: DeviceTokenHandler = DeviceTokenHandler();
    private var toolbar: Toolbar? = null
    private val firebaseUtil: FirebaseUtil = FirebaseUtil();
    private lateinit var optionsBtn: Button
    private val pushNotificationSender: PushNotificationSender = PushNotificationSender()

    private val statusbarColorHandler: StatusbarColorHandler = StatusbarColorHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statusbarColorHandler.setStatusbarBackgroundColor(this, Color.WHITE)


        // Configuración del diseño de la actividad
        setContentView(R.layout.activity_card_swipe)

        // Inicialización de la base de datos y otras importaciones
        initDatabase()
        initImports()

        cardSwipeTopbar = CardSwipeTopbar(this)
        this.cardSwipeTopbar.configureTopbar()

        // Obtener el perfil del usuario actual
        getCurrentProfile()
            .thenAccept { userEmail ->
                FirebaseMessaging.getInstance()
                    .token
                    .addOnSuccessListener { result ->
                        if (Objects.isNull(result)) {
                            return@addOnSuccessListener;
                        }

                        firebaseUtil.getCurrentFirebaseUser();

                        firebaseUtil.getCurrentUser { user: User ->
                            Log.i("CardSwipeActivity", "userEmail attached to token: " + user.email)
                            deviceTokenHandler.storeDeviceToken(user.email, result);

                            firebaseUtil.getCountUnreadMessagesInAllChatrooms(user.email)
                                .thenAccept{ result ->
                                    if(result > 0){
                                        this.cardSwipeTopbar.showBadge(CardSwipeTopbar.CardSwipeTobarOption.CHAT)
                                    }
                                    Log.i("CardSwipeActivity", "finished count of messages: "+result)
                                }

                            firebaseUtil.getCountUnCheckedMatches(user.email)
                                .thenAccept{ result ->
                                    if(result > 0){
                                        this.cardSwipeTopbar.showBadge(CardSwipeTopbar.CardSwipeTobarOption.MATCHES)
                                    }
                                    Log.i("CardSwipeActivity", "finished count of matches: "+result)
                                }
                        }

                    }
            }

        val reloadButton = findViewById<Button>(R.id.reaload_card_swipe_activity_button)

        reloadButton.setOnClickListener {
            finish()
            startActivity(getIntent())
        }
    }

    // Paginate profile results
    /*private fun paginate() {
        val old = adapter.items
        addList().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("paginación", "paginación")
                val new = task.result
                if (new.isEmpty()) {
                    // Si no hay más perfiles disponibles, muestra el texto
                    Toast.makeText(this, "No more profiles available 1", Toast.LENGTH_LONG).show()
                } else {
                    val callback = CardStackCallback(old, new)
                    val res = DiffUtil.calculateDiff(callback)
                    adapter.items = new
                    res.dispatchUpdatesTo(adapter)
                }
            } else {
                Toast.makeText(
                    this,
                    "No se pudo cargar la información de la base de datos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnCompleteListener {
            if (!it.isSuccessful || it.result.isEmpty()) {
                // Si la tarea no tuvo éxito o la lista de perfiles es vacía,
                // muestra el texto "no more profiles available"
                Toast.makeText(this, "No more profiles available 2", Toast.LENGTH_LONG).show()
            }
        }
    }*/
    private fun paginate() {
        addList().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val new = task.result
                Log.d("paginate", "Data fetched: $new")
                if (new.isNullOrEmpty()) {
                    Toast.makeText(this, "No more profiles available", Toast.LENGTH_LONG).show()
                } else {

                    val callback = CardStackCallback(adapter.items, new)
                    val diffResult = DiffUtil.calculateDiff(callback)
                    adapter.items = new
                    Log.d("paginate", "Adapter items: ${adapter.items}")
                    Toast.makeText(this, "Success: " + adapter.itemCount, Toast.LENGTH_LONG).show()
                    diffResult.dispatchUpdatesTo(adapter)
                }
            } else {
                Toast.makeText(this, "Failed to load database information", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // Add compatible profile into a list
    private fun addList(): Task<List<Itemx>> {
        val taskSource = TaskCompletionSource<List<Itemx>>()

        // Llamar a getOtherProfiles y obtener la lista de perfiles
        otherProfileActions.getOtherProfiles(userDogProfile!!) { profiles ->
            // Limpiar la lista antes de agregar nuevos elementos
            items.clear()

            // Iterar sobre cada perfil y agregar un elemento Itemx a la lista de items
            profiles.forEach { profile ->
                items.add(
                    Itemx(
                        profile.pic1,
                        profile.name,
                        profile.age,
                        profile.shortDescription,
                        profile.userEmail
                    )
                )
            }
            // Añade una card final
           /* items.add(
                Itemx(
                    getResourceUri(R.drawable.end_card),
                    "",
                    "",
                    "",
                    "",
                )
            )*/

            // Establecer el resultado en la tarea
            if (!taskSource.task.isComplete) {
                taskSource.setResult(items)
            }
        }

        return taskSource.task
    }

    // Last card path
    private fun getResourceUri(@DrawableRes drawableId: Int): String {
        return Uri.parse("android.resource://" + packageName + "/" + drawableId).toString()
    }

    // Get logged in user's dog profile
    private fun getCurrentProfile(): CompletableFuture<String> {
        val response = CompletableFuture<String>();
        profileActions.getCurrentProfile { profile ->
            userDogProfile = profile
            Toast.makeText(this, userDogProfile?.name, Toast.LENGTH_SHORT).show()
            setupCardStackView()
            response.complete(profile.userEmail)
        }
        return response
    }

    fun setupCardStackView() {
        val cardStackView = findViewById<CardStackView>(R.id.card_stack_view)

//        var manager = CardStackLayoutManager(this, null)
        manager = CardStackLayoutManager(this, null)

        manager!!.setStackFrom(StackFrom.None)
        manager!!.setVisibleCount(3)
        manager!!.setTranslationInterval(8.0f)
        manager!!.setScaleInterval(0.95f)
        manager!!.setSwipeThreshold(0.3f)
        manager!!.setMaxDegree(20.0f)
        manager!!.setDirections(Direction.HORIZONTAL)
        manager!!.setCanScrollHorizontal(true)
        manager!!.setSwipeableMethod(SwipeableMethod.Manual)
        manager!!.setOverlayInterpolator(LinearInterpolator())

        manager = CardStackLayoutManager(this, object : CardStackListener {

            override fun onCardDragging(direction: Direction?, ratio: Float) {
                Log.d(TAG, "onCardDragging: d=" + direction?.name + " ratio=" + ratio)
            }

            override fun onCardSwiped(direction: Direction?) {
                when (direction) {
                    Direction.Right -> {
                        Toast.makeText(
                            this@CardSwipeActivity,
                            "Direction Right",
                            Toast.LENGTH_SHORT
                        ).show()
                        counter++
                        Log.i("Counter", counter.toString())

                        Log.i("itemX", items[manager!!.topPosition - 1].userEmail)
                        profileMatching = ProfilesMatching(
                            userDogProfile?.userEmail,
                            userDogProfile?.name,
                            items[manager!!.topPosition - 1].userEmail,
                            items[manager!!.topPosition - 1].name
                        )
                        Log.i("Mi profile matching", profileMatching.user_target.toString())
                        saveLike(profileMatching)
                        checkingANewMatchExist()
                    }

                    else -> {
                        Toast.makeText(this@CardSwipeActivity, "Take care", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                // Recarga la cola de cards
                if (manager!!.topPosition == adapter.itemCount) {
                    Toast.makeText(
                        this@CardSwipeActivity,
                        "Nothing more to show",
                        Toast.LENGTH_SHORT
                    ).show()
                    paginate()
                    val cardContainer = findViewById<View>(R.id.card_stack_view)
                    cardContainer.setVisibility(View.GONE)
                }
//                Log.i("man - adapt",manager.topPosition.toString() + " "+ adapter.itemCount.toString())
            }

            override fun onCardRewound() {
                Log.d(TAG, "onCardRewound: " + manager!!.topPosition)
            }

            override fun onCardCanceled() {
                Log.d(TAG, "onCardCanceled: " + manager!!.topPosition)
            }

            override fun onCardAppeared(view: View?, position: Int) {
                val tv = view?.findViewById<TextView>(R.id.item_name)
                Log.d(TAG, "onCardAppeared: " + position + ", name: " + tv?.text)
            }

            override fun onCardDisappeared(view: View?, position: Int) {
                val tv = view?.findViewById<TextView>(R.id.item_name)
                Log.d(TAG, "onCardDisappeared: " + position + ", name: " + tv?.text)
            }
        })

        // Loads card with items from list
        addList().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("addCards", "adding cards");
                val items = task.result
                adapter = CardStackAdapter(items, manager!!) { checkNoMoreCards() }
                cardStackView.layoutManager = manager
                cardStackView.adapter = adapter
                cardStackView.itemAnimator = DefaultItemAnimator()
//                checkNoMoreCards()
            } else {
                Toast.makeText(this, "Could not load database information", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun checkNoMoreCards() {
        Log.i("CardSwipeActivity", "manager topPosition: " + manager!!.topPosition)
        if (manager!!.topPosition == adapter.itemCount) {
            val cardContainer = findViewById<View>(R.id.card_stack_view)
            cardContainer.setVisibility(View.GONE)
        }
    }

    private fun saveLike(profilesMatching: ProfilesMatching) {
        // Método para guardar el perfil que ha sido gustado
        val data = hashMapOf(
            "user_original" to profilesMatching.user_original,
            "profile_original" to profilesMatching.profile_original,
            "user_target" to profilesMatching.user_target,
            "profile_target" to profilesMatching.profile_target,
            "likeAlreadyChecked" to profilesMatching.likeAlreadyChecked,
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("profiles_matching")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Like guardado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el like: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun checkingANewMatchExist() {
        val db = FirebaseFirestore.getInstance()
        db.collection("profiles_matching")
            .whereEqualTo("user_target", profileMatching.user_original)
            .whereEqualTo("profile_target", profileMatching.profile_original)
            .whereEqualTo("user_original", profileMatching.user_target)
            .whereEqualTo("profile_original", profileMatching.profile_target)
            .whereEqualTo("likeAlreadyChecked", false)
            .get()
            .addOnSuccessListener {
                it
                for (documentos in it) {

                    sendPushNotification(profileMatching.user_target!!)

                    //MATCH!
                    Toast.makeText(this, "IT'S A MATCH!", Toast.LENGTH_SHORT).show()
                    Log.d("ORTU2", "${documentos.data}")
                    val intent = Intent(this, SplashScreenActivity::class.java)
                    intent.putExtra("targetEmail", profileMatching.user_target)
                    intent.putExtra("pic_original", items[manager?.topPosition?.minus(1)!!].image)
                    intent.putExtra("pic_target", userDogProfile?.pic1)
                    intent.putExtra("profile_original", profileMatching.profile_original)
                    intent.putExtra("profile_target", profileMatching.profile_target)
                    startActivity(intent)
                }
            }
    }

    // Initialize database instance
    private fun initDatabase() {
        firebaseAuth = FirebaseAuth.getInstance()
    }


    // Initialize other class imports
    private fun initImports() {
        profileActions = ProfileActions()
        otherProfileActions = OtherProfileActions()
    }

    private fun sendPushNotification(targetUserEmail: String){

        firebaseUtil.currentUserDetails()
            .get()
            .addOnCompleteListener { currentUserDetailsTask ->
                firebaseUtil.getCurrentUser { currentUser ->
                    firebaseUtil.getOtherUser(targetUserEmail){ targetUser ->
                        val notificationData = PushNotificationData(
                            "Nuevo MATCH!!",
                            String.format("Tienes un match del usuario %s", currentUser.username),
                            currentUser,
                            targetUser
                        );
                        pushNotificationSender.sendNotification(notificationData)
                    }

                }
        }

    }

}

/*
     private fun addList(): List<Itemx> {
        items.add(Itemx("https://upload.wikimedia.org/wikipedia/commons/thumb/e/ee/1AKC_Maltese_Dog_Show_2011.jpg/250px-1AKC_Maltese_Dog_Show_2011.jpg", name, age, user))
        items.add(Itemx("https://pamipe.com/wiki/wp-content/uploads/2022/09/Bichon-Maltes-2.jpg", name, age, user))
        items.add(Itemx("https://upload.wikimedia.org/wikipedia/commons/thumb/e/ee/1AKC_Maltese_Dog_Show_2011.jpg/250px-1AKC_Maltese_Dog_Show_2011.jpg", name, age, user))
        items.add(Itemx("https://www.kiwoko.com/blogmundoanimal/wp-content/uploads/2023/06/bichon-maltes-informacion.jpg", name, age, user))
        items.add(Itemx("https://upload.wikimedia.org/wikipedia/commons/thumb/e/ee/1AKC_Maltese_Dog_Show_2011.jpg/250px-1AKC_Maltese_Dog_Show_2011.jpg", name, age, user))

        items.add(Itemx("https://upload.wikimedia.org/wikipedia/commons/thumb/e/ee/1AKC_Maltese_Dog_Show_2011.jpg/250px-1AKC_Maltese_Dog_Show_2011.jpg", "Markonah", "24", "Jember"))
        items.add(Itemx("https://upload.wikimedia.org/wikipedia/commons/thumb/e/ee/1AKC_Maltese_Dog_Show_2011.jpg/250px-1AKC_Maltese_Dog_Show_2011.jpg", "Marpuah", "20", "Malang"))
        items.add(Itemx("https://www.kiwoko.com/blogmundoanimal/wp-content/uploads/2023/06/bichon-maltes-informacion.jpg", "Sukijah", "27", "Jonggol"))
        items.add(Itemx("https://upload.wikimedia.org/wikipedia/commons/thumb/e/ee/1AKC_Maltese_Dog_Show_2011.jpg/250px-1AKC_Maltese_Dog_Show_2011.jpg", "Markobar", "19", "Bandung"))
        items.add(Itemx("https://pamipe.com/wiki/wp-content/uploads/2022/09/Bichon-Maltes-2.jpg", "Marmut", "25", "Hutan"))

        return items
    }*/



