package org.chelak.gomoku;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Sergey on 31.12.2015.
 */
public class GameBoardView extends View implements View.OnTouchListener {

    private int boardSize = 15;
    private int[][] board;
    private GomokuLogic.WinLineItem[] winLine;

    private Paint paint;
    private Callbacks callbacks;
    private Bitmap grid;
    private Bitmap blackStone;
    private Bitmap winner;
    private Bitmap whiteStone;
    private Bitmap prevMove;
    private Bitmap cursor;
    private int prevX, prevY;
    private int curX, curY;

    private double scaleRate;

    public GameBoardView(Context context) {
        super(context);
    }

    public GameBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        scaleRate = 0.0;
        blackStone = BitmapFactory.decodeResource(getResources(), R.drawable.stone_black);
        whiteStone = BitmapFactory.decodeResource(getResources(), R.drawable.stone_white);
        winner = BitmapFactory.decodeResource(getResources(), R.drawable.winner);
        prevMove = BitmapFactory.decodeResource(getResources(), R.drawable.stone_prev);
        cursor = BitmapFactory.decodeResource(getResources(), R.drawable.cursor);
        setOnTouchListener(this);
    }

    private static Bitmap scaleBitmap(Bitmap original, double rate) {
        int width = (int)(rate * original.getWidth());
        if ( width%2==0)
            width++;
        int height = (int)(rate * original.getHeight());
        if (height%2==0)
            height++;
        return Bitmap.createScaledBitmap(original, width, height, true);
    }

    private Bitmap scaleBitmap(Bitmap original) {
        return scaleBitmap(original, scaleRate);
    }

    private void resizeImages(Canvas canvas) {
        if ( scaleRate != 0.0 )
            return;
        int cellSize = canvas.getHeight() / boardSize;
        double rate = 1.0*cellSize / blackStone.getWidth();
        scaleRate = rate < 1 ? rate : 1;
        if ( scaleRate < 1 ) {
            blackStone = scaleBitmap(blackStone);
            whiteStone = scaleBitmap(whiteStone);
            winner = scaleBitmap(winner);
            prevMove = scaleBitmap(prevMove);
            cursor = scaleBitmap(cursor);
        }
    }

    private Bitmap createGrid(int size) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(150, 255, 255, 255);
        Paint pnt = new Paint();
        pnt.setColor(Color.BLACK);
        pnt.setStyle(Paint.Style.STROKE);
        pnt.setStrokeWidth(3);
        int cellSize = size / boardSize;
        int halfSize = cellSize/2;
        canvas.save();
        for ( int i = 0; i < boardSize; ++i ) {
            canvas.drawLine(halfSize, halfSize, halfSize, cellSize* boardSize -halfSize, pnt);
            canvas.translate(cellSize, 0);
        }
        canvas.restore();
        for ( int i = 0; i < boardSize; ++i ) {
            canvas.drawLine(halfSize, halfSize, cellSize* boardSize -halfSize, halfSize, pnt);
            canvas.translate(0, cellSize);
        }
        return bitmap;
    }

    protected void drawStones(Canvas canvas) {
        int cellSize = canvas.getHeight() / boardSize;
        int offset = (cellSize - blackStone.getWidth()) / 2;
        for ( int i = 0; i < boardSize; ++i ) {
            float x = cellSize*i + offset;
            for ( int j = 0; j < boardSize; ++j ) {
                if ( board[i][j] == GomokuLogic.NONE )
                    continue;
                Bitmap bmp = board[i][j] == GomokuLogic.CROSS ? blackStone : whiteStone;
                float y = cellSize*j + offset;
                canvas.drawBitmap(bmp, x, y, paint);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        resizeImages(canvas);
        if ( grid == null )
            grid = createGrid(getWidth());
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(grid, 0, 0, paint);
        drawStones(canvas);
        if ( winLine != null )
            drawWinLine(canvas);
        if ( prevX >= 0 && prevY >= 0 )
            showPrevMove(canvas);
        showCursor(canvas);
    }

    private void drawWinLine(Canvas canvas) {
        int cellSize = canvas.getHeight() / boardSize;
        int offset = (cellSize - winner.getWidth()) / 2;
        for (GomokuLogic.WinLineItem item : winLine) {
            float x = cellSize*item.first + offset;
            float y = cellSize*item.second + offset;
            canvas.drawBitmap(winner, x, y, paint);
        }
    }

    private void showPrevMove(Canvas canvas) {
        int cellSize = canvas.getHeight() / boardSize;
        int offset = (cellSize - prevMove.getWidth()) / 2;
        float posX = cellSize* prevX + offset;
        float posY = cellSize* prevY + offset;
        canvas.drawBitmap(prevMove, posX, posY, paint);
    }

    private void showCursor(Canvas canvas) {
        int cellSize = canvas.getHeight() / boardSize;
        int offset = (cellSize - cursor.getWidth()) / 2;
        float posX = cellSize*curX + offset;
        float posY = cellSize*curY + offset;
        canvas.drawBitmap(cursor, posX, posY, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(size, size);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if ( event.getAction() == MotionEvent.ACTION_DOWN ||
                event.getAction() == MotionEvent.ACTION_MOVE ) {
            int cellSize = v.getHeight() / boardSize;
            int x = (int) (event.getX() / cellSize);
            int y = (int) (event.getY() / cellSize);
            if (callbacks!=null && x < boardSize && y < boardSize && x >= 0 && y >= 0 )
                callbacks.onCellTouch(x,y);
        }
        return true;
    }

    public void setCursor(int x, int y, boolean invalidate) {
        curX = x;
        curY = y;
        if ( invalidate )
            invalidate();
    }

    public void setBoard(int[][] board, int x, int y) {
        this.prevX = x;
        this.prevY = y;
        this.board = board;
        invalidate();
    }

    public void resetBoard(int[][] board) {
        this.winLine = null;
        setBoard(board, -1, -1);
    }

    public void setWinLine(GomokuLogic.WinLineItem[] winLine) {
        this.winLine = winLine;
        invalidate();
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public interface Callbacks {
        void onCellTouch(int x, int y);
    }
}
