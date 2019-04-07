package es.unizar.eina.notepadv3;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static es.unizar.eina.notepadv3.NotesDbAdapter.KEY_ROWID;
import static es.unizar.eina.notepadv3.NotesDbAdapter.KEY_TITLE;

public class NoteEdit extends AppCompatActivity {
    private EditText mTitleText;
    private EditText mBodyText;
    private TextView mIDText;
    private Long mRowId;
    private NotesDbAdapter mDbHelper;
    private Spinner mCatDropdown;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note);

        mIDText = (TextView) findViewById(R.id.NoteID);
        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mCatDropdown = (Spinner) findViewById(R.id.spinner1);

        // Esto carga las categorias en el spinner de edit note
        List<String> items = new ArrayList<>();
        Cursor mCategoryCursor = mDbHelper.fetchAllCategories();
        if (mCategoryCursor.moveToFirst()) {
            do {
                items.add(mCategoryCursor.getString(1));
            } while (mCategoryCursor.moveToNext());
        }
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        mCatDropdown.setAdapter((SpinnerAdapter) adapter);


        Button confirmButton = (Button) findViewById(R.id.confirm);

        mIDText.setText("****");

        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = (extras != null) ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                    : null;
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mIDText.setText(mRowId.toString());
            mTitleText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
            // Si la categoria que tiene asignada existe
            Cursor category = mDbHelper.fetchCategory(note.getInt(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_CATEGORY)));
            if(category.getCount() != 0){
                String title = category.getString(category.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
                // Busca el la posicion en el dropdown a partir del nombre
                mCatDropdown.setSelection(((ArrayAdapter<String>)mCatDropdown.getAdapter()).getPosition(title));
            }
            // Si no existe, por defecto tiene la categoria vacia
            else {
                mCatDropdown.setSelection(0);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
    }
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }
    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();
        String category = mCatDropdown.getSelectedItem().toString();
        // Por defecto categoria "vacia"
        int idCat = 1;
        if (category != null) {
            Cursor categoria = mDbHelper.fetchCategory(category);
            idCat = categoria.getInt(categoria.getColumnIndexOrThrow(KEY_ROWID));
            System.out.println(idCat);
        }
        if (mRowId == null) {
            long id = mDbHelper.createNote(title, body, idCat);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, title, body, idCat);
        }
    }
}
