package com.quibbler.sevenmusic.fragment.found;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.quibbler.sevenmusic.R;

public class FoundTopPlaceholderFragment extends Fragment {
    private ImageView mImageView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_found_top_carousel_item, container, false);
        mImageView = view.findViewById(R.id.found_fragment_iv_carousel);
        mImageView.setImageResource(R.drawable.carousel_placeholder);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        mImageView.setImageResource(R.drawable.carousel_placeholder);
    }
}
