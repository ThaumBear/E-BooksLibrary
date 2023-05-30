package com.example.bookapp.classes.Activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapp.databinding.ActivityRegisterBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var name = ""
    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Буль ласка, зачекайте..")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            startActivity(Intent(this,
                                              LoginActivity::class.java))
            finish()
        }

        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {

        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(
                this,
                "Введіть Ваше ім'я..",
                Toast.LENGTH_SHORT
            ).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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
        } else if (cPassword.isEmpty()) {
            Toast.makeText(
                this,
                "Підтвердіть пароль..",
                Toast.LENGTH_SHORT
            ).show()
        } else if (password != cPassword) {
            Toast.makeText(
                this,
                "Паролі не співпадають..",
                Toast.LENGTH_SHORT
            ).show()
        }
        else {
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        progressDialog.setMessage("Створюємо акаунт..")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // успішно
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                // помилка
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Не вийшло зареєструвати акаунт: ${e.message}..",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateUserInfo() {
        progressDialog.setMessage("Зберігаємо дані..")

        val timestamp = System.currentTimeMillis()

        val uid = firebaseAuth.uid
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" // empty account icon
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                // успішно
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Акаунт створено!",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(
                    Intent(
                        this@RegisterActivity,
                        DashboardUserActivity::class.java
                    )
                )
                finish()
            }
            .addOnFailureListener { e ->
                // помилка
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Не вийшло зберегти користувача: ${e.message}..",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}