package com.kubix.myjobwallet.entrate;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.kubix.myjobwallet.MainActivity;
import com.kubix.myjobwallet.R;
import com.kubix.myjobwallet.fragment.BtnSheetEntrateFragment;
import java.util.ArrayList;
import java.util.List;

public class EntrateActivity extends AppCompatActivity implements View.OnClickListener {

    //FAB
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    private Animation fab_apri, fab_chiudi, fab_ruota_avanti, fab_ruota_indietro;

    //ADMOB NATIVA
    private static String LOG_TAG = "ENTRATE";
    NativeExpressAdView mAdView;
    VideoController mVideoController;

    // LISTA RECYCLER VIEW
    private List<Entrate> entrateList = new ArrayList<>();
    private RecyclerView recyclerView;
    private com.kubix.myjobwallet.entrate.CustomAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrate);

        //INDICIZZA
        recyclerView = (RecyclerView) findViewById(R.id.listaEntrate);

        //SETTAGGI RECYCLER VIEW
        mAdapter = new com.kubix.myjobwallet.entrate.CustomAdapter(entrateList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {

            //EVENTI DI CLICK DEL FOTTUTO RECYCLER
            @Override
            public void onClick(View view, int position) {
                //final Uscite movie = entrateList.get(position);
                //Toast.makeText(getApplicationContext(), movie.getTitolo() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {
                final Entrate movie = entrateList.get(position);
                MainActivity.db.execSQL("DELETE FROM Entrate WHERE Titolo = '"+movie.getTitolo()+"' AND Cifra = '"+movie.getEntrata()+"' AND Ora = '"+movie.getPromemoria()+"' AND Data = '"+movie.getDataEntrata()+"'");
                entrateList.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
        }));

        //TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarEntrate);
        setTitle(R.string.toolbarEntrate);
        toolbar.setTitleTextColor(getResources().getColor(R.color.coloreTestoBianco));
        setSupportActionBar(toolbar);

        //ADMOB NATIVA
        mAdView = (NativeExpressAdView) findViewById(R.id.adViewEntrate);
        mAdView.setVideoOptions(new VideoOptions.Builder()
                .setStartMuted(true)
                .build());
        mVideoController = mAdView.getVideoController();
        mVideoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
            @Override
            public void onVideoEnd() {
                Log.d(LOG_TAG, "Video playback is finished.");
                super.onVideoEnd();
            }
        });
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (mVideoController.hasVideoContent()) {
                    Log.d(LOG_TAG, "Received an ad that contains a video asset.");
                } else {
                    Log.d(LOG_TAG, "Received an ad that does not contain a video asset.");
                }
            }
        });

        mAdView.loadAd(new AdRequest.Builder().build());


        //FAB MENU
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab_apri = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_apri);
        fab_chiudi = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_chiudi);
        fab_ruota_avanti = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_ruota_avanti);
        fab_ruota_indietro = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_ruota_indietro);
        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);

        caricaEntrate();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                animateFAB();
                break;
            case R.id.fab1:
                BottomSheetDialogFragment bottomSheetDialogFragment = new BtnSheetEntrateFragment();
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                break;
            case R.id.fab2:
                startActivity(new Intent(this, EntrateAggiungiActivity.class));
                break;
        }
    }

    public void animateFAB() {

        if (isFabOpen) {

            fab.startAnimation(fab_ruota_indietro);
            fab1.startAnimation(fab_chiudi);
            fab2.startAnimation(fab_chiudi);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
            Log.d("Raj", "close");

        } else {

            fab.startAnimation(fab_ruota_avanti);
            fab1.startAnimation(fab_apri);
            fab2.startAnimation(fab_apri);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
            Log.d("Raj", "open");

        }

    }

    @Override
    public void onRestart(){
        super.onRestart();
        caricaEntrate();
    }

    public void caricaEntrate(){
        //CARICA ENTRATE IN RECYCLER
        try {
            entrateList.clear();
            mAdapter.notifyDataSetChanged();
            Cursor cr= MainActivity.db.rawQuery("SELECT * FROM Entrate ORDER BY Titolo",null);
            if(cr!=null){
                if(cr.moveToFirst()){
                    do{
                        String campoTitoloEntrata=cr.getString(cr.getColumnIndex("Titolo"));
                        String campoCifraEntrata=cr.getString(cr.getColumnIndex("Cifra"));
                        String campoPromemoria = cr.getString(cr.getColumnIndex("Ora"));
                        String campoDataEntrata= cr.getString(cr.getColumnIndex("Data"));
                        Entrate entrate = new Entrate (campoTitoloEntrata, campoCifraEntrata, campoPromemoria,campoDataEntrata);
                        entrateList.add(entrate);
                        mAdapter.notifyDataSetChanged();
                    }while (cr.moveToNext());
                }else
                    Snackbar.make(fab, getString(R.string.noEntrateAggiunte), Snackbar.LENGTH_LONG).show();
            }
            cr.close();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}

