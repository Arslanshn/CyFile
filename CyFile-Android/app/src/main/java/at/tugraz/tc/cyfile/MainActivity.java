package at.tugraz.tc.cyfile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import at.tugraz.tc.cyfile.note.NoteService;
import at.tugraz.tc.cyfile.ui.PatternLockActivity;

/**
 * Main activity
 * Created by cobri on 3/21/2018.
 */
public class MainActivity extends AppCompatActivity {

    @Inject
    NoteService noteService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((CyFileApplication) getApplication()).getNoteComponent().inject(this);

        Intent intent = new Intent(this, PatternLockActivity.class);
        startActivity(intent);
    }
}
