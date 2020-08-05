package org.chelak.gomoku;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by sergey.chelak on 05.02.16.
 */
public class GomokuTwoPlayersFragment extends GomokuBaseFragment {

    public static GomokuTwoPlayersFragment getInstance() {
        return new GomokuTwoPlayersFragment();
    }

    @Override
    public int getFragmentId() {
        return R.layout.fragment_two_players;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getFragmentView();
        ImageButton btnPut = (ImageButton) view.findViewById(R.id.btnPutSecond);
        btnPut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerMove();
            }
        });
        updatePutButtonStates();
    }

    @Override
    public void onResume(Preferences preferences) {
        super.onResume(preferences);
        makeMove();
    }

    @Override
    protected boolean makeMove() {
        if (super.makeMove()) {
            getGameBoardView().setBoard(logic.getBoard(), prevX, prevY);
            updatePutButtonStates();
            return true;
        }
        return false;
    }

    private void updatePutButtonStates() {
        View view = getFragmentView();
        ImageButton btnFirst = (ImageButton)view.findViewById(R.id.btnPut);
        ImageButton btnSecond  = (ImageButton)view.findViewById(R.id.btnPutSecond);
        btnFirst.setEnabled(isUserMove);
        btnSecond.setEnabled(!isUserMove);
    }
}
