package com.example.bookapp.classes.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.classes.Activities.BookDetailActivity
import com.example.bookapp.classes.Application.ApplicationClass
import com.example.bookapp.classes.Filters.FilterBookUser
import com.example.bookapp.classes.Models.ModelBook
import com.example.bookapp.databinding.RowBookUserBinding

class AdapterBookUser : RecyclerView.Adapter<AdapterBookUser.HolderBookUser>, Filterable {

    private var context: Context
    public var bookArrayList: ArrayList<ModelBook>
    private lateinit var binding: RowBookUserBinding
    private val filterList: ArrayList<ModelBook>
    private var filter: FilterBookUser? = null
    private val userType: String

    constructor(context: Context, bookArrayList: ArrayList<ModelBook>, userType: String) {
        this.context = context
        this.bookArrayList = bookArrayList
        this.filterList = bookArrayList
        this.userType = userType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderBookUser {
        binding = RowBookUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderBookUser(binding.root)
    }

    override fun getItemCount(): Int {
        return bookArrayList.size
    }

    override fun onBindViewHolder(holder: HolderBookUser, position: Int) {
        val model = bookArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp
        val formattedDate = ApplicationClass.formatTimeStamp(timestamp)

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        ApplicationClass.loadCategory(categoryId = categoryId, holder.categoryTv)

        ApplicationClass.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null)

        ApplicationClass.loadPdfSize(pdfUrl, title, holder.sizeTv)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            intent.putExtra("userType", userType)
            context.startActivity(intent)
        }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterBookUser(filterList, this)
        }

        return filter as FilterBookUser
    }

    inner class HolderBookUser(itemView: View): RecyclerView.ViewHolder(itemView) {
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
    }


}