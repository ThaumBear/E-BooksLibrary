package com.example.bookapp.classes.Activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.bookapp.R
import com.example.bookapp.classes.Application.ApplicationClass
import com.example.bookapp.classes.Application.Constants
import com.example.bookapp.databinding.ActivityBookDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class BookDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookDetailBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var bookTitle = ""
    private var bookUrl = ""
    private var bookId = ""
    private var isInMyFavorite = false

    // тип користувача - передається з минулої сторінки
    private lateinit var userType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailBinding.inflate(layoutInflater)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Буль ласка, зачекайте..")
        progressDialog.setCanceledOnTouchOutside(false)

        // перевірка типу користувача
        firebaseAuth = FirebaseAuth.getInstance()
        userType = intent.getStringExtra("userType")!!
        checkUser()

        // одержання id книги
        bookId = intent.getStringExtra("bookId")!!

        // якщо користувач авторизований - перевірити, че є книга улюбленою
        if (userType != "none") {
            checkIsFavorite()
        }

        // збільшити кількість переглядів
        ApplicationClass.incrementCount(bookId, "viewsCount")
        loadBookDetails()

        // натиснення на кнопку "назад"
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // натиснення на кнопку перегляду книги
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            intent.putExtra("userType", userType)
            startActivity(intent)
        }

        // натиснення на кнопку завантаження книги
        binding.downloadBookBtn.setOnClickListener {
            // якщо права на завантаження немає - затребувати їх
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                downloadBook()
            } else {
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        // натиснення на кнопку додання до улюблених
        binding.favoriteBtn.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(
                    this,
                    "Ви не авторизовані..",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (isInMyFavorite) {
                    ApplicationClass.removeFromFavorite(this, bookId, bookTitle)
                } else {
                    addToFavorite()
                }
            }
        }
    }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                downloadBook()
            } else {
                Toast.makeText(
                    this,
                    "Доступ заборонено..",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun downloadBook() {
        progressDialog.setMessage("Завантажуємо файл..")
        progressDialog.show()

        var ref = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        ref.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                saveToDownloadsFolder(bytes)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Не вдалося завантажити файл: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveToDownloadsFolder(bytes: ByteArray?) {
        val fileName = "${System.currentTimeMillis()}.pdf"

        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            downloadsFolder.mkdirs() // створення директорії, якщо такої не існує

             val filePath = downloadsFolder.path + "/" + fileName

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(
                this,
                "Файл завантажено за шляхом: $filePath",
                Toast.LENGTH_LONG
            ).show()
            progressDialog.dismiss()
            // збільшити кількість завантажнь
            ApplicationClass.incrementCount(bookId, "downloadsCount")

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Не вдалося завантажити файл: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    // одержання дати
                    val date = ApplicationClass.formatTimeStamp(timestamp.toLong())

                    // одержання категорії
                    ApplicationClass.loadCategory(categoryId, binding.categoryTv)

                    // одержання сторінок та розміру PDF
                    ApplicationClass.loadPdfFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )
                    ApplicationClass.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)

                    // заповнення даних
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadsCount
                    binding.dateTv.text = date

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun checkIsFavorite() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()

                    if (isInMyFavorite) {
                        // зміна виду кнопки
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.remove_white,
                            0,
                            0)
                        binding.favoriteBtn.text = "Прибрати з улюблених"
                    } else {
                        // зміна виду кнопки
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.favorite_white,
                            0,
                            0)
                        binding.favoriteBtn.text = "Додати до улюблених"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

    }

    private fun addToFavorite() {
        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Книга ${bookTitle} додана до улюблених!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Не вдалося додати до улюблених: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // перевірка користувача; якщо користувач - звичайний, або неавторизований, то тема змінюється
    private fun checkUser() {
        val layout: Int

        // якщо користувач - звичайни, змінюється тема
        when (userType) {
            "none" -> {
                changeTheme()
                layout = R.layout.activity_splash
                binding.favoriteBtn.visibility = View.GONE
            }
            "user" -> {
                changeTheme()
                layout = R.layout.activity_splash
            }
            else -> layout = R.layout.splash_admin
        }

        ApplicationClass.showScreen(layout, binding.root, this@BookDetailActivity)
    }

    // зміна теми для неавторизованих або звичайних користувачів
    private fun changeTheme() {
        binding.main.setBackgroundResource(R.drawable.back01)
        binding.toolbarRl.setBackgroundResource(R.drawable.shape_toolbar02_3)

        val color = resources.getColor(R.color.teal_200)

        // зміна кольору
        binding.progressBar.indeterminateDrawable.setColorFilter(
            color,
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        binding.readBookBtn.setBackgroundColor(color)
        binding.downloadBookBtn.setBackgroundColor(color)
        binding.favoriteBtn.setBackgroundColor(color)
    }
}