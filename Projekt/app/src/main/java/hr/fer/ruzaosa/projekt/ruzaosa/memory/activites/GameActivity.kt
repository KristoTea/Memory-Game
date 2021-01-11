package hr.fer.ruzaosa.projekt.ruzaosa.memory.activites

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import hr.fer.ruzaosa.lecture4.ruzaosa.R
import hr.fer.ruzaosa.lecture4.ruzaosa.k.activites.MenuActivity
import hr.fer.ruzaosa.lecture4.ruzaosa.k.retrofit.RetrofitInstance
import hr.fer.ruzaosa.lecture4.ruzaosa.k.retrofit.User
import hr.fer.ruzaosa.projekt.ruzaosa.memory.retrofit.GameBody
import hr.fer.ruzaosa.projekt.ruzaosa.memory.retrofit.GameService
import kotlinx.android.synthetic.main.*
import kotlinx.android.synthetic.main.activity_game.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Math.*
import java.util.stream.DoubleStream.builder
import java.util.stream.Stream.builder
import com.google.firebase.messaging.Message


class GameActivity : AppCompatActivity() {

    private lateinit var buttons: List<ImageButton>
    private lateinit var cards: List<Card>
    private var indexOfSingleSelectedCard: Int = -1
    private var foundPairs: Int = 0
    private lateinit var username: String

    // dodano da bi radila fja chooseWinner. treba ih inicijalizirati ispravno!
    val user1 = User("firstName", "lastName", "username", "email", "password", "token")
    val user2 = User("firstName2", "lastName2", "username2", "email2", "password2", "token2")
    val gameId = 1L

    val retIn = RetrofitInstance.getRetrofit().create(GameService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        //povuci username iz firebase

        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) { }

        myplayer.setBackgroundResource(R.drawable.roundbutton)
            // kristo istuc cu te zbog ovoga >:(A
        //myPlayerUsername.setText(username)
        quitGameBtn.setOnClickListener{ finish() }
        timer.start()
        progressBar.apply {
            progressBarColor = Color.WHITE
            progressMax = 100f
            progressBarWidth = 6f
        }

        val images = mutableListOf(R.drawable.bijela, R.drawable.crvena, R.drawable.zuta, R.drawable.narancasta,
                R.drawable.crna, R.drawable.siva, R.drawable.roza, R.drawable.smeda,
                R.drawable.plava, R.drawable.tamnoljubicasta, R.drawable.ljubicasta, R.drawable.tamnoplava,
                R.drawable.zelena, R.drawable.svijetlozelena, R.drawable.plavozelena)

        images.addAll(images) // podupla se sve

        images.shuffle()

        buttons = listOf(tile1, tile2, tile3, tile4, tile5, tile6, tile7, tile8, tile9, tile10,
                tile11, tile12, tile13, tile14, tile15, tile16, tile17, tile18, tile19, tile20,
                tile21, tile22, tile23, tile24, tile25, tile26, tile27, tile28, tile29, tile30)

        cards = buttons.indices.map { index ->
            Card(images[index])
        }

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                val flip: Animation? = AnimationUtils.loadAnimation(this, R.anim.flip)
                if (!cards[index].isOpened) button.startAnimation(flip)
                updateModels(index)
                updateViews()
            }
        }
    }

    private fun updateViews() {
        val fadeOut: Animation? = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        cards.forEachIndexed { index, card ->
            val button = buttons[index]
            if (card.isMatched) {
                if (!card.isFadedOut) {
                    button.startAnimation(fadeOut)
                    card.isFadedOut = true
                }
                button.alpha = 0.1F
            }
            button.setImageResource(if (card.isOpened) card.id else R.drawable.nalicje)
        }
    }

    private fun updateModels(position: Int) {
        val card = cards[position]

        if (card.isOpened) {
            Toast.makeText(this, "Invalid move!", Toast.LENGTH_SHORT).show()
            return
        }

        if (indexOfSingleSelectedCard == -1) {
            // vec su otvorene 2 ili nijedna
            // treba ponistit sve i zapamtit otvorenu kartu
            restoreCards()
            this.indexOfSingleSelectedCard = position
        } else {
            // otvorena je jedna karta i mi smo sad drugu
            val flipAndFadeOut: Animation? = AnimationUtils.loadAnimation(this, R.anim.flip_fade_out)
            var previous: Int = indexOfSingleSelectedCard // indeks prethodno otvorene

            for (i in 0..29) { buttons[i].isClickable = false }

            if (!checkForMatch(indexOfSingleSelectedCard, position)) {
                Handler().postDelayed({
                    buttons[previous].startAnimation(flipAndFadeOut)
                    buttons[position].startAnimation(flipAndFadeOut)
                    buttons[previous].setImageResource(R.drawable.nalicje)
                    buttons[position].setImageResource(R.drawable.nalicje)
                    cards[previous].isOpened = false
                    cards[position].isOpened = false
                    for (i in 0..29) {
                        buttons[i].isClickable = true
                    }

                }, 1000)
            }
            else {
                Handler().postDelayed({
                    for (i in 0..29) {
                        buttons[i].isClickable = true
                    }
                }, 1000)
            }

            indexOfSingleSelectedCard = -1
        }
        card.isOpened = !card.isOpened
    }

    private fun restoreCards() {
        for (card in cards) {
            if (!card.isMatched) {
                card.isOpened = false
            }
        }
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if (cards[position1].id == cards[position2].id) {
            cards[position1].isMatched = true
            cards[position2].isMatched = true

            progressBar.apply {
                setProgressWithAnimation(progress + 6.66666f, 400)
            }

            foundPairs=foundPairs+1;
            if(foundPairs==15){
                timer.stop()
                Handler().postDelayed({
                    startActivity(Intent(this@GameActivity, MenuActivity::class.java))
                }, 400)
                Toast.makeText(this@GameActivity, "You have completed the puzzle in " + timer.text + "s", Toast.LENGTH_LONG)
                    .show()
            }
            return true
        }
        return false
    }
    private fun endGAme(game: GameBody) {
        val retIn = RetrofitInstance.getRetrofit().create(GameService::class.java)
        //if challenger won
        retIn.challengerFinished(game.gameId).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@GameActivity, "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                //or
                //Toast.makeText(this@GameActivity, "You lost the game :(", Toast.LENGTH_SHORT)
                //.show()
            }

            override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
            ) {
                Toast.makeText(this@GameActivity, "Congratulations! You won!", Toast.LENGTH_SHORT)
                        .show()
            }
        })
        retIn.challengedFinished(game.gameId).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@GameActivity, "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                //or
                //Toast.makeText(this@GameActivity, "You lost the game :(", Toast.LENGTH_SHORT)
                //.show()
            }

            override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
            ) {
                //Toast.makeText(this@GameActivity, "Congratulations! You won!", Toast.LENGTH_SHORT)
                //.show()
                //send notification to challanged via token ?
                // This registration token comes from the client FCM SDKs.
                // This registration token comes from the client FCM SDKs.
                val registrationToken = game.challenged.token

// See documentation on defining a message payload.

// See documentation on defining a message payload.
                val message: Message = Message.builder()
                        .putData("You")
                        .putData("lost")
                        .setToken(registrationToken)
                        .build()

// Send a message to the device corresponding to the provided
// registration token.

// Send a message to the device corresponding to the provided
// registration token.
                val response: String = FirebaseMessaging.getInstance().send(message)
// Response is a message ID string.
// Response is a message ID string.
                println("Successfully sent message: $response")
            }
        })
    }
}
