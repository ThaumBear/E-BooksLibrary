package com.example.bookapp.classes.Activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.bookapp.R
import androidx.activity.result.ActivityResult
import com.example.bookapp.classes.Application.ApplicationClass
import com.example.bookapp.databinding.ActivityProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var imageUri: Uri? = null
    private var name = ""

    // тип користувача - передається з минулої сторінки
    private lateinit var userType: String

    override fun onBackPressed() {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("userType", userType)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Буль ласка, зачекайте..")
        progressDialog.setCanceledOnTouchOutside(false)

        userType = intent.getStringExtra("userType")!!
        if (userType == "user") {
            changeTheme()
        }

        // перевірка користувача
        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileIv.setOnClickListener {
            showImageAttachMenu()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        name = binding.nameEt.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(
                this,
                "Введіть Ваше ім'я..",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (imageUri == null) {
                updateProfile("")
            } else {
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Завантажуємо фото профіля..")
        progressDialog.show()

        val filePathAndName = "ProfileImages/" + firebaseAuth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Не вдалося завантажити фото профіля: ${e.message}..",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Оновлюємо профіль..")

        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["name"] = "$name"
        if (imageUri != null) {
            hashmap["profileImage"] = uploadedImageUrl
        }

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Профіль успішно оновлено!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Не вдалося оновити профіль: ${e.message}..",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // функція перевіряє користувача у БД, завантажує
    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val userType = "${snapshot.child("userType").value}"

                    binding.nameEt.setText(name)

                    // відобразити зображення
                    try {
                        Glide.with(this@ProfileEditActivity)
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

    private fun showImageAttachMenu() {
        val popupMenu = PopupMenu(this, binding.profileIv)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Камера")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Галерея")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId
            when (id) {
                0 -> pickImageCamera()
                1 -> pickImageGallery()
            }
            true
        }
    }

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback <ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data

                binding.profileIv.setImageURI(imageUri)
            } else {
                Toast.makeText(
                    this,
                    "Відмінено..",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback <ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data

                binding.profileIv.setImageURI(imageUri)
            } else {
                Toast.makeText(
                    this,
                    "Відмінено..",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    // зміна теми для звичайного користувача
    private fun changeTheme() {
        binding.root.setBackgroundResource(R.drawable.back01)
        binding.toolbarRl.setBackgroundResource(R.drawable.shape_toolbar01)
        binding.nameEt.setBackgroundResource(R.drawable.shape_edit_text01)
        binding.updateBtn.setBackgroundResource(R.drawable.shape_button01)
        binding.toolbarTitleTv.setTextColor(resources.getColor(R.color.teal_700))
    }
}