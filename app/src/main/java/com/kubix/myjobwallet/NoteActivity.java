package com.kubix.myjobwallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by mowmo on 26/09/17.
 */

public class NoteActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingAddNote);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity (new Intent(NoteActivity.this,NoteAggiungiActivity.class));

            }
        });
    }
}