package com.kubix.myjobwallet.spese;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.kubix.myjobwallet.MainActivity;
import com.kubix.myjobwallet.R;
import com.kubix.myjobwallet.fragment.BtnSheetSpeseFragment;
import com.kubix.myjobwallet.setting.ClsSettings;
import com.kubix.myjobwallet.utility.BottomNavigationViewHelper;
import com.kubix.myjobwallet.utility.VariabiliGlobali;

import java.util.ArrayList;
import java.util.List;

public class SpeseActivity extends AppCompatActivity implements View.OnClickListener {



    // LISTA RECYCLER VIEW
    private List<Uscite> usciteList = new ArrayList<>();
    private RecyclerView recyclerView;
    private com.kubix.myjobwallet.spese.CustomAdapter mAdapter;

    //OGGETTI PER MODIFICA
    EditText testoModifica;
    Button buttonModifica;

    //ADMOB NATIVA
    private static String LOG_TAG = "EXAMPLE";
    private AdView mAdView;
    VideoController mVideoController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ClsSettings settings = new ClsSettings(getBaseContext());
        if (settings.get_temadark()) {
            setTheme(R.style.AppTheme_Dark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spese);

        // AdMob
        MobileAds.initialize(this, "ca-app-pub-9460579775308491~5760945149");
        mAdView = findViewById(R.id.ad_view_spese);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        //INDICIZZA
        recyclerView = (RecyclerView) findViewById(R.id.listaSpese);

        //SETTAGGI RECYCLER VIEW
        mAdapter = new com.kubix.myjobwallet.spese.CustomAdapter(usciteList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new com.kubix.myjobwallet.spese.RecyclerTouchListener(getApplicationContext(), recyclerView, new com.kubix.myjobwallet.spese.RecyclerTouchListener.ClickListener() {

            //EVENTI DI CLICK DEL RECYCLER
            @Override
            public void onClick(View view, int position) {

                //MODIFICA ENTRATA
                final Uscite movie = usciteList.get(position);
                vecchiaData = movie.getDataUscita();
                vecchioTitolo = movie.getTitolo();
                vecchiaCifra = movie.getUscita();
                vecchioTag = movie.getCategoriaUscita();

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SpeseActivity.this);
                LayoutInflater inflater = SpeseActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.custom_modifica_uscite, null);
                dialogBuilder.setView(dialogView);
                testoModifica = (EditText) dialogView.findViewById(R.id.testoModificaCifraUscita);
                buttonModifica = (Button) dialogView.findViewById(R.id.bottoneModificaCifraUscita);
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }

            @Override
            public void onLongClick(View view, final int position) {

                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(SpeseActivity.this);
                builder.setTitle("Elimina")
                        .setMessage("Vuoi Eliminare?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // ELIMINAZIONE
                                final Uscite movie = usciteList.get(position);
                                MainActivity.db.execSQL("DELETE FROM Uscite WHERE Titolo = '"+movie.getTitolo()+"' AND Cifra = '"+movie.getUscita()+"' AND Ora = '"+movie.getPromemoria()+"' AND Data = '"+movie.getDataUscita()+"' AND Categoria = '"+movie.getCategoriaUscita()+"'");
                                usciteList.remove(position);
                                mAdapter.notifyItemRemoved(position);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // NOTHING
                            }
                        })
                        .setIcon(R.drawable.ic_dialog_alert)
                        .show();
            }
        }));

        //TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSpese);
        setTitle("Spese");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //CONTROLLO PER LA VISUALIZZAZIONE DELL'ADD VIEW
        if (VariabiliGlobali.statoPremium.equals("SI")){
            mAdView.setVisibility(View.GONE);
        }

        caricaEntrate();

        //BottomBar
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_category:
                        BottomSheetDialogFragment bottomSheetDialogFragment = new BtnSheetSpeseFragment();
                        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                        break;
                    case R.id.action_add:
                        startActivity(new Intent(SpeseActivity.this, SpeseAggiungiActivity.class));
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public void onRestart(){
        super.onRestart();
        caricaEntrate();
    }

    public void caricaEntrate(){
        //CARICA NOTE IN LISTA
        try {
            usciteList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Uscite ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloSpesa=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraSpesa=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoCategoriaSpesa=cr.getString(cr.getColumnIndex("Categoria"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataSpesa= cr.getString(cr.getColumnIndex("Data"));
                        String campoTestoGiorno = cr.getString(cr.getColumnIndex("GiornoTesto"));
                        String campoNumeroGiorno = cr.getString(cr.getColumnIndex("GiornoNumero"));
                        String campoNumeroMese = cr.getString(cr.getColumnIndex("MeseNumero"));
                        String campoNumeroAnno = cr.getString(cr.getColumnIndex("AnnoNumero"));
                        Uscite uscite = new Uscite (campoTitoloSpesa, campoCifraSpesa, campoPromemoria,campoDataSpesa, campoCategoriaSpesa, campoTestoGiorno, campoNumeroGiorno, campoNumeroMese, campoNumeroAnno);
                        usciteList.add(uscite);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else{
                    //NOTHING
                }
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //EVENTI BOTTOMSHEET ENTRATE
    public void clickBottomSheetSpeseCasa(View v){
        //CARICA SPESE CASA IN LISTA
        try {
            usciteList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Uscite WHERE Categoria = 'Casa' ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloSpesa=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraSpesa=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoCategoriaSpesa=cr.getString(cr.getColumnIndex("Categoria"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataSpesa= cr.getString(cr.getColumnIndex("Data"));
                        String campoTestoGiorno = cr.getString(cr.getColumnIndex("GiornoTesto"));
                        String campoNumeroGiorno = cr.getString(cr.getColumnIndex("GiornoNumero"));
                        String campoNumeroMese = cr.getString(cr.getColumnIndex("MeseNumero"));
                        String campoNumeroAnno = cr.getString(cr.getColumnIndex("AnnoNumero"));
                        Uscite uscite = new Uscite (campoTitoloSpesa, campoCifraSpesa, campoPromemoria,campoDataSpesa, campoCategoriaSpesa, campoTestoGiorno, campoNumeroGiorno, campoNumeroMese, campoNumeroAnno);
                        usciteList.add(uscite);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else{
                    //NOTHING
                }
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void clickBottomSheetSpeseTrasporti(View v){
        //CARICA SPESE TRASPORTI IN LISTA
        try {
            usciteList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Uscite WHERE Categoria = 'Trasporti' ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloSpesa=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraSpesa=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoCategoriaSpesa=cr.getString(cr.getColumnIndex("Categoria"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataSpesa= cr.getString(cr.getColumnIndex("Data"));
                        String campoTestoGiorno = cr.getString(cr.getColumnIndex("GiornoTesto"));
                        String campoNumeroGiorno = cr.getString(cr.getColumnIndex("GiornoNumero"));
                        String campoNumeroMese = cr.getString(cr.getColumnIndex("MeseNumero"));
                        String campoNumeroAnno = cr.getString(cr.getColumnIndex("AnnoNumero"));
                        Uscite uscite = new Uscite (campoTitoloSpesa, campoCifraSpesa, campoPromemoria,campoDataSpesa, campoCategoriaSpesa, campoTestoGiorno, campoNumeroGiorno, campoNumeroMese, campoNumeroAnno);
                        usciteList.add(uscite);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else{
                    //NOTHING
                }
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void clickBottomSheetSpeseAuto(View v){
        //CARICA SPESE AUTO IN LISTA
        try {
            usciteList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Uscite WHERE Categoria = 'Auto' ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloSpesa=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraSpesa=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoCategoriaSpesa=cr.getString(cr.getColumnIndex("Categoria"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataSpesa= cr.getString(cr.getColumnIndex("Data"));
                        String campoTestoGiorno = cr.getString(cr.getColumnIndex("GiornoTesto"));
                        String campoNumeroGiorno = cr.getString(cr.getColumnIndex("GiornoNumero"));
                        String campoNumeroMese = cr.getString(cr.getColumnIndex("MeseNumero"));
                        String campoNumeroAnno = cr.getString(cr.getColumnIndex("AnnoNumero"));
                        Uscite uscite = new Uscite (campoTitoloSpesa, campoCifraSpesa, campoPromemoria,campoDataSpesa, campoCategoriaSpesa, campoTestoGiorno, campoNumeroGiorno, campoNumeroMese, campoNumeroAnno);
                        usciteList.add(uscite);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else{
                    //NOTHING
                }
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void clickBottomSheetSpeseCarburante(View v){
        //CARICA SPESE CARBURANTE IN LISTA
        try {
            usciteList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Uscite WHERE Categoria = 'Carburante' ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloSpesa=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraSpesa=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoCategoriaSpesa=cr.getString(cr.getColumnIndex("Categoria"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataSpesa= cr.getString(cr.getColumnIndex("Data"));
                        String campoTestoGiorno = cr.getString(cr.getColumnIndex("GiornoTesto"));
                        String campoNumeroGiorno = cr.getString(cr.getColumnIndex("GiornoNumero"));
                        String campoNumeroMese = cr.getString(cr.getColumnIndex("MeseNumero"));
                        String campoNumeroAnno = cr.getString(cr.getColumnIndex("AnnoNumero"));
                        Uscite uscite = new Uscite (campoTitoloSpesa, campoCifraSpesa, campoPromemoria,campoDataSpesa, campoCategoriaSpesa, campoTestoGiorno, campoNumeroGiorno, campoNumeroMese, campoNumeroAnno);
                        usciteList.add(uscite);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else{
                    //NOTHING
                }
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void clickBottomSheetSpeseBollette(View v){
        //CARICA SPESE BOLLETTE IN LISTA
        try {
            usciteList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Uscite WHERE Categoria = 'Bollette' ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloSpesa=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraSpesa=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoCategoriaSpesa=cr.getString(cr.getColumnIndex("Categoria"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataSpesa= cr.getString(cr.getColumnIndex("Data"));
                        String campoTestoGiorno = cr.getString(cr.getColumnIndex("GiornoTesto"));
                        String campoNumeroGiorno = cr.getString(cr.getColumnIndex("GiornoNumero"));
                        String campoNumeroMese = cr.getString(cr.getColumnIndex("MeseNumero"));
                        String campoNumeroAnno = cr.getString(cr.getColumnIndex("AnnoNumero"));
                        Uscite uscite = new Uscite (campoTitoloSpesa, campoCifraSpesa, campoPromemoria,campoDataSpesa, campoCategoriaSpesa, campoTestoGiorno, campoNumeroGiorno, campoNumeroMese, campoNumeroAnno);
                        usciteList.add(uscite);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else{
                    //NOTHING
                }
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void clickBottomSheetSpeseShopping(View v){
        //CARICA SPESE SHOPPING IN LISTA
        try {
            usciteList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Uscite WHERE Categoria = 'Shopping' ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloSpesa=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraSpesa=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoCategoriaSpesa=cr.getString(cr.getColumnIndex("Categoria"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataSpesa= cr.getString(cr.getColumnIndex("Data"));
                        String campoTestoGiorno = cr.getString(cr.getColumnIndex("GiornoTesto"));
                        String campoNumeroGiorno = cr.getString(cr.getColumnIndex("GiornoNumero"));
                        String campoNumeroMese = cr.getString(cr.getColumnIndex("MeseNumero"));
                        String campoNumeroAnno = cr.getString(cr.getColumnIndex("AnnoNumero"));
                        Uscite uscite = new Uscite (campoTitoloSpesa, campoCifraSpesa, campoPromemoria,campoDataSpesa, campoCategoriaSpesa, campoTestoGiorno, campoNumeroGiorno, campoNumeroMese, campoNumeroAnno);
                        usciteList.add(uscite);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else{
                    //NOTHING
                }
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void clickBottomSheetSpeseCibo(View v){
        //CARICA SPESE CIBO E BEVANDE IN LISTA
        try {
            usciteList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Uscite WHERE Categoria = 'Cibo & Bevande' ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloSpesa=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraSpesa=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoCategoriaSpesa=cr.getString(cr.getColumnIndex("Categoria"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataSpesa= cr.getString(cr.getColumnIndex("Data"));
                        String campoTestoGiorno = cr.getString(cr.getColumnIndex("GiornoTesto"));
                        String campoNumeroGiorno = cr.getString(cr.getColumnIndex("GiornoNumero"));
                        String campoNumeroMese = cr.getString(cr.getColumnIndex("MeseNumero"));
                        String campoNumeroAnno = cr.getString(cr.getColumnIndex("AnnoNumero"));
                        Uscite uscite = new Uscite (campoTitoloSpesa, campoCifraSpesa, campoPromemoria,campoDataSpesa, campoCategoriaSpesa, campoTestoGiorno, campoNumeroGiorno, campoNumeroMese, campoNumeroAnno);
                        usciteList.add(uscite);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else{
                    //NOTHING
                }
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void clickBottomSheetSpeseSvago(View v){
        //CARICA SPESE SVAGO IN LISTA
        try {
            usciteList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Uscite WHERE Categoria = 'Svago' ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloSpesa=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraSpesa=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoCategoriaSpesa=cr.getString(cr.getColumnIndex("Categoria"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataSpesa= cr.getString(cr.getColumnIndex("Data"));
                        String campoTestoGiorno = cr.getString(cr.getColumnIndex("GiornoTesto"));
                        String campoNumeroGiorno = cr.getString(cr.getColumnIndex("GiornoNumero"));
                        String campoNumeroMese = cr.getString(cr.getColumnIndex("MeseNumero"));
                        String campoNumeroAnno = cr.getString(cr.getColumnIndex("AnnoNumero"));
                        Uscite uscite = new Uscite (campoTitoloSpesa, campoCifraSpesa, campoPromemoria,campoDataSpesa, campoCategoriaSpesa, campoTestoGiorno, campoNumeroGiorno, campoNumeroMese, campoNumeroAnno);
                        usciteList.add(uscite);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else{
                    //NOTHING
                }
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void clickBottomSheetSpeseViaggi(View v){
        //CARICA SPESE VIAGGI IN LISTA
        try {
            usciteList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Uscite WHERE Categoria = 'Viaggi' ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloSpesa=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraSpesa=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoCategoriaSpesa=cr.getString(cr.getColumnIndex("Categoria"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataSpesa= cr.getString(cr.getColumnIndex("Data"));
                        String campoTestoGiorno = cr.getString(cr.getColumnIndex("GiornoTesto"));
                        String campoNumeroGiorno = cr.getString(cr.getColumnIndex("GiornoNumero"));
                        String campoNumeroMese = cr.getString(cr.getColumnIndex("MeseNumero"));
                        String campoNumeroAnno = cr.getString(cr.getColumnIndex("AnnoNumero"));
                        Uscite uscite = new Uscite (campoTitoloSpesa, campoCifraSpesa, campoPromemoria,campoDataSpesa, campoCategoriaSpesa, campoTestoGiorno, campoNumeroGiorno, campoNumeroMese, campoNumeroAnno);
                        usciteList.add(uscite);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else{
                    //NOTHING
                }
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void clickBottomSheetSpeseAltro(View v){
        //CARICA SPESE ALTRO IN LISTA
        try {
            usciteList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Uscite WHERE Categoria = 'Altro' ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloSpesa=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraSpesa=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoCategoriaSpesa=cr.getString(cr.getColumnIndex("Categoria"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataSpesa= cr.getString(cr.getColumnIndex("Data"));
                        String campoTestoGiorno = cr.getString(cr.getColumnIndex("GiornoTesto"));
                        String campoNumeroGiorno = cr.getString(cr.getColumnIndex("GiornoNumero"));
                        String campoNumeroMese = cr.getString(cr.getColumnIndex("MeseNumero"));
                        String campoNumeroAnno = cr.getString(cr.getColumnIndex("AnnoNumero"));
                        Uscite uscite = new Uscite (campoTitoloSpesa, campoCifraSpesa, campoPromemoria,campoDataSpesa, campoCategoriaSpesa, campoTestoGiorno, campoNumeroGiorno, campoNumeroMese, campoNumeroAnno);
                        usciteList.add(uscite);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else{
                    //NOTHING
                }
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void clickBottomSheetSpeseTutte(View v){
        //CARICA TUTTE LE SPESE IN LISTA
        try {
            usciteList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Uscite ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloSpesa=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraSpesa=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoCategoriaSpesa=cr.getString(cr.getColumnIndex("Categoria"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataSpesa= cr.getString(cr.getColumnIndex("Data"));
                        String campoTestoGiorno = cr.getString(cr.getColumnIndex("GiornoTesto"));
                        String campoNumeroGiorno = cr.getString(cr.getColumnIndex("GiornoNumero"));
                        String campoNumeroMese = cr.getString(cr.getColumnIndex("MeseNumero"));
                        String campoNumeroAnno = cr.getString(cr.getColumnIndex("AnnoNumero"));
                        Uscite uscite = new Uscite (campoTitoloSpesa, campoCifraSpesa, campoPromemoria,campoDataSpesa, campoCategoriaSpesa, campoTestoGiorno, campoNumeroGiorno, campoNumeroMese, campoNumeroAnno);
                        usciteList.add(uscite);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else{
                    //NOTHING
                }
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    String vecchiaData;
    String vecchioTitolo;
    String vecchiaCifra;
    String vecchioTag;

    public void modificaSpesa(View v){
       if (! testoModifica.getText().toString().equals("")){
           MainActivity.db.execSQL("UPDATE Uscite SET Cifra = '"+testoModifica.getText().toString()+"' WHERE Data = '"+vecchiaData+"' AND Titolo = '"+vecchioTitolo+"' AND Cifra = '"+vecchiaCifra+"' AND Categoria = '"+vecchioTag+"'");
           Toast.makeText(this, "Modificata!", Toast.LENGTH_SHORT).show();
           finish();
       }else{
           //NOTHING
       }
    }

    // Freccia Indietro
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }
}
