package com.example.bookapp.classes.Adapters

import android.app.AlertDialog
import com.example.bookapp.classes.Application.ApplicationClass
import com.example.bookapp.classes.Models.ModelBook
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.classes.Activities.BookDetailActivity
import com.example.bookapp.classes.Filters.FilterBookAdmin
import com.example.bookapp.databinding.RowBookAdminBinding
import com.example.bookapp.classes.Activities.EditBookActivity

class AdapterBookAdmin : RecyclerView.Adapter<AdapterBookAdmin.HolderBookAdmin>, Filterable {

    private var context: Context
    public var bookArrayList: ArrayList<ModelBook>
    private lateinit var binding: RowBookAdminBinding
    private val filterList: ArrayList<ModelBook>
    private var filter: FilterBookAdmin? = null

    constructor(context: Context, bookArrayList: ArrayList<ModelBook>) : super() {
        this.context = context
        this.bookArrayList = bookArrayList
        this.filterList = bookArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderBookAdmin {
        binding = RowBookAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderBookAdmin(binding.root)
    }

    override fun getItemCount(): Int {
        return bookArrayList.size
    }

    override fun onBindViewHolder(holder: HolderBookAdmin, position: Int) {
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

        // натиснення на кнопку дій
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        // натиснення на поле з книгою
        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            intent.putExtra("userType", "admin")
            context.startActivity(intent)
        }


    }

    private fun moreOptionsDialog(model: ModelBook, holder: AdapterBookAdmin.HolderBookAdmin) {
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        val options = arrayOf("Редагувати", "Видалити")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Оберіть дію:")
            .setItems(options) { dialog, position ->
                when (position) {
                    0 -> {
                        val intent = Intent(context, EditBookActivity::class.java)
                        intent.putExtra("bookId", bookId)
                        context.startActivity(intent)
                    }
                    1 -> {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Видалення")
                            .setMessage("Ви впевнені, що хочете видалити дану книгу?")
                            .setPositiveButton("Так") {dialog, _ ->
                                ApplicationClass.deleteBook(context, bookId, bookUrl, bookTitle)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Ні") {dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            }
            .show()

    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterBookAdmin(filterList, this)
        }

        return filter as FilterBookAdmin
    }


    inner class HolderBookAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
    }

}