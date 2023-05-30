package com.example.bookapp.classes.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.bookapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

public class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed(Runnable {
            checkUser()
        }, 1000)
    }

    private fun checkUser () {
        var layout: Int

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // показати звичайний сплеш екран

            setContentView(R.layout.activity_splash)
            // через секунду здійснити перехід до головного екрану
            Handler().postDelayed(Runnable {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 1000)
        }

        else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // користувач - або звичайний, або адміністратор
                        val userType = snapshot.child("userType").value

                        if (userType == "user") {
                            // через секунду здійснити перехід до головного екрану
                            startActivity(
                                Intent(
                                    this@SplashActivity,
                                    DashboardUserActivity::class.java
                                )
                            )
                            finish()
                        }
                        else if (userType == "admin") {
                            startActivity(
                                Intent(
                                    this@SplashActivity,
                                    DashboardAdminActivity::class.java
                                )
                            )
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

}