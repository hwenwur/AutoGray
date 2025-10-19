package com.xuxing.autogray;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PackageListAdapter extends RecyclerView.Adapter<PackageListAdapter.ViewHolder> {
    private List<String> packageList;
    private OnPackageRemoveListener removeListener;

    public interface OnPackageRemoveListener {
        void onRemove(String packageName);
    }

    public PackageListAdapter(Set<String> packages, OnPackageRemoveListener listener) {
        this.packageList = new ArrayList<>(packages);
        this.removeListener = listener;
    }

    public void updateList(Set<String> packages) {
        this.packageList = new ArrayList<>(packages);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String packageName = packageList.get(position);
        holder.packageNameText.setText(packageName);
        holder.removeButton.setOnClickListener(v -> removeListener.onRemove(packageName));
    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView packageNameText;
        ImageButton removeButton;

        ViewHolder(View itemView) {
            super(itemView);
            packageNameText = itemView.findViewById(R.id.packageNameText);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}
