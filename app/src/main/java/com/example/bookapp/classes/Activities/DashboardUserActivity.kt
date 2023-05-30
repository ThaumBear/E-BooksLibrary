package com.example.bookapp.classes.Activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.style.UnderlineSpan
import android.text.SpannableString
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.bookapp.R
import com.example.bookapp.classes.Application.ApplicationClass
import com.example.bookapp.classes.BooksUserFragment
import com.example.bookapp.classes.Models.ModelCategory
import com.example.bookapp.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardUserBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    // типу користувача
    private lateinit var userType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)

        // перевірка користувача
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        // відображення категорій книг
        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        // натиснення на кнопку профілю
        binding.profileBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userType", userType)
            startActivity(intent)
        }

        // натиснення на кнопку виходу
        binding.logoutBtn.setOnClickListener() {
            if (firebaseAuth.currentUser != null) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Вихід")
                    .setMessage("Ви впевнені, що хочете вийти з акаунту?")
                    .setPositiveButton("Так") {_, _ ->
                        Toast.makeText(
                            this,
                            "Виходимо..",
                            Toast.LENGTH_SHORT
                        ).show()
                        signOut()
                    }
                    .setNegativeButton("Ні") {a, _ ->
                        a.dismiss()
                    }
                    .show()
            } else {
                signOut()
            }
        }
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )

        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                val modelAll = ModelCategory(
                    "01",
                    "Усі",
                    1,
                    ""
                )
                val modelMostViewed = ModelCategory(
                    "01",
                    "Найбільш популярні",
                    1,
                    ""
                )
                val modelMostDownloaded = ModelCategory(
                    "01",
                    "Найбільш завантажувані",
                    1,
                    ""
                )

                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)

                // усі книги
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}",
                        userType
                    ), modelAll.category
                )
                // найбільш переглядувані
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}",
                        userType
                    ), modelMostViewed.category
                )
                // найбільш завантажувані
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostDownloaded.id}",
                        "${modelMostDownloaded.category}",
                        "${modelMostDownloaded.uid}",
                        userType
                    ), modelMostDownloaded.category
                )
                viewPagerAdapter.notifyDataSetChanged()

                // за категоріями
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)

                    categoryArrayList.add(model!!)
                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}",
                            userType
                        ), model.category
                    )
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        viewPager.adapter = viewPagerAdapter
    }


    class ViewPagerAdapter(
        fm: FragmentManager,
        behavior: Int,
        context: Context
    ) : FragmentPagerAdapter(fm, behavior) {

        private val fragmentList: ArrayList<BooksUserFragment> = ArrayList()
        private val fragmentTitleList: ArrayList<String> = ArrayList()
        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BooksUserFragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }

    }

    private fun signOut() {
        firebaseAuth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun checkUser() {
        val layout = R.layout.activity_splash
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser == null) {
            val notLoggedInString = "Не авторизовано"
            // показ підчеркнутого рядка про неавторизованість
            underlineEmail(notLoggedInString)

            // скриття кнопки профілю
            binding.profileBtn.visibility = View.GONE
            userType = "none"
        }
        else {
            val email = firebaseUser.email
            // показ пошти користувача та її підчеркування
            underlineEmail(email!!)
            userType = "user"
        }
        ApplicationClass.showScreen(layout, binding.root, this@DashboardUserActivity)
    }

    // підчеркування пошти
    private fun underlineEmail(emailLine: String) {
        val mSpannableEmail = SpannableString(emailLine)
        mSpannableEmail.setSpan(UnderlineSpan(), 0, mSpannableEmail.length, 0)
        binding.subTitleTv.text = mSpannableEmail
    }
}