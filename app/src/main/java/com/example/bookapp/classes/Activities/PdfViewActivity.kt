package com.example.bookapp.classes.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.example.bookapp.R
import com.example.bookapp.classes.Application.ApplicationClass
import com.example.bookapp.classes.Application.Constants
import com.example.bookapp.databinding.ActivityPdfViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    var bookId = ""

    // тип користувача - передається з минулої сторінки
    private lateinit var userType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)

        firebaseAuth = FirebaseAuth.getInstance()

        // якщо користувач звичайний або неавторизований - змінити тему
        userType = intent.getStringExtra("userType")!!
        if (userType == "user" || firebaseAuth.currentUser == null) {
            changeTheme()
        }

        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!
        loadBookDetails()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pdfUrl = snapshot.child("url").value

                    loadBookFromUrl("$pdfUrl")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        ref.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1
                        binding.subTitleTv.text = "Сторінка: $currentPage/$pageCount"

                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Не вдалося завантажити PDF файл: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = View.GONE
            }
    }

    // зміна теми для неавторизованих або звичайних користувачів
    private fun changeTheme() {
        binding.toolbarRl.setBackgroundResource(R.drawable.shape_toolbar02_3)

        val color = resources.getColor(R.color.teal_200)
        // зміна кольору
        binding.progressBar.indeterminateDrawable.setColorFilter(
            color,
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }
}