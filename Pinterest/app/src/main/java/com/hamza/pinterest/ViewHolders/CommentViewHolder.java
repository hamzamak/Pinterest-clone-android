package com.hamza.pinterest.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hamza.pinterest.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentViewHolder extends RecyclerView.ViewHolder {
    public TextView owner_comment, owner_name ,date_comment ;
    public CircleImageView comment_avatar ;
    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);
        comment_avatar = itemView.findViewById(R.id.comment_avatar);
        owner_comment = itemView.findViewById(R.id.owner_comment);
        owner_name = itemView.findViewById(R.id.owner_name);
        date_comment = itemView.findViewById(R.id.date_comment);
    }
}
