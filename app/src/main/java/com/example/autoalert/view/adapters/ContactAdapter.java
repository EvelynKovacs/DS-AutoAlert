package com.example.autoalert.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autoalert.R;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<String> contactList;
    private OnContactDeleteListener onDeleteListener;

    public ContactAdapter(List<String> contactList, OnContactDeleteListener listener) {
        this.contactList = contactList;
        this.onDeleteListener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }



    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        if (contactList != null && !contactList.isEmpty()) {
            String contact = contactList.get(position);
            holder.contactName.setText(contact);

            // Mostrar el botón de eliminar
            holder.deleteButton.setVisibility(View.VISIBLE);

            // Configurar el click listener para el botón de eliminar
            holder.deleteButton.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onDeleteListener.onContactDelete(adapterPosition);
                }
            });
        } else {
            // Ocultar el botón de eliminar y mostrar un mensaje si la lista está vacía
            holder.deleteButton.setVisibility(View.GONE);
            holder.contactName.setText("No contacts available");
        }
    }


    @Override
    public int getItemCount() {
        return contactList != null && !contactList.isEmpty() ? contactList.size() : 0;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactName;
        Button deleteButton;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.txtContactName);
            deleteButton = itemView.findViewById(R.id.btnDeleteContact);
        }
    }

    public interface OnContactDeleteListener {
        void onContactDelete(int position);
    }
}
