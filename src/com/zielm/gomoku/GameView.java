package com.zielm.gomoku;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

public class GameView extends SurfaceView {

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN)
					nextMove((int)(event.getX() / size), (int)(event.getY() / size));
				return false;
			}
		});
	}
	
	Paint bgPaint = new Paint();
	Paint paintCircle = new Paint();
	Paint paintCross = new Paint();
	{
		bgPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		paintCircle.setARGB(0xff, 0xff, 0x00, 0x00);
		paintCircle.setStyle(Paint.Style.STROKE);
		paintCross.setARGB(0xff, 0x00, 0x00, 0xff);
	}
	
	Rect rects[] = new Rect[] { 
							new Rect(53, 12, 53 + 56, 12 + 56), // empty
							new Rect(109, 68, 109 + 56, 68 + 56), // wreck 
							new Rect(164, 12, 164 + 56, 12 + 56), // enemy
							new Rect(53, 68, 53 + 56, 68 + 56)}; // player
	
	byte EMPTY = 0;
	byte WRECK = 1;
	byte ENEMY = 2;
	byte PLAYER = 3;
	byte UNREACHABLE = 4;
	int playerX = 5, playerY = 5;
	
	void nextMove(int x, int y) {
		if(getAt(x, y) == 0) {
			map[x][y] = 1;
			System.err.println("You have set at " + x + " " + y);
			myRedraw();
			checkWin(1);
			makeMove();
			myRedraw();
			checkWin(2);
		}
	}
	
	void checkWin(int who) {
		for(int x=0; x<mw; x++) {
			int lastWrong = -1;
			for(int y=0; y<mh; y++) {
				if(map[x][y] != who) lastWrong = y;
				if(lastWrong + 5 == y) { won(who, x, y, x, lastWrong + 1); return; }
			}
		}
		for(int x=0; x<mh; x++) {
			int lastWrong = -1;
			for(int y=0; y<mw; y++) {
				if(map[y][x] != who) lastWrong = y;
				if(lastWrong + 5 == y) { won(who, y, x, lastWrong + 1, x); return; }
			}
		}
	}
	
	Rect winRect ;
	void won(int who, int x1, int y1, int x2, int y2) {
		label.setText(who == 1 ? "You won." : "You lose.");
		winRect = new Rect(x1, y1, x2, y2);
		myRedraw();
	}
	
	void makeMove() {
		int who = 1; // check for 3 seq of enemy
		for(int x=0; x<mw; x++) {
			int lastWrong = -1;
			for(int y=0; y<mh; y++) {
				if(map[x][y] != who) lastWrong = y;
				if(lastWrong + 3 == y) { 
					if(getAt(x, y + 1) == 0) {
						map[x][y + 1] = 2;
						return;
					} else if(getAt(x, lastWrong) == 0) {
						map[x][lastWrong] = 2;
						return;
					}
				}
			}
		}
		for(int y=0; y<mh; y++) {
			int lastWrong = -1;
			for(int x=0; x<mw; x++) {
				if(map[x][y] != who) lastWrong = x;
				if(lastWrong + 3 == x) { 
					if(getAt(x + 1, y) == 0) {
						map[x + 1][y] = 2;
						return;
					} else if(getAt(lastWrong, y) == 0) {
						map[lastWrong][y] = 2;
						return;
					}
				}
			}
		}
		for(int y=1; y<mh; y++) {
			for(int x=1; x<mw; x++) {
				if(getAt(x, y) == 0) {
					map[x][y] = 2;
					return;
				}
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas c) {
		c.drawRect(0, 0, getWidth(), getHeight(), bgPaint);
		Rect r = new Rect();
		Rect m = new Rect();
		System.err.println("redraw");
		for(int x=0; x<mw; x++) {
			for(int y=0; y<mh; y++) {
				r.left = x * size;
				r.top = y * size;
				r.right = x * size + size;
				r.bottom = y * size + size;
				m.set(r);
				c.drawBitmap(mBitmap, rects[0], m, null);
			}
		}
		for(int x=0; x<mw; x++) {
			for(int y=0; y<mh; y++) {
				r.left = x * size;
				r.top = y * size;
				r.right = x * size + size;
				r.bottom = y * size + size;
				m.set(r);
				//c.drawBitmap(mBitmap, rects[0], m, null);
				if(map[x][y] == 1) {
					//System.err.println("cross at " + x + " " +y + " " + r + " " + m);
					c.drawLine(r.left, r.top, r.right, r.bottom, paintCross);
					c.drawLine(r.right, r.top, r.left, r.bottom, paintCross);
				} else if(map[x][y] == 2) {
					//stem.err.println("circle at " + x + " " +y + " " + r);
					c.drawCircle(r.left + size / 2, r.top + size/2, size/2, paintCircle);
				} 
			}
		}
		if(winRect != null) {
			c.drawLine(winRect.left * size + size / 2 , winRect.top * size + size / 2 ,
					winRect.right * size + size / 2, winRect.bottom * size + size / 2,   
					paintCircle);
		}
	}
	
	void init() {
		/*(new Thread() {
			public void run() {
				try{
					while(true) {
						Thread.sleep(1000);
						synchronized(GameView.this) { myRedraw(); }
					}
				} catch(Exception ex) {}
			}
		}).start();*/
		mBitmap = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(activity.getResources(), R.drawable.robotkill),
				240, 140, true);
		labelText = label.getText() + " ";
	}
	String labelText;
	int energy = 5;
	int level = 1;
	
	int size = 56;
	byte[][] map;
	int mw, mh;
	Bitmap mBitmap;
	Activity activity;
	TextView label;
	
	void myRedraw() {
		Canvas c = getHolder().lockCanvas();
		if(c != null) {
			onDraw(c);
			getHolder().unlockCanvasAndPost(c);
		}
	}

	Random rand = new Random();
	public void newGame() {
		winRect = null;
		if((label.getText() + "").startsWith("You")) {
			label.setText("New game.");
		}
		mw = getWidth() / size;
		mh = getHeight() / size;
		map = new byte[mw][mh];
		if(mw == 0 || mh == 0) {
			myRedraw();
			return;
		}
		myRedraw();
	}
	
	byte getAt(int x, int y){
		if(x < 0 || y < 0 || x >= mw || y >= mh) return UNREACHABLE;
		return map[x][y];
	}
	
}
