package es.unizar.eina.notepadv3;
/* commit comment test */
//comemnt

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

import es.unizar.eina.send.SendAbstractionImpl;

import static es.unizar.eina.notepadv3.NotesDbAdapter.KEY_TITLE;


public class Notepadv3 extends AppCompatActivity {

    private static final int ACTIVITY_CREATE_NOTE = 0;
    private static final int ACTIVITY_EDIT_NOTE = 1;
    private static final int ACTIVITY_CREATE_CATEGORY = 2;
    private static final int ACTIVITY_EDIT_CATEGORY = 3;

    private static final int ADD_NOTE = Menu.FIRST;
    private static final int DELETE_NOTE = Menu.FIRST + 1;
    private static final int EDIT_NOTE = Menu.FIRST + 2;
    private static final int SEND_NOTE_SMS = Menu.FIRST + 3; //----- Modified
    private static final int SEND_NOTE_EMAIL = Menu.FIRST + 4; //----- Modified
    private static final int VOLUME_TESTS = Menu.FIRST + 5; //----- Modified
    private static final int OVERLOAD_TESTS = Menu.FIRST + 6; //----- Modified
    private static final int ORDER_BY_NAME = Menu.FIRST + 7; //----- Modified
    private static final int ORDER_BY_CATEGORY = Menu.FIRST + 8; //----- Modified
    private static final int FILTER_BY_CATEGORY = Menu.FIRST + 9; //----- Modified
    private static final int ADD_CATEGORY = Menu.FIRST + 10; //----- Modified
    private static final int DELETE_CATEGORY = Menu.FIRST + 11; //----- Modified
    private static final int EDIT_CATEGORY = Menu.FIRST + 12; //----- Modified
    private static final int SHOW_ALL_NOTES = Menu.FIRST + 13; //----- Modified
    private static final int CATEGORIES = Menu.FIRST + 14; //----- Modified


    private NotesDbAdapter mDbHelper;
    private ListView mList;
    private int position;
    private boolean notas = true;
    private boolean byCategory = false;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepadv3);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        mList = (ListView) findViewById(R.id.list);
        fillDataNote();

        registerForContextMenu(mList);

        mList.setSelection(0);

    }

    private void fillDataNote() {
        Cursor mNotesCursor;
        if (byCategory == false) {
            mNotesCursor = mDbHelper.fetchAllNotes();
        } else {
            mNotesCursor = mDbHelper.fetchAllNotesByCategory();
        }
        startManagingCursor(mNotesCursor);

        String[] from = new String[]{KEY_TITLE};

        int[] to = new int[]{R.id.text1};

        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notes_row, mNotesCursor, from, to);
        mList.setAdapter(notes);
    }

    private void fillDataCategory() {
        Cursor mCategoryCursor = mDbHelper.fetchAllCategories();
        startManagingCursor(mCategoryCursor);

        String[] from = new String[]{KEY_TITLE};

        int[] to = new int[]{R.id.text1};

        SimpleCursorAdapter categories =
                new SimpleCursorAdapter(this, R.layout.notes_row, mCategoryCursor, from, to);
        mList.setAdapter(categories);
    }

    private void fillDataNoteByCategory(String cat) {
        Cursor category = mDbHelper.fetchCategory(cat);
        Cursor mCategoryCursor = mDbHelper.fetchAllNotesByCategory(category.getInt(category.getColumnIndexOrThrow(NotesDbAdapter.KEY_ROWID)));
        startManagingCursor(mCategoryCursor);

        String[] from = new String[]{KEY_TITLE};

        int[] to = new int[]{R.id.text1};

        SimpleCursorAdapter categories =
                new SimpleCursorAdapter(this, R.layout.notes_row, mCategoryCursor, from, to);
        mList.setAdapter(categories);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, ADD_NOTE, Menu.NONE, R.string.menu_insert);
        menu.add(Menu.NONE, ADD_CATEGORY, Menu.NONE, "Add Category");
        menu.add(Menu.NONE, SHOW_ALL_NOTES, Menu.NONE, "Show All Notes");
        menu.add(Menu.NONE, CATEGORIES, Menu.NONE, "Show All Categories");
        menu.add(Menu.NONE, ORDER_BY_NAME, Menu.NONE, "Order Notes by Name");
        menu.add(Menu.NONE, ORDER_BY_CATEGORY, Menu.NONE, "Order Notes by Category");
        menu.add(Menu.NONE, FILTER_BY_CATEGORY, Menu.NONE, "Filter Notes by Category");
        menu.add(Menu.NONE, VOLUME_TESTS, Menu.NONE, "Volume Test");
        menu.add(Menu.NONE, OVERLOAD_TESTS, Menu.NONE, "Overload Test");

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ADD_NOTE:
                createNote();
                break;
            case ADD_CATEGORY:
                createCategory();
                break;
            case CATEGORIES:
                notas = false;
                fillDataCategory();
                break;
            case ORDER_BY_NAME:
                byCategory = false;
                notas = true;
                fillDataNote();
                break;
            case ORDER_BY_CATEGORY:
                byCategory = true;
                notas = true;
                fillDataNote();
                break;
            case FILTER_BY_CATEGORY:
                // Esto carga las categorias en el spinner de edit note
                final List<String> items = new ArrayList<>();
                Cursor mCategoryCursor = mDbHelper.fetchAllCategories();
                if (mCategoryCursor.moveToFirst()) {
                    do {
                        items.add(mCategoryCursor.getString(1));
                    } while (mCategoryCursor.moveToNext());
                }
                Dialog d = createDialog(items);
                d.show();
                break;
            case SHOW_ALL_NOTES:
                notas = true;
                fillDataNote();
                break;
            case VOLUME_TESTS:
                int category = (int) mDbHelper.createCategory("volume");
                for (int i = 1; i <= 1000; i++) {
                    mDbHelper.createNote("titulo_volumen_" + Integer.toString(i), "body" + Integer.toString(i), category);
                }
                // Refresh main page
                Intent intent_volume = getIntent();
                finish();
                startActivity(intent_volume);
                break;
            case OVERLOAD_TESTS:
                String cuerpo = "c";
                int body_length = cuerpo.length();
                category = (int) mDbHelper.createCategory("overload");
                for (int i = 1; i <= 21; i++) {
                    body_length = cuerpo.length(); // para debug
                    mDbHelper.createNote("titulo_overload_" + Integer.toString(i), cuerpo, category);
                    cuerpo = cuerpo + cuerpo;
                }
                // Refresh main page
                Intent intent_overload = getIntent();
                finish();
                startActivity(intent_overload);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Genera en pantalla las diferentes opciones disponibles al realizar una pulsacion larga sobre
     * una nota existente
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (notas == true){
            menu.add(Menu.NONE, DELETE_NOTE, Menu.NONE, R.string.menu_delete);
            menu.add(Menu.NONE, EDIT_NOTE, Menu.NONE, R.string.menu_edit);
            menu.add(Menu.NONE, SEND_NOTE_SMS, Menu.NONE, "Send Note SMS");
            menu.add(Menu.NONE, SEND_NOTE_EMAIL, Menu.NONE, "Send Note EMAIL");
        }
        else {
            menu.add(Menu.NONE, DELETE_CATEGORY, Menu.NONE, "Delete Category");
            menu.add(Menu.NONE, EDIT_CATEGORY, Menu.NONE, "Edit Category");
        }
    }

    /**
     * En funcion de la seleccion realizada, hará una accion u otra (borrar, editar, enviar por
     * SMS, enviar por email
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case DELETE_NOTE:
                mDbHelper.deleteNote(info.id);
                fillDataNote();
                mList.setSelection(info.position);
                break;

            case EDIT_NOTE:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                editNote(info.position, info.id);
                break;

            case SEND_NOTE_SMS:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

                Cursor note = mDbHelper.fetchNote(info.id);
                SendAbstractionImpl SAI = new SendAbstractionImpl(this, "SMS");

                startManagingCursor(note);

                SAI.send(note.getString(1), note.getString(2));
                break;

            case SEND_NOTE_EMAIL:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

                Cursor noteE = mDbHelper.fetchNote(info.id);
                SendAbstractionImpl SAIE = new SendAbstractionImpl(this, "");

                startManagingCursor(noteE);

                SAIE.send(noteE.getString(1), noteE.getString(2));
                break;

            case DELETE_CATEGORY:
                mDbHelper.deleteCategory(info.id);
                fillDataCategory();
                mList.setSelection(info.position);
                break;

            case EDIT_CATEGORY:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                editCategory(info.position, info.id);
                break;
        }

        return super.onContextItemSelected(item);

    }

    private void createNote() {
        Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE_NOTE);
    }


    protected void editNote(int posicion, long id) {
        Intent i = new Intent(this, NoteEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT_NOTE);
        position = posicion;
    }

    private void createCategory() {
        Intent i = new Intent(this, CatEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE_CATEGORY);
    }

    private void editCategory(int posicion, long id) {
        Intent i = new Intent(this, CatEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT_CATEGORY);
        position = posicion;
    }


    /*comment */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (notas == true) {
            fillDataNote();
        } else {
            fillDataCategory();
        }

        mList.setSelection(position); // para guardar la posicion de la ultima nota editada
    }

    private Dialog createDialog(final List<String> items) {
        CharSequence[] cs = items.toArray(new CharSequence[items.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose category:");
        builder.setItems(cs, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String item = items.get(which);
                fillDataNoteByCategory(item);
            }
        });
        return builder.create();
    }

}


//frasanz@bifi.es