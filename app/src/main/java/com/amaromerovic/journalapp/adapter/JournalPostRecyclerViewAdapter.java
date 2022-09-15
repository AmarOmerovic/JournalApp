package com.amaromerovic.journalapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amaromerovic.journalapp.model.Journal;
import com.amaromerovic.journalapp.util.Util;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

import journalapp.databinding.JournalPostItemBinding;

public class JournalPostRecyclerViewAdapter extends RecyclerView.Adapter<JournalPostRecyclerViewAdapter.ViewHolder> {
    private final List<Journal> journalList;
    private final Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference journalListReference = db.collection("Journal");
    private final Activity activity;

    public JournalPostRecyclerViewAdapter(List<Journal> journalList, Context context, Activity activity) {
        this.journalList = journalList;
        this.context = context;
        this.activity = activity;
        this.journalList.sort(JournalPostRecyclerViewAdapter::compare);
    }

    private static int compare(@NonNull Journal journal, @NonNull Journal t1) {
        return t1.getTimestamp().compareTo(journal.getTimestamp());
    }


    @NonNull
    @Override
    public JournalPostRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(JournalPostItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull JournalPostRecyclerViewAdapter.ViewHolder holder, int position) {
        Glide.with(context)
                .load(Uri.parse(journalList.get(position).getImageUri()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(android.R.drawable.stat_sys_download)
                .error(android.R.drawable.stat_notify_error)
                .override(800, 400)
                .into(holder.binding.postImage);

        holder.binding.postUsername.setText(journalList.get(position).getUsername());
        String timeAgo = DateUtils.getRelativeTimeSpanString(journalList.get(position).getTimestamp().getSeconds() * 1000).toString();
        holder.binding.postDate.setText(timeAgo);
        holder.binding.postTitle.setText(journalList.get(position).getTitle());
        holder.binding.postThoughts.setText(journalList.get(position).getThoughts());

        holder.binding.deleteButton.setOnClickListener(view -> journalListReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    if (journalList.get(position).getUserID().equals(documentSnapshot.getString(Util.USER_ID_KEY))
                            && journalList.get(position).getImageUri().equals(documentSnapshot.getString("imageUri"))) {
                        documentSnapshot.getReference().delete();
                        journalList.remove(position);
                        notifyDataSetChanged();
                    }
                }
            }
        }));

        holder.binding.shareButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, journalList.get(position).getTitle());
            intent.putExtra(Intent.EXTRA_TEXT, journalList.get(position).getTitle() + " " + journalList.get(position).getThoughts());
            activity.startActivity(intent);

        });

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final JournalPostItemBinding binding;

        public ViewHolder(@NonNull JournalPostItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
