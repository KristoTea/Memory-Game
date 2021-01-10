package hr.fer.ruzaosa.lecture4.ruzaosa.k.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import hr.fer.ruzaosa.lecture4.ruzaosa.R
import hr.fer.ruzaosa.lecture4.ruzaosa.k.retrofit.RetrofitInstance
import hr.fer.ruzaosa.lecture4.ruzaosa.k.retrofit.User
import hr.fer.ruzaosa.lecture4.ruzaosa.k.retrofit.UsersService
import kotlinx.android.synthetic.main.activity_registration.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val etUsername = findViewById<EditText>(R.id.usernameRegistration)
        val etFirstName = findViewById<EditText>(R.id.firstName)
        val etLastName = findViewById<EditText>(R.id.lastName)
        val etEmail = findViewById<EditText>(R.id.emailRegistration)
        val etPassword = findViewById<EditText>(R.id.passwordRegistration)
        val btnRegister = findViewById<Button>(R.id.registerButton)

        btnRegister.setOnClickListener {
            btnRegister.isEnabled = false
            btnRegister.text = "REGISTERING"
            if (TextUtils.isEmpty(etUsername.text.toString()) || TextUtils.isEmpty(etFirstName.text.toString()) ||
                    TextUtils.isEmpty(etLastName.text.toString()) || TextUtils.isEmpty(etEmail.text.toString()) ||
                    TextUtils.isEmpty(etPassword.text.toString())) {
                btnRegister.isEnabled = true
                btnRegister.text = "REGISTER"
                Toast.makeText(
                        this@RegistrationActivity,
                        "One or more empty fields!",
                        Toast.LENGTH_LONG
                ).show()
            } else {
                val firstName = etFirstName.text.toString()
                val lastName = etLastName.text.toString()
                val username = etUsername.text.toString()
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    val token = task.result.toString()
                    signup(firstName, lastName, username, email, password, token)
                })
            }
        }
    }

    private fun signup(firstName: String, lastName: String, username: String,
                       email: String, password: String, token: String) {

        val btnRegister = findViewById<Button>(R.id.registerButton)
        val retIn = RetrofitInstance.getRetrofit().create(UsersService::class.java)//UVIK POČETAK ZA REST POZIV
        val registerInfo = User(firstName, lastName, username, email, password, token)

        retIn.registerUser(registerInfo).enqueue(object :
                Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                btnRegister.isEnabled = true
                btnRegister.text = "REGISTER"
                Toast.makeText(
                        this@RegistrationActivity,
                        t.message,
                        Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    Toast.makeText(this@RegistrationActivity, "Registration success!", Toast.LENGTH_SHORT)
                            .show()
                    startActivity(Intent(this@RegistrationActivity, LogInActivity::class.java))
                } else {
                    btnRegister.isEnabled = true
                    btnRegister.text = "REGISTER"
                    Toast.makeText(this@RegistrationActivity, "Registration failed!", Toast.LENGTH_SHORT)
                            .show()
                }
            }
        })
    }
}