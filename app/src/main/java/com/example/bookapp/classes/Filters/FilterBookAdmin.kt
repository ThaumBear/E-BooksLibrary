package com.example.bookapp.classes.Filters

import com.example.bookapp.classes.Models.ModelBook
import android.widget.Filter
import com.example.bookapp.classes.Adapters.AdapterBookAdmin

class FilterBookAdmin : Filter {

    var filterList: ArrayList<ModelBook>
    var adapterBookAdmin: AdapterBookAdmin

    constructor(filterList: ArrayList<ModelBook>, adapterBookAdmin: AdapterBookAdmin) : super() {
        this.filterList = filterList
        this.adapterBookAdmin = adapterBookAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence? = constraint
        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelBook>()

            for (i in filterList.indices) {
                if (filterList[i].title.lowercase().contains(constraint)) {
                    filteredModels.add(filterList[i])
                }
            }

            results.count = filteredModels.size
            results.values = filteredModels

        } else {
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapterBookAdmin.bookArrayList = results!!.values as ArrayList<ModelBook>
        adapterBookAdmin.notifyDataSetChanged()
    }
}