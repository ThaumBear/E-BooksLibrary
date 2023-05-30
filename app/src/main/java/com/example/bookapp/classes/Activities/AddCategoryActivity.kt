package com.example.bookapp.classes.Activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookapp.databinding.ActivityAddCategoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Будь ласка, зачекайте..")
        progressDialog.setCanceledOnTouchOutside(false)

        // натиснення на кнопку виходу
        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, DashboardAdminActivity::class.java))
            finish()
        }

        // натиснення на кнопку завантаження
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        category = binding.categoryEt.text.toString().trim()

        if (category.isEmpty()) {
            Toast.makeText(this,
                "Введіть категорію..",
                Toast.LENGTH_SHORT).show()
        } else {
            addCategory()
        }
    }

    private fun addCategory() {
        progressDialog.show()

        val timestamp = System.currentTimeMillis()
        val hashMap = HashMap<String, Any>()

        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                // успішно
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Категорію успішно додано!",
                    Toast.LENGTH_SHORT
                ).show()

            }
            .addOnFailureListener { e ->
                // помилка
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Не вдалося додати категорію: ${e.message}..",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }
}