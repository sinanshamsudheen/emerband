package com.example.emerband;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.emerband.adapters.ContactAdapter;
import com.example.emerband.database.DatabaseHelper;
import com.example.emerband.models.Contact;
import java.util.List;
import java.util.regex.Pattern;

public class EmergencyContactsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private DatabaseHelper databaseHelper;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,13}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Load contacts and setup adapter
        List<Contact> contacts = databaseHelper.getAllContacts();
        adapter = new ContactAdapter(contacts, this::deleteContact);
        recyclerView.setAdapter(adapter);

        // Setup FAB
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddContactDialog());
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        builder.setView(dialogView);
        builder.setTitle(R.string.add_contact);

        TextInputEditText etName = dialogView.findViewById(R.id.etContactName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhoneNumber);

        AlertDialog dialog = builder.create();
        
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError(getString(R.string.name_required));
                return;
            }

            if (phone.isEmpty()) {
                etPhone.setError(getString(R.string.phone_required));
                return;
            }

            if (!PHONE_PATTERN.matcher(phone).matches()) {
                etPhone.setError(getString(R.string.invalid_phone));
                return;
            }

            Contact contact = new Contact(name, phone);
            if (databaseHelper.addContact(contact)) {
                adapter.updateContacts(databaseHelper.getAllContacts());
                Toast.makeText(this, R.string.contact_added, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, R.string.error_adding_contact, Toast.LENGTH_SHORT).show();
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void deleteContact(Contact contact) {
        databaseHelper.deleteContact(contact);
        adapter.updateContacts(databaseHelper.getAllContacts());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
} 