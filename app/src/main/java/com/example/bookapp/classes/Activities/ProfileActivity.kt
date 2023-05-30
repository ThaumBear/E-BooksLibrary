package com.example.bookapp.classes.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.example.bookapp.R
import com.example.bookapp.classes.Adapters.AdapterBookFavorite
import com.example.bookapp.classes.Application.ApplicationClass
import com.example.bookapp.classes.Models.ModelBook
import com.example.bookapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapterBookFavorite: AdapterBookFavorite
    private lateinit var booksArrayList: ArrayList<ModelBook>

    // тип користувача - передається з минулої сторінки
    private lateinit var userType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        userType = intent.getStringExtra("userType")!!
        checkUserType(userType)

        // перевірка користувача
        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        // завантаження улюблених книг
        loadFavoriteBooks()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileEditBtn.setOnClickListener {
            val intent = Intent(this, ProfileEditActivity::class.java)
            intent.putExtra("userType", userType)
            startActivity(intent)
            finish()
        }
    }

    // завантаження даних користувача
    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val formattedDate = ApplicationClass.formatTimeStamp(timestamp.toLong())

                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = when(userType) {
                        "user" -> "Користувач"
                          else -> "Адміністратор"
                    }

                    // відобразити зображення
                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.person_gray)
                            .into(binding.profileIv)

                    } catch (_: Exception) {

                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    // завантаження улюблених книг
    private fun loadFavoriteBooks() {
        booksArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksArrayList.clear()

                    for (ds in snapshot.children) {
                        val bookId = "${ds.child("bookId").value}"

                        val modelBook = ModelBook()
                        modelBook.id = bookId

                        booksArrayList.add(modelBook)
                    }

                    val favBooksCount = "${booksArrayList.size}"
                    binding.favoriteBookCountTv.text = favBooksCount

                    // сховати надпис, якщо немає улюблених книг
                    if (favBooksCount == "0") {
                        binding.favoritesLabelTv.visibility = View.GONE
                    }

                    adapterBookFavorite = AdapterBookFavorite(
                        this@ProfileActivity,
                        booksArrayList,
                        userType
                    )
                    binding.favoritesRv.adapter = adapterBookFavorite
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

    }

    // перевірка типу користувача для показу потрібного типу сплеш екрану
    private fun checkUserType(userType: String) {
        // змінна для визначення сплеш екрану
        val layout: Int

        // якщо користувач - звичайний, то змінюється тема
        if (userType == "user") {
            changeTheme()
            layout = R.layout.activity_splash
        } else {
            layout = R.layout.splash_admin
        }

        ApplicationClass.showScreen(layout, binding.root, this@ProfileActivity)
    }

    // зміна теми для звичайних користувачів
    private fun changeTheme() {
        binding.main.setBackgroundResource(R.drawable.back01)
        binding.backProfileV.setBackgroundResource(R.drawable.back02)
        binding.toolbarRl.setBackgroundResource(R.drawable.shape_toolbar02_3)
    }
}
