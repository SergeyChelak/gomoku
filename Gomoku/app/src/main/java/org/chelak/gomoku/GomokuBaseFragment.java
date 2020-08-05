package org.chelak.gomoku;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by sergey.chelak on 05.02.16.
 */
public abstract class GomokuBaseFragment extends BaseFragment {

    private final static int BOARD_SIZE = 15;

    private Callbacks callbacks;

    protected GomokuLogic logic;
    protected int prevX, prevY;
    protected int curX, curY;
    protected boolean isUserMove;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        setupLogic();
        setupServiceButtons();
        GameBoardView view = getGameBoardView();
        view.setBoardSize(BOARD_SIZE);
        view.setCallbacks(new GameBoardView.Callbacks() {
            @Override
            public void onCellTouch(int x, int y) {
                if (logic.isGameOver()) {
                    makeMove();
                } else {
                    curX = x;
                    curY = y;
                    getGameBoardView().setCursor(x, y, true);
                }
            }
        });
    }

    protected GameBoardView getGameBoardView() {
        return (GameBoardView) getFragmentView().findViewById(R.id.gameBoard);
    }

    private void setupServiceButtons() {
        View view = getFragmentView();
        ImageButton btnPut = (ImageButton) view.findViewById(R.id.btnPut);
        btnPut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerMove();
            }
        });
        ImageButton btnRestart = (ImageButton) view.findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restart();
            }
        });

        ImageButton btnSound = (ImageButton) view.findViewById(R.id.btnSound);
        updateSoundButtonImage();
        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences pref = new Preferences(getActivity());
                boolean isSoundEnabled = pref.isSoundEnabled();
                pref.setSoundEnabled(!isSoundEnabled);
                updateSoundButtonImage();
            }
        });

        ImageButton btnMenu = (ImageButton)view.findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( callbacks != null )
                    callbacks.onMenu();
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void setupLogic() {
        if (logic == null) {
            logic = new GomokuLogic(BOARD_SIZE);
            resetPositions();
            // TODO implement more valid logic
            isUserMove = Math.random() > 0.5;
        } else {
            getGameBoardView().setCursor(curX, curY, false);
        }
        logic.setDelegate(new GomokuLogic.Delegate() {
            @Override
            public void onGameOver(WinState winState, GomokuLogic.WinLineItem[] winLine) {
                playSound(isUserMove ? Sound.WIN : Sound.LOOSE);
                getGameBoardView().setWinLine(winLine);
                if (callbacks != null)
                    callbacks.onGameOver();
            }

            @Override
            public void onMoveComplete(int x, int y) {
                isUserMove = !isUserMove;
                prevX = x;
                prevY = y;
                makeMove();
            }
        });
    }

    public void playerMove() {
        if (!logic.isGameOver()) {
            playSound(Sound.CLICK);
            logic.playerMove(curX, curY);
        }
    }

    private void resetPositions() {
        curX = curY = BOARD_SIZE / 2;
        prevX = prevY = -1;
        getGameBoardView().setCursor(curX, curY, false);
    }

    public void restart() {
        isUserMove = Math.random() > 0.5;
        resetPositions();
        logic.resetGame();
        getGameBoardView().resetBoard(logic.getBoard());
        makeMove();
    }

    protected boolean makeMove() {
        if (logic.isGameOver()) {
            restart();
            return false;
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private enum Sound {
        CLICK,
        WIN,
        LOOSE
    }

    private void playSound(Sound sound) {
        Preferences preferences = new Preferences(getActivity());
        if (!preferences.isSoundEnabled())
            return;
        int soundId;
        switch (sound) {
            case CLICK:
                soundId = R.raw.click;
                break;
            case WIN:
                soundId = R.raw.win;
                break;
            case LOOSE:
                soundId = R.raw.loose;
                break;
            default:
                return;
        }
        try {
            MediaPlayer player = MediaPlayer.create(getActivity(), soundId);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.reset();
                    mp.release();
                }
            });
            player.start();
        } catch (Exception e) {
            //
        }
    }

    private void updateSoundButtonImage() {
        ImageButton btnSound = (ImageButton) getFragmentView().findViewById(R.id.btnSound);
        Preferences pref = new Preferences(getActivity());
        btnSound.setSelected(pref.isSoundEnabled());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public interface Callbacks {
        void onGameOver();
        void onMenu();
    }

    public Callbacks getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }
}
