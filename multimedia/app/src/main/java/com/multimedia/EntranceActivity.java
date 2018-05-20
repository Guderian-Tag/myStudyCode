package com.multimedia;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.multimedia.project.MixtureActivity;
import com.multimedia.project.ProjectActivity;
import com.multimedia.project.RecordAudioActivity;

public class EntranceActivity extends AppCompatActivity implements View.OnClickListener{

    Button recordAndPlay;
    Button opengl;
    Button mediaManager;
    Button mix;
    Button recordAudio;
    Button dubbing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        init();
    }

    private void init() {
        recordAndPlay = (Button) findViewById(R.id.record_and_pay);
        opengl = (Button) findViewById(R.id.opengl);
        mediaManager = (Button) findViewById(R.id.media_manager);
        recordAndPlay.setOnClickListener(this);
        opengl.setOnClickListener(this);
        mediaManager.setOnClickListener(this);
        mix = (Button) findViewById(R.id.mix);
        mix.setOnClickListener(this);
        recordAudio = (Button) findViewById(R.id.record_audio);
        recordAudio.setOnClickListener(this);
        dubbing = (Button) findViewById(R.id.dubbing);
        dubbing.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.record_and_pay:
                intent = new Intent(this,MainActivity.class);
                break;
            case R.id.opengl:
                intent = new Intent(this,OpenGLActivity.class);
                break;
            case R.id.media_manager:
                intent = new Intent(this, ProjectActivity.class);
                break;
            case R.id.mix:
                intent = new Intent(this, MixtureActivity.class);
                break;
            case R.id.record_audio:
                intent = new Intent(this, RecordAudioActivity.class);
                break;
            case R.id.dubbing:
                intent = new Intent(this,DubbingActivity.class);
                break;
        }
        startActivity(intent);
    }
}
