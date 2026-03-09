package com.quibbler.sevenmusic.fragment.found

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.quibbler.sevenmusic.R

class FoundTopPlaceholderFragment : Fragment() {
    private var mImageView: ImageView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_found_top_carousel_item, container, false)
        mImageView = view.findViewById<ImageView>(R.id.found_fragment_iv_carousel)
        mImageView!!.setImageResource(R.drawable.carousel_placeholder)
        mImageView!!.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //        mImageView.setImageResource(R.drawable.carousel_placeholder);
    }
}
