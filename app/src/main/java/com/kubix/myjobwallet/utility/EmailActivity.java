package com.kubix.myjobwallet.utility;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.kubix.myjobwallet.R;

public class EmailActivity extends AppCompatActivity{

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        //TODO TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarEmail);
        setTitle(R.string.toolbarEmail);
        toolbar.setTitleTextColor(getResources().getColor(R.color.testoBianco));
        setSupportActionBar(toolbar);

        findViewById(R.id.fabInviaEmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });
    }

    //AVVIA IL PANNELLO DI SCELTA PER EMAIL
    private void sendFeedback() {
        final Intent _Intent = new Intent(android.content.Intent.ACTION_SEND);
        _Intent.setType("text/html");
        _Intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ getString(R.string.email) });
        _Intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.emailOggetto));
        _Intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.emailTesto));
        startActivity(Intent.createChooser(_Intent, getString(R.string.emailTitolo)));
    }
}
