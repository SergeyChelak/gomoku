package org.chelak.gomoku;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Sergey on 31.12.2015.
 */
public class GomokuFragment extends GomokuBaseFragment {

    public static GomokuFragment getInstance() {
        return new GomokuFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getFragmentView();
        TextView txt = (TextView) view.findViewById(R.id.textTitle);
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "all_yoko.ttf");
        txt.setTypeface(font);
    }

    @Override
    public void onResume(Preferences preferences) {
        super.onResume(preferences);
        makeMove();
    }

    @Override
    protected boolean makeMove() {
        if (super.makeMove()) {
            if (isUserMove)
                getGameBoardView().setBoard(logic.getBoard(), prevX, prevY);
            else
                logic.programMove();
            return true;
        }
        return false;
    }

    @Override
    public int getFragmentId() {
        return R.layout.fragment_gomoku;
    }

}
