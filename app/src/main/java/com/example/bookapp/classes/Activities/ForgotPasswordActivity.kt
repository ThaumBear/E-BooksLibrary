package com.example.bookapp.classes.Activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapp.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Буль ласка, зачекайте")
        progressDialog.setCanceledOnTouchOutside(false)

        // натиснення на кнопку "назад"
        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // натиснення на кнопку надсилання інструкцій
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        email = binding.emailEt.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(
                this,
                "Введіть поштову адресу..",
                Toast.LENGTH_SHORT
            ).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(
                this,
                "Некоректний формат адреси..",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        progressDialog.setMessage("Надсилаємо інструкції щодо скидання паролю на $email..")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Інструкції надіслано на $email!",
                    Toast.LENGTH_SHORT
                ).show()

            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Не вдалося надіслати інструкції: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}