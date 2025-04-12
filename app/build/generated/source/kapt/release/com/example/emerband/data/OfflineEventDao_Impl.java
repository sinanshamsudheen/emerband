package com.example.emerband.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class OfflineEventDao_Impl implements OfflineEventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<OfflineEvent> __insertionAdapterOfOfflineEvent;

  private final EntityDeletionOrUpdateAdapter<OfflineEvent> __deletionAdapterOfOfflineEvent;

  private final EntityDeletionOrUpdateAdapter<OfflineEvent> __updateAdapterOfOfflineEvent;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllEvents;

  public OfflineEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfOfflineEvent = new EntityInsertionAdapter<OfflineEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `offline_events` (`id`,`eventType`,`timestamp`,`latitude`,`longitude`,`additionalData`,`retryAttempts`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final OfflineEvent entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getEventType() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getEventType());
        }
        statement.bindLong(3, entity.getTimestamp());
        if (entity.getLatitude() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLongitude());
        }
        if (entity.getAdditionalData() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getAdditionalData());
        }
        statement.bindLong(7, entity.getRetryAttempts());
      }
    };
    this.__deletionAdapterOfOfflineEvent = new EntityDeletionOrUpdateAdapter<OfflineEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `offline_events` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final OfflineEvent entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfOfflineEvent = new EntityDeletionOrUpdateAdapter<OfflineEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `offline_events` SET `id` = ?,`eventType` = ?,`timestamp` = ?,`latitude` = ?,`longitude` = ?,`additionalData` = ?,`retryAttempts` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final OfflineEvent entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getEventType() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getEventType());
        }
        statement.bindLong(3, entity.getTimestamp());
        if (entity.getLatitude() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLongitude());
        }
        if (entity.getAdditionalData() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getAdditionalData());
        }
        statement.bindLong(7, entity.getRetryAttempts());
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllEvents = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM offline_events";
        return _query;
      }
    };
  }

  @Override
  public long insert(final OfflineEvent event) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfOfflineEvent.insertAndReturnId(event);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final OfflineEvent event) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfOfflineEvent.handle(event);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final OfflineEvent event) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfOfflineEvent.handle(event);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAllEvents() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllEvents.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAllEvents.release(_stmt);
    }
  }

  @Override
  public List<OfflineEvent> getAllEvents() {
    final String _sql = "SELECT * FROM offline_events ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfEventType = CursorUtil.getColumnIndexOrThrow(_cursor, "eventType");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfAdditionalData = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalData");
      final int _cursorIndexOfRetryAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "retryAttempts");
      final List<OfflineEvent> _result = new ArrayList<OfflineEvent>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final OfflineEvent _item;
        final String _tmpEventType;
        if (_cursor.isNull(_cursorIndexOfEventType)) {
          _tmpEventType = null;
        } else {
          _tmpEventType = _cursor.getString(_cursorIndexOfEventType);
        }
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        final String _tmpLatitude;
        if (_cursor.isNull(_cursorIndexOfLatitude)) {
          _tmpLatitude = null;
        } else {
          _tmpLatitude = _cursor.getString(_cursorIndexOfLatitude);
        }
        final String _tmpLongitude;
        if (_cursor.isNull(_cursorIndexOfLongitude)) {
          _tmpLongitude = null;
        } else {
          _tmpLongitude = _cursor.getString(_cursorIndexOfLongitude);
        }
        final String _tmpAdditionalData;
        if (_cursor.isNull(_cursorIndexOfAdditionalData)) {
          _tmpAdditionalData = null;
        } else {
          _tmpAdditionalData = _cursor.getString(_cursorIndexOfAdditionalData);
        }
        _item = new OfflineEvent(_tmpEventType,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpAdditionalData);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpRetryAttempts;
        _tmpRetryAttempts = _cursor.getInt(_cursorIndexOfRetryAttempts);
        _item.setRetryAttempts(_tmpRetryAttempts);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<OfflineEvent> getEventsByType(final String type) {
    final String _sql = "SELECT * FROM offline_events WHERE eventType = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (type == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, type);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfEventType = CursorUtil.getColumnIndexOrThrow(_cursor, "eventType");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfAdditionalData = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalData");
      final int _cursorIndexOfRetryAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "retryAttempts");
      final List<OfflineEvent> _result = new ArrayList<OfflineEvent>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final OfflineEvent _item;
        final String _tmpEventType;
        if (_cursor.isNull(_cursorIndexOfEventType)) {
          _tmpEventType = null;
        } else {
          _tmpEventType = _cursor.getString(_cursorIndexOfEventType);
        }
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        final String _tmpLatitude;
        if (_cursor.isNull(_cursorIndexOfLatitude)) {
          _tmpLatitude = null;
        } else {
          _tmpLatitude = _cursor.getString(_cursorIndexOfLatitude);
        }
        final String _tmpLongitude;
        if (_cursor.isNull(_cursorIndexOfLongitude)) {
          _tmpLongitude = null;
        } else {
          _tmpLongitude = _cursor.getString(_cursorIndexOfLongitude);
        }
        final String _tmpAdditionalData;
        if (_cursor.isNull(_cursorIndexOfAdditionalData)) {
          _tmpAdditionalData = null;
        } else {
          _tmpAdditionalData = _cursor.getString(_cursorIndexOfAdditionalData);
        }
        _item = new OfflineEvent(_tmpEventType,_tmpTimestamp,_tmpLatitude,_tmpLongitude,_tmpAdditionalData);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpRetryAttempts;
        _tmpRetryAttempts = _cursor.getInt(_cursorIndexOfRetryAttempts);
        _item.setRetryAttempts(_tmpRetryAttempts);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getEventCount() {
    final String _sql = "SELECT COUNT(*) FROM offline_events";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
