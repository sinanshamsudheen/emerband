package com.example.emerband.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emerband.R;
import com.example.emerband.models.Contact;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<Contact> contacts;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Contact contact);
    }

    public ContactAdapter(List<Contact> contacts, OnDeleteClickListener listener) {
        this.contacts = contacts;
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.nameTextView.setText(contact.getName());
        holder.numberTextView.setText(contact.getPhone());
        
        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onDeleteClickListener.onDeleteClick(contacts.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void updateContacts(List<Contact> newContacts) {
        this.contacts = newContacts;
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView numberTextView;
        ImageButton deleteButton;

        ContactViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contact_name);
            numberTextView = itemView.findViewById(R.id.contact_number);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
} 