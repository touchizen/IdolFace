package com.touchizen.idolface.ui.countries

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.touchizen.idolface.R
import com.touchizen.idolface.databinding.ItemCountryBinding
import com.touchizen.idolface.model.Country
import com.touchizen.idolface.utils.Countries
import com.touchizen.idolface.utils.ItemClickListener
import java.util.*
import kotlin.collections.ArrayList

class AdCountries : RecyclerView.Adapter<AdCountries.UserViewModel>() {

    lateinit var countries: ArrayList<Country>

    private lateinit var  allCountries: ArrayList<Country>

    fun setData() {
        this.countries = Countries.getCountries() as ArrayList<Country>
        allCountries= ArrayList()
        allCountries.addAll(countries)
    }

    companion object {
        var itemClickListener: ItemClickListener? = null
    }

    fun filter(query: String) {
        try {
            countries.clear()
            if (query.isEmpty())
                countries.addAll(allCountries)
            else {
                for (country in allCountries) {
                    if (country.name.toLowerCase(Locale.getDefault())
                            .contains(query.toLowerCase(Locale.getDefault()))
                    )
                        countries.add(country)
                }
            }
            notifyDataSetChanged()
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewModel {
        val binding: ItemCountryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_country, parent, false
        )
        return UserViewModel(binding)
    }


    override fun onBindViewHolder(holder: UserViewModel, position: Int) {
        holder.bind(countries[position])
    }

    class UserViewModel(val binding: ItemCountryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Country) {
            binding.country = item
            binding.viewRoot.setOnClickListener { v ->
                itemClickListener?.onItemClicked(v, bindingAdapterPosition)
            }
            binding.executePendingBindings()
        }
    }

    override fun getItemCount() = countries.size

}
