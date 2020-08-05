package org.chelak.gomoku;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

/**
 * Created by Sergey on 31.12.2015.
 */
public class GomokuActivity extends Activity {

    private final static String BUNDLE_FRAGMENT_ID = "gomokuFragment";
    private final static String GAME_MODE = "game.mode";

    private GomokuBaseFragment gomokuFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gomoku);
        updateActiveFragment();
    }

    private void setActiveMode(long mode) {
        Preferences pref = new Preferences(this);
        pref.putLong(GAME_MODE, mode);
        updateActiveFragment();
    }

    private void updateActiveFragment() {
        Preferences pref = new Preferences(this);
        long mode = pref.getLong(GAME_MODE, 0);
        GomokuBaseFragment fragment = mode == 0 ? GomokuFragment.getInstance() :
                GomokuTwoPlayersFragment.getInstance();
        replaceFragment(fragment);
    }

    private void replaceFragment(GomokuBaseFragment fragment) {
        this.gomokuFragment = fragment;
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        gomokuFragment.setCallbacks(new GomokuFragment.Callbacks() {
            @Override
            public void onGameOver() {
                //
            }

            @Override
            public void onMenu() {
                showMenuDialog();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getFragmentManager().putFragment(outState, BUNDLE_FRAGMENT_ID, gomokuFragment);
    }

    private void showMenuDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_game_mode, null);
        builder.setView(view);
        final Dialog dialog = builder.create();
        {
            TextView txt = (TextView) view.findViewById(R.id.dialogTitle);
            Typeface font = Typeface.createFromAsset(GomokuActivity.this.getAssets(), "all_yoko.ttf");
            txt.setTypeface(font);

            ImageButton btnPlayerVsAndroid = (ImageButton)view.findViewById(R.id.buttonAndroid);
            btnPlayerVsAndroid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setActiveMode(0);
                    dialog.dismiss();
                }
            });
            ImageButton btnTwoPlayers = (ImageButton)view.findViewById(R.id.buttonTwoPlayers);
            btnTwoPlayers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setActiveMode(1);
                    dialog.dismiss();
                }
            });
            ImageButton btnFacebook = (ImageButton)view.findViewById(R.id.buttonPublic);
            btnFacebook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = getString(R.string.url_public_facebook);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }
}
