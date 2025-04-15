package com.example.emerband.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.emerband.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "emergency_contacts.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_CONTACTS = "contacts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE = "phone";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PHONE + " TEXT"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }

    public boolean addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_PHONE, contact.getPhone());
        long result = db.insert(TABLE_CONTACTS, null, values);
        db.close();
        return result != -1;
    }

    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setId(cursor.getInt(0));
                contact.setName(cursor.getString(1));
                contact.setPhone(cursor.getString(2));
                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contactList;
    }

    public void deleteContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(contact.getId())});
        db.close();
    }
} 