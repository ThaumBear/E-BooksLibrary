package com.example.bookapp.classes.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.R
import com.example.bookapp.classes.Activities.BookDetailActivity
import com.example.bookapp.classes.Application.ApplicationClass
import com.example.bookapp.classes.Models.ModelBook
import com.example.bookapp.databinding.RowBookFavoriteBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterBookFavorite : RecyclerView.Adapter<AdapterBookFavorite.HolderBookFavorite> {

    private val context: Context
    private var bookArrayList: ArrayList<ModelBook>
    private val userType: String
    private lateinit var binding: RowBookFavoriteBinding

    constructor(context: Context, bookArrayList: ArrayList<ModelBook>, userType: String) {
        this.context = context
        this.bookArrayList = bookArrayList
        this.userType = userType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderBookFavorite {
        binding = RowBookFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderBookFavorite(binding.root)
    }

    override fun getItemCount(): Int {
        return bookArrayList.size
    }

    override fun onBindViewHolder(holder: HolderBookFavorite, position: Int) {
        val model = bookArrayList[position]

        loadBookDetails(model, holder)

        // натиснення на книгу
        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetailActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }

        // натиснення на кнопку прибрання з улюблених
        holder.removeFavBtn.setOnClickListener {
            ApplicationClass.removeFromFavorite(context, model.id, model.title)
        }
    }

    private fun loadBookDetails(model: ModelBook, holder: AdapterBookFavorite.HolderBookFavorite) {
        val bookId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"

                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewsCount = viewsCount.toLong()
                    model.downloadsCount = downloadsCount.toLong()

                    val formattedDate = ApplicationClass.formatTimeStamp(timestamp.toLong())

                    holder.titleTv.text = title
                    holder.descriptionTv.text = description
                    holder.dateTv.text = formattedDate

                    // підлаштування кольору кнопки під тип користувача
                    when (userType) {
                        "user" -> holder.removeFavBtn.setImageResource(R.drawable.remove_teal)
                        "admin" -> holder.removeFavBtn.setImageResource(R.drawable.remove_salmon)
                    }

                    ApplicationClass.loadCategory("$categoryId", holder.categoryTv)
                    ApplicationClass.loadPdfFromUrlSinglePage(
                        "$url",
                        "$title",
                        holder.pdfView,
                        holder.progressBar,
                        null)
                    ApplicationClass.loadPdfSize("$url", "$title", holder.sizeTv)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    inner class HolderBookFavorite(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val removeFavBtn = binding.removeFavBtn
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
    }
}