package es.unizar.eina.notepadv3;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CatEdit extends AppCompatActivity {
    private EditText mTitleText;
    private TextView mIDText;
    private Long mRowId;
    private NotesDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        setContentView(R.layout.cat_edit);
        setTitle(R.string.edit_category);

        mIDText = (TextView) findViewById(R.id.CategoryID);
        mTitleText = (EditText) findViewById(R.id.title);

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
            Cursor note = mDbHelper.fetchCategory(mRowId);
            startManagingCursor(note);
            mIDText.setText(mRowId.toString());
            mTitleText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
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
        String ID = mIDText.getText().toString();
        String title = mTitleText.getText().toString();
        if (mRowId == null) {
            long id = mDbHelper.createCategory(title);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateCategory(mRowId, title);
        }
    }
}
