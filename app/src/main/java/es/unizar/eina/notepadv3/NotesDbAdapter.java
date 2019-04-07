package es.unizar.eina.notepadv3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 *
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class NotesDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE_NOTES =
            "create table notes (_id integer primary key autoincrement, title text not null, body text not null, category integer, foreign key (category) references categories(_id));";

    private static final String DATABASE_CREATE_CATEGORIES =
            "create table categories (_id integer primary key autoincrement, title text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE_NOTES = "notes";
    private static final String DATABASE_TABLE_CATEGORIES = "categories";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_CATEGORIES);
            db.execSQL(DATABASE_CREATE_NOTES);
            // Creación de categoria vacía
            ContentValues values = new ContentValues();
            values.put(KEY_TITLE,"");
            db.insert(DATABASE_TABLE_CATEGORIES,null, values);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            db.execSQL("DROP TABLE IF EXISTS categories");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public NotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public NotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     *
     * @param title the title of the note. -> title != null y title.length() > 0
     * @param body the body of the note. -> body != null
     * @return rowId or -1 if failed
     */
    public long createNote(String title, String body, int category) {
        ContentValues initialValues = new ContentValues();

        /*
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
        */

        if (title != null && title.length() > 0 && body != null) {
            initialValues.put(KEY_TITLE, title);
            initialValues.put(KEY_BODY, body);
            initialValues.put(KEY_CATEGORY, category);
            return mDb.insert(DATABASE_TABLE_NOTES, null, initialValues);
        }
        else return -1;
    }

    /**
     * Delete the note with the given rowId
     *
     * @param rowId id of note to delete. -> rowId > 0
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        /*
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
        */
        if (rowId > 0) {
            return mDb.delete(DATABASE_TABLE_NOTES, KEY_ROWID + "=" + rowId, null) > 0;
        }
        else return false;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     *
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE_NOTES, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY, KEY_CATEGORY}, null, null, null, null, KEY_TITLE+" COLLATE NOCASE");
    }

    public Cursor fetchAllNotesByCategory() {

        return mDb.query(DATABASE_TABLE_NOTES, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY, KEY_CATEGORY}, null, null, null, null, KEY_CATEGORY);
    }

    public Cursor fetchAllNotesByCategory(int cat) {
        System.out.println(cat);
        return mDb.query(DATABASE_TABLE_NOTES, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY, KEY_CATEGORY}, KEY_CATEGORY + "=" + cat, null, null, null, KEY_TITLE+" COLLATE NOCASE");
    }


    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE_NOTES, new String[] {KEY_ROWID,
                                KEY_TITLE, KEY_BODY, KEY_CATEGORY}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     *
     * @param rowId id of note to update. -> rowId > 0
     * @param title value to set note title to. -> title != null y title.length() > 0
     * @param body value to set note body to. -> body != null
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String title, String body, int category) {
        ContentValues args = new ContentValues();

        /*
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
        */

        if (rowId > 0 && title != null && title.length() > 0 && body != null) {
            args.put(KEY_TITLE, title);
            args.put(KEY_BODY, body);
            args.put(KEY_CATEGORY, category);
            return mDb.update(DATABASE_TABLE_NOTES, args, KEY_ROWID + "=" + rowId, null) > 0;
        }
        else return false;

    }

    /**
     * Crea una nueva categoría a partir del título y texto
     * proporcionados. Si la nota se crea correctamente,
     * devuelve el   * nuevo rowId de la nota; en otro caso,
     * devuelve -1 para indicar el fallo.
     *
     *  @param title
     *	    el título de la categoria;
     *	    title != null y title.length() > 0
     *  @return rowId de la nueva categoría o -1 si no se ha
     *  podido crear
     */
    public long createCategory(String title) {
        ContentValues initialValues = new ContentValues();
        if (title != null && title.length() > 0) {
            initialValues.put(KEY_TITLE, title);
            return mDb.insert(DATABASE_TABLE_CATEGORIES, null, initialValues);
        }
        else return -1;
    }

    /**
     * Delete the note with the given rowId
     *
     * @param rowId id of note to delete. -> rowId > 0
     * @return true if deleted, false otherwise
     */
    public boolean deleteCategory(long rowId) {
        if (rowId > 0) {
            /* Se podria hacer tambien con la funcion fetchAllNotesByCategory(..) */
            Cursor notas = mDb.query(DATABASE_TABLE_NOTES, new String[] {KEY_ROWID, KEY_TITLE, KEY_BODY}, KEY_CATEGORY + "=" + rowId, null, null, null, null);
            if (notas.moveToFirst()) {
                do {
                    updateNote(notas.getLong(0), notas.getString(1), notas.getString(2), 1);
                } while (notas.moveToNext());
            }
            return mDb.delete(DATABASE_TABLE_CATEGORIES, KEY_ROWID + "=" + rowId, null) > 0;
        }
        else return false;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     *
     * @return Cursor over all notes
     */
    public Cursor fetchAllCategories() {

        return mDb.query(DATABASE_TABLE_CATEGORIES, new String[] {KEY_ROWID, KEY_TITLE}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchCategory(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE_CATEGORIES, new String[] {KEY_ROWID,
                                KEY_TITLE}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }


    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param title id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchCategory(String title) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE_CATEGORIES, new String[] {KEY_ROWID,
                                KEY_TITLE}, KEY_TITLE + "='" + title + "'", null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     *
     * @param rowId id of note to update. -> rowId > 0
     * @param title value to set note title to. -> title != null y title.length() > 0
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateCategory(long rowId, String title) {
        ContentValues args = new ContentValues();

        if (rowId > 0 && title != null && title.length() > 0) {
            args.put(KEY_TITLE, title);
            return mDb.update(DATABASE_TABLE_CATEGORIES, args, KEY_ROWID + "=" + rowId, null) > 0;
        }
        else return false;

    }
}