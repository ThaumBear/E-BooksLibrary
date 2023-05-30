package com.example.bookapp.classes.Activities

import com.example.bookapp.classes.Adapters.AdapterCategory
import com.example.bookapp.classes.Models.ModelCategory
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.widget.Toast
import com.example.bookapp.R
import com.example.bookapp.classes.Application.ApplicationClass
import com.example.bookapp.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        // натиснення на кнопку виходу з акаунту
        binding.logoutBtn.setOnClickListener() {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Вихід")
                .setMessage("Ви впевнені, що хочете вийти з акаунту?")
                .setPositiveButton("Так") {_, _ ->
                    Toast.makeText(
                        this,
                        "Виходимо..",
                        Toast.LENGTH_SHORT
                    ).show()
                    firebaseAuth.signOut()
                    checkUser()
                }
                .setNegativeButton("Ні") {a, _ ->
                    a.dismiss()
                }
                .show()
        }

        // натиснення на кнопку профілю
        binding.profileBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userType", "admin")
            startActivity(intent)
        }



        // натиснення на кнопку пошуку
        binding.searchEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


        // натиснення на кнопку додавання категорії
        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, AddCategoryActivity::class.java))
        }

        // натиснення на кнопку додавання книги
        binding.addBookFab.setOnClickListener {
            startActivity(Intent(this, AddBookActivity::class.java))
        }

    }

    private fun loadCategories() {
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)

                    categoryArrayList.add(model!!)
                }

                adapterCategory = AdapterCategory(
                    this@DashboardAdminActivity,
                    categoryArrayList
                )

                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun checkUser() {
        val layout = R.layout.splash_admin
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            val email = firebaseUser.email
            //  показ поштової адреси із її підчеркуванням
            val mSpannableEmail = SpannableString(email)
            mSpannableEmail.setSpan(UnderlineSpan(), 0, mSpannableEmail.length, 0)
            binding.subTitleTv.text = mSpannableEmail
        }
        ApplicationClass.showScreen(layout, binding.root, this@DashboardAdminActivity)

    }
}