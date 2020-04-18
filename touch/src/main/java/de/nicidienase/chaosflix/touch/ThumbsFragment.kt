package de.nicidienase.chaosflix.touch

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.nicidienase.chaosflix.common.mediadata.ThumbnailParser
import de.nicidienase.chaosflix.common.viewmodel.ViewModelFactory
import de.nicidienase.chaosflix.touch.databinding.FragmentThumbsTestBinding
import de.nicidienase.chaosflix.touch.databinding.ItemThumbBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThumbsFragment : Fragment(R.layout.fragment_thumbs_test) {

    private var uri: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentThumbsTestBinding.bind(view)

        val client = ViewModelFactory.getInstance(requireContext()).apiFactory.client
        binding.thumbList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val thumbsAdapter = ThumbsAdapter()
        binding.thumbList.adapter = thumbsAdapter
        lifecycleScope.launch(Dispatchers.IO) {
            val list = ThumbnailParser(client).parse(uri)
            withContext(Dispatchers.Main) {
                thumbsAdapter.submitList(list)
            }
        }
    }

    private val drawableMap: Map<String, Bitmap> = mutableMapOf()

    private suspend fun loadImageForUri(uri: String): Drawable? {
        return withContext(Dispatchers.IO) {
            val imgUri = Uri.parse(uri)
            val sourceBitmap = drawableMap[imgUri.path] ?: Glide.with(requireContext())
                    .asBitmap()
                    .load(imgUri)
                    .submit()
                    .get()
            val params = imgUri.getQueryParameter("xywh")?.split(",")
            return@withContext if (params?.size == 4) {
                val (x, y, w, h) = params.map { it.toInt() }
                BitmapDrawable(Bitmap.createBitmap(sourceBitmap, x, y, w, h))
            } else {
                null
            }
        }
    }

    private inner class ThumbsAdapter : ListAdapter<ThumbnailParser.ThumbnailInfo, ThumbsAdapter.ViewHolder>(object : DiffUtil.ItemCallback<ThumbnailParser.ThumbnailInfo>() {
        override fun areItemsTheSame(oldItem: ThumbnailParser.ThumbnailInfo, newItem: ThumbnailParser.ThumbnailInfo): Boolean = oldItem === newItem
        override fun areContentsTheSame(oldItem: ThumbnailParser.ThumbnailInfo, newItem: ThumbnailParser.ThumbnailInfo): Boolean = oldItem == newItem
    }) {

        inner class ViewHolder(val binding: ItemThumbBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemThumbBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            this@ThumbsFragment.lifecycleScope.launch {
                val item = getItem(position)
                holder.binding.apply {
                    thumbImage.setImageDrawable(loadImageForUri(item.thumb))
                    startTime.text = item.startTime.toString()
                    endTime.text = item.endTime.toString()
                }
            }
        }
    }
}
