package com.example.bookapp.classes.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Буль ласка, зачекайте..")
        progressDialog.setCanceledOnTouchOutside(false)

        // натиснення на кнопку виходу
        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // натиснення на кнопку реєстрації
        binding.noAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        // натиснення на кнопку авторизації
        binding.loginBtn.setOnClickListener {
            validateData()
        }

        // натиснення на кпопку відновлення паролю
        binding.forgotPass.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
            finish()
        }

    }

    private fun validateData() {
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(
                this,
                "Некоректний формат адреси..",
                Toast.LENGTH_SHORT
            ).show()
        } else if (password.isEmpty()) {
            Toast.makeText(
                this,
                "Введіть пароль..",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            loginUser()
        }
    }

    private fun loginUser() {
        progressDialog.setMessage("Авторизуємося..")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // успішно
                checkUser()
            }
            .addOnFailureListener { e ->
                // помилка
                Toast.makeText(
                    this,
                    "Не вдалося авторизуватися: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog.dismiss()
            }

    }

    private fun checkUser() {
        progressDialog.setMessage("Перевіряємо користувача..")
        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    val intent: Intent

                    // користувач - або звичайний, або адміністратор
                    val userType = snapshot.child("userType").value

                    intent = when (userType) {
                        "user" -> Intent(
                            this@LoginActivity,
                            DashboardUserActivity::class.java
                        )
                        else -> Intent(
                            this@LoginActivity,
                            DashboardAdminActivity::class.java
                        )
                    }
                    startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

    }

}