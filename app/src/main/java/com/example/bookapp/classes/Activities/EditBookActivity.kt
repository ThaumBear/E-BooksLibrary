package com.example.bookapp.classes.Activities

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookapp.databinding.ActivityEditBookBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBookBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var categoryTitleArrayList: ArrayList<String>
    private lateinit var categoryIdArrayList: ArrayList<String>
    private var selectedCategoryTitle = ""
    private var selectedCategoryId = ""
    private var description = ""
    private var bookId = ""
    private var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBookBinding.inflate(layoutInflater)


        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Будь ласка, зачекайте..")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()

        // автоматичне заповнення полів даними, що містяться в базі
        loadBookInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private fun loadBookInfo() {
        var ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val category = snapshot.child("category").value.toString()
                                binding.categoryTv.text = category
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadCategories() {
        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        var ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryTitleArrayList.clear()
                categoryIdArrayList.clear()

                for (ds in snapshot.children) {
                    val category = "${ds.child("category").value}"
                    val id = "${ds.child("id").value}"

                    categoryTitleArrayList.add(category)
                    categoryIdArrayList.add(id)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun categoryPickDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Оберіть категорію:")
            .setItems(categoriesArray) { dialog, position ->
                selectedCategoryTitle = categoryTitleArrayList[position]
                selectedCategoryId = categoryIdArrayList[position]

                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }

    private fun validateData() {
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

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
        } else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(
                this,
                "Оберіть категорії книги..",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            updateBook()
        }
    }

    private fun updateBook() {
        progressDialog.setMessage("Оновлюємо дані про книгу..")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "${description}"
        hashMap["categoryId"] = "${selectedCategoryId}"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Дані успішно оновлено!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Не вдалося оновити дані: ${e.message}..",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }
}