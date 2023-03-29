package com.hamza.pinterest.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hamza.pinterest.Models.Comment;
import com.hamza.pinterest.R;
import com.hamza.pinterest.ViewHolders.CommentViewHolder;
import com.squareup.picasso.Picasso;

import java.util.List;


public class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    Context context ;
    List<Comment> comments ;

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentViewHolder(LayoutInflater.from(context).inflate(R.layout.comment_recycler_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.owner_name.setText(comments.get(position).getOwner().getUsername());
        holder.owner_comment.setText(comments.get(position).getContent());
        Picasso.get().load(comments.get(position).getOwner().getAvatarUrl()).placeholder(R.drawable.avatar).into(holder.comment_avatar);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
}
