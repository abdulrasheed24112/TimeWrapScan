package com.development.nest.time.wrap.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.development.nest.time.wrap.databinding.GallaryItemsLayoutBinding
import com.development.nest.time.wrap.utils.AppEvents
import java.io.File

@SuppressLint("NotifyDataSetChanged")
class GalleryAdapter :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    var listener: AppEvents? = null

    private var arrayList = arrayListOf<String>()

    fun setList(list: ArrayList<String>) {
        arrayList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: GallaryItemsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(item: String) {
            val file = File(item)
            binding.fileName.text = file.nameWithoutExtension
            Log.d("galleryFragment", "bindView: ${file.extension}")
            if (file.extension == "mp4") {
                binding.videoIcon.visibility = View.VISIBLE
            } else {
                binding.videoIcon.visibility = View.GONE
            }
            Glide.with(binding.imageView.context).load(item).into(binding.imageView)
            itemView.setOnClickListener {
                listener?.onClick(file)
            }
            binding.menuIcon.setOnClickListener {
                listener?.onIteMenuClick(file)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = GallaryItemsLayoutBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bindView(arrayList[position])
    }

    override fun getItemCount() = arrayList.size

}
