package com.example.bookapp.classes.Activities

import com.example.bookapp.classes.Models.ModelCategory
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bookapp.databinding.ActivityAddBookBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class AddBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBookBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var categoryArrayList: java.util.ArrayList<ModelCategory>

    private var pdfUri: Uri? = null
    private var title = ""
    private var description = ""
    private var category = ""

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadBookCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Будь ласка, зачекайте..")
        progressDialog.setCanceledOnTouchOutside(false)

        // натиснення на кнопку виходу
        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, DashboardAdminActivity::class.java))
            finish()
        }

        // натиснення на кнопку обрання категорії
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        // натиснення на кнопку прикріплення PDF
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        // натиснення на кнопку завантаження
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(
                this,
                "Введіть назву..",
                Toast.LENGTH_SHORT
            ).show()
        } else if (description.isEmpty()) {
            Toast.makeText(
                this,
                "Введіть опис..",
                Toast.LENGTH_SHORT
            ).show()
        } else if (category.isEmpty()) {
            Toast.makeText(
                this,
                "Оберіть категорії книги..",
                Toast.LENGTH_SHORT
            ).show()
        } else if (pdfUri == null) {
            Toast.makeText(
                this,
                "Прикріпіть {DF..",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            uploadPdfToStorage()
        }

    }

    private fun loadBookCategories() {
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun categoryPickDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Оберіть категорію:")
            .setItems(categoriesArray) { dialog, position ->
                selectedCategoryId = categoryArrayList[position].id
                selectedCategoryTitle = categoryArrayList[position].category

                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }

    private fun pdfPickIntent() {
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    private val pdfActivityResultLauncher = registerForActivityResult (
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback <ActivityResult> {  result ->
            if (result.resultCode == RESULT_OK) {
                pdfUri = result.data!!.data
            }
            else {
                Toast.makeText(
                    this,
                    "Відмінено..",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    // завантаження PDF до Firebase Storage
    private fun uploadPdfToStorage() {
        progressDialog.setMessage("Заватажуємо PDF..")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Books/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // успішно
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"
                uploadPdfToDb(uploadedPdfUrl, timestamp)
            }
            .addOnFailureListener { e ->
                // помилка
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Не вдалося завантажити: ${e.message}..",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    // завантаження даних про книгу до Firebase Database
    private fun uploadPdfToDb(uploadedPdfUrl: String, timestamp: Long) {
        progressDialog.setMessage("Завантажуємо інформацію про книгу..")

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {

                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Завантаження завершено!",
                    Toast.LENGTH_SHORT
                ).show()
                pdfUri = null
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Не вдалося завантажити:  ${e.message}..",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }
}