package com.development.nest.time.wrap.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.development.nest.time.wrap.R
import com.development.nest.time.wrap.databinding.LanguageItemLayoutBinding
import com.development.nest.time.wrap.utils.AppEvents
import com.development.nest.time.wrap.utils.SharedPreferenceHelper
import com.development.nest.time.wrap.utils.SharedPreferenceHelper.SELECTED_LANGUAGE
import org.json.JSONArray

class LanguagesAdapter(val jsonArray: JSONArray) :
    RecyclerView.Adapter<LanguagesAdapter.ViewHolder>() {

    var listener: AppEvents? = null
    init {
        flags()
    }

    inner class ViewHolder(private val binding: LanguageItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NotifyDataSetChanged")
        fun bindView(position: Int) {
            val jsonObject = jsonArray.getJSONObject(position)
            val code = jsonObject.getString("code")
            val nativeName = jsonObject.getString("nativeName")

            binding.languageName.text = nativeName

            val selected = SharedPreferenceHelper[SELECTED_LANGUAGE, "en"]
            binding.radioBtn.isChecked = code == selected

            val flag = flags[code]
            flag?.let { binding.flagIcon.setImageResource(it) }

            itemView.setOnClickListener {
                SharedPreferenceHelper[SELECTED_LANGUAGE] = code
                notifyDataSetChanged()
                listener?.onClick()
            }
            binding.radioBtn.setOnClickListener {
                SharedPreferenceHelper[SELECTED_LANGUAGE] = code
                notifyDataSetChanged()
                listener?.onClick()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = LanguageItemLayoutBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bindView(position)
    }



    override fun getItemCount() = jsonArray.length()

    private lateinit var flags: HashMap<String, Int>
    private fun flags() {
        flags = HashMap()
        flags["en"] = R.drawable.flag_en
        flags["ar"] = R.drawable.flag_ar
        flags["pt"] = R.drawable.portuguese
        flags["es"] = R.drawable.spanish
        flags["fr"] = R.drawable.french
        flags["hi"] = R.drawable.hindi
        flags["de"] = R.drawable.flag_dutch
        flags["ko"] = R.drawable.korean
        flags["ja"] = R.drawable.japan
        flags["ru"] = R.drawable.russian
        flags["th"] = R.drawable.thai
        flags["in"] = R.drawable.indonesian
        flags["tr"] = R.drawable.turkish
        flags["it"] = R.drawable.italy
        flags["bn"] = R.drawable.bangali
    }
}