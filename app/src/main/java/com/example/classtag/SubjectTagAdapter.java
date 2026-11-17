package com.example.classtag;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubjectTagAdapter extends RecyclerView.Adapter<SubjectTagAdapter.SubjectTagViewHolder> {

    public interface OnSubjectTagClickListener {
        void onSubjectTagClick(SubjectTag subjectTag);
    }

    private final List<SubjectTag> subjectTags;
    private final OnSubjectTagClickListener listener;

    public SubjectTagAdapter(List<SubjectTag> subjectTags, OnSubjectTagClickListener listener) {
        this.subjectTags = subjectTags;
        this.listener = listener;
    }

    public void addSubjectTag(SubjectTag subjectTag) {
        this.subjectTags.add(0, subjectTag);
        notifyItemInserted(0);
    }

    public void updateList(List<SubjectTag> newList) {
        this.subjectTags.clear();
        this.subjectTags.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectTagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject_card, parent, false);
        return new SubjectTagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectTagViewHolder holder, int position) {
        SubjectTag subjectTag = subjectTags.get(position);
        holder.tvName.setText(subjectTag.getName());
        holder.tvMeta.setText(subjectTag.getTopicsCount());
        holder.vAccent.setBackgroundColor(
                holder.itemView.getResources().getColor(subjectTag.getAccentColorRes()));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSubjectTagClick(subjectTag);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjectTags.size();
    }

    static class SubjectTagViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvMeta;
        final View vAccent;

        SubjectTagViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSubjectName);
            tvMeta = itemView.findViewById(R.id.tvSubjectMeta);
            vAccent = itemView.findViewById(R.id.vAccent);
        }
    }
}
