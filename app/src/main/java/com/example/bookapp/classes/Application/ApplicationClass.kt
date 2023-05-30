package com.example.bookapp.classes.Application

import android.app.Activity
import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.os.Handler
import android.text.format.DateFormat
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.example.bookapp.R
import com.example.bookapp.databinding.ActivityBookDetailBinding
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ApplicationClass: Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {

        // перекодування timestamp у дату
        fun formatTimeStamp(timestamp: Long) : String {
            val cal = Calendar.getInstance(Locale("ukr", "UA"))
            cal.timeInMillis = timestamp

            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        // одержання розміру PDF файлу
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetaData ->
                    val bytes = storageMetaData.sizeBytes.toDouble()

                    val kb = bytes/1024
                    val mb = kb/1024
                    if (mb >= 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)} bytes"
                    }
                }
        }

        // завантаження 1-ої сторінки PDF файлу
        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->

                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError {
                            progressBar.visibility = View.INVISIBLE
                        }
                        .onPageError { _, _ ->
                            progressBar.visibility = View.INVISIBLE
                        }
                        .onLoad { nbPages ->
                            progressBar.visibility = View.INVISIBLE

                            if (pagesTv != null) {
                                pagesTv.text = "$nbPages"
                            }
                        }
                        .load()
                }

        }


        fun loadCategory(categoryId: String, categoryTv: TextView) {
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category: String = "${snapshot.child("category").value}"
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }


        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Будь ласка, зачекайте..")
            progressDialog.setMessage("Видяляємо ${bookTitle}..")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            val pdfRef = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            pdfRef.delete()
                .addOnSuccessListener {
                    val bookRef = FirebaseDatabase.getInstance().getReference("Books")
                    bookRef.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Книгу успішно видалено!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Не вдалося видалити книгу: ${e.message}..",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Не вдалося видалити PDF файл: ${e.message}..",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            progressDialog.dismiss()
        }

        /* інкрементація вказаного параметру в базі даних:
        * - "viewsCount" - кількість переглядів книги
        * - "downloadsCount" - кількість завантажень*/
        fun incrementCount(bookId: String, countParam: String) {
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var count = "${snapshot.child(countParam).value}"

                        if (count == "" || count == "null") {
                            count = "0"
                        }

                        val newCount = count.toLong() + 1

                        val hashMap = HashMap<String, Any>()
                        hashMap[countParam] = newCount

                        ref.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

        // прибрання книги з улюблених
        fun removeFromFavorite(context: Context, bookId: String, bookTitle: String) {
            val firebaseAuth = FirebaseAuth.getInstance()

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Книга ${bookTitle} прибрана з улюблених!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Не вдалося прибрати з улюблених: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // перехід від сплеш екрану до основного макету
        fun showScreen(layout: Int, layoutRoot: RelativeLayout, activity: Activity) {
            activity.setContentView(layout)
            Handler().postDelayed(Runnable {
                activity.setContentView(layoutRoot)
            }, 1100)
        }
    }
}