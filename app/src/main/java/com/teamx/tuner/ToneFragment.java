package com.teamx.tuner;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pkmmte.pkrss.Article;
import com.pkmmte.pkrss.Callback;
import com.pkmmte.pkrss.PkRSS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ToneFragment extends Fragment implements Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    Context context;
    RecyclerView recyclerView;
    ArrayList<Tone> toneList = new ArrayList<>();
    ToneRecyclerViewAdapter adapter;
    View loadingView;
    MediaPlayer mediaPlayer;
    int pos = -1;

    public ToneFragment() {
    }

    public static ToneFragment newInstance() {
        ToneFragment fragment = new ToneFragment();
        Bundle args = new Bundle();
        // arg go here
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
//
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tone_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        loadingView = view.findViewById(R.id.loading);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (context != null) {
            PkRSS.with(context).load("http://megascripts.com/radio/?feed=rss2&amp%3Bcat=1").callback(this).async();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }

    @Override
    public void onPreload() {

    }

    @Override
    public void onLoaded(List<Article> newArticles) {
        loadingView.setVisibility(View.GONE);
        toneList = new ArrayList<>();
        for (Article article : newArticles) {
            Tone tone = new Tone();
            tone.name = article.getTitle();
            if (tone.name == null || tone.name.isEmpty()) tone.name = "Unknown song";
            if (article.getContent() != null) {
                tone.url = Html.fromHtml(article.getContent()).toString();
                if (!tone.url.contains(".mp3")) tone.url = null;
            }
            tone.on = false;
            tone.progress = 0;
            if (tone.url != null) toneList.add(tone);
        }
        adapter = new ToneRecyclerViewAdapter(toneList, new OnListFragmentInteractionListener() {
            @Override
            public void onAddToneClick(Tone item) {
                Intent intent = new Intent(context, RingtoneActivity.class);
                intent.putExtra("Tone", item);
                startActivity(intent);
            }

            @Override
            public void onPlayClick(Tone item, int position) {
                if (pos != -1) {
                    toneList.get(pos).progress = 0;
                }
                if (item.on) {
                    for (Tone tone : toneList) if (!item.equals(tone) && tone.on) tone.on = false;
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }

                    pos = position;
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setOnPreparedListener(ToneFragment.this);
                    mediaPlayer.setOnErrorListener(ToneFragment.this);
                    mediaPlayer.setOnCompletionListener(ToneFragment.this);
                    try {
                        mediaPlayer.setDataSource(item.url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.prepareAsync();
                } else {
                    // stop music
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                adapter.notifyDataSetChanged();
            }
        });
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onLoadFailed() {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        progressBarUpdater();
    }

    private void progressBarUpdater() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            toneList.get(pos).progress = (int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100);
            adapter.notifyDataSetChanged();
            Runnable notification = new Runnable() {
                public void run() {
                    progressBarUpdater();
                }
            };
            new Handler().postDelayed(notification, 1000);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mediaPlayer.release();
        mediaPlayer = null;
        toneList.get(pos).on = false;
        toneList.get(pos).progress = 0;
        adapter.notifyDataSetChanged();
    }

    public interface OnListFragmentInteractionListener {
        void onAddToneClick(Tone item);

        void onPlayClick(Tone item, int position);
    }
}
