package com.hamza.pinterest.ViewHolders;


import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.hamza.pinterest.R;

public class HomeViewHolder extends RecyclerView.ViewHolder {
    public CardView card_home_container ;
    public TextView textView_item_description ;
    public ImageView imageView_item ;
    public CircularProgressIndicator circularProgress ;
    public ImageButton likeButtonIcon ;
    public HomeViewHolder(@NonNull View itemView) {
        super(itemView);
        card_home_container = itemView.findViewById(R.id.card_home_container);
        textView_item_description = itemView.findViewById(R.id.textView_item_description);
        imageView_item = itemView.findViewById(R.id.imageView_item);
        circularProgress= itemView.findViewById(R.id.circularProgress);
        likeButtonIcon = itemView.findViewById(R.id.likeButtonIcon);
    }
}
