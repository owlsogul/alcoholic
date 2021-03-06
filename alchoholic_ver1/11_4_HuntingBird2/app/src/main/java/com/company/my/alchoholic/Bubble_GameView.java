package com.company.my.alchoholic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.company.my.alchoholic.sensor.Sensor;
import com.company.my.alchoholic.sensor.SensorInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

//-----------------------------
// GameView
//-----------------------------
public class Bubble_GameView extends View {
    private Context context;    // Context 저장용
    public GameThread mThread;

    // 배경과 화면 크기
    private Bitmap imgBack;
    private int w, h;

    //점수
    private int score = 0;

    //타이머
    private int inputNumber = 10;
    // Random.
    private Random rnd = new Random();
    private Paint paint = new Paint();

    // 비눗방울, 파편
    private List<Bubble> mBubble = Collections.synchronizedList( new ArrayList<Bubble>() );
    static public List<Bubble_SmallBubble> mSmall = Collections.synchronizedList( new ArrayList<Bubble_SmallBubble>() );
    //sqlite
    myDBAdapter dbAdapter;
    //센서
    final Sensor sensor = SensorInstance.getInstance();
    //-----------------------------
    // 생성자
    //-----------------------------
    public Bubble_GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Context 저장
        this.context = context;
        dbAdapter = new myDBAdapter(context);

        dbAdapter.open();
        dbAdapter.clear();
        dbAdapter.insert("0");//첫 시작에서만 0
        dbAdapter.close();

        //타이머
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                /**
                 * 넘겨받은 what값을 이용해 실행할 작업을 분류합니다
                 */
                if(msg.what==1){
                    Log.d("What Number : ", "What is 1");
                }else if(msg.what==2){
                    Log.d("What Number : ", "What is 2");
                }
            }
        };
        /*
        Runnable task = new Runnable(){
            public void run(){
                while(inputNumber > 0){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}

                    --inputNumber;

                    handler.sendEmptyMessage(1);


                    Message message= Message.obtain();
                    message.what = 2;
                    handler.sendMessage(message);
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
        */

        // 점수의 글자 크기와 색
      //  paint.setTextSize(60);
      //  paint.setColor(Color.WHITE);
    }

    //-----------------------------
    // View의 크기 구하기
    //-----------------------------
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 화면의 폭과 높이
        this.w = w;
        this.h = h;

        // 배경 이미지
        imgBack = BitmapFactory.decodeResource(getResources(), R.drawable.sky);
        imgBack = Bitmap.createScaledBitmap(imgBack, w, h, true);

        // 스레드 기동
        if (mThread == null) {
            mThread = new GameThread();
            mThread.start();
        }
    }

    //-----------------------------
    // View의 종료
    //-----------------------------
    @Override
    protected void onDetachedFromWindow() {
        mThread.canRun = false;
        super.onDetachedFromWindow();
    }

    //-----------------------------
    // 화면 그리기 - 동기화
    //-----------------------------
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(imgBack, 0, 0, null);

        // 비눗방울 그리기
        synchronized (mBubble) {
            for (Bubble tmp : mBubble) {
                canvas.drawBitmap(tmp.bubble, tmp.x - tmp.r, tmp.y - tmp.r, null);
            }
        }

        // 비눗방울 파편
        synchronized (mSmall) {
            for (Bubble_SmallBubble tmp : mSmall) {
                paint.setAlpha(tmp.alpha);
                canvas.drawBitmap(tmp.bubble, tmp.x - tmp.r, tmp.y - tmp.r, paint);
            }
        }
   //     paint.setTextAlign(Paint.Align.LEFT);
    //    canvas.drawText("남은 시간 : " + inputNumber, w / 2, 80, paint);
    }

    //-----------------------------
    // 비눗방울 만들기 - 동기화
    //-----------------------------
    private void makeBubble() {
        synchronized (mBubble) {
            if (mBubble.size() < 20 && rnd.nextInt(400) < 8) {//비눗방울 나오는 속도 조절
                mBubble.add(new Bubble(context, w, h));
            }
        }
    }

    //-----------------------------
    // 이동 - 동기화
    //-----------------------------
    private void moveBubble() {
        // 비눗방울
        synchronized (mBubble) {
            for (Bubble tmp : mBubble) {
                tmp.update();
            }
        }

        // 비눗방울 파편
        synchronized (mSmall) {
            for (Bubble_SmallBubble tmp : mSmall) {
                tmp.update();
            }
        }
    }

    //-----------------------------
    // 수명이 끝난 오브젝트 제거 - 동기화
    //-----------------------------
    private void removeDead() {
      //  dbAdapter = new myDBAdapter(this);
        // 풍선
        synchronized (mBubble) {
            for (int i = mBubble.size() - 1; i >= 0; i--) {
                if (mBubble.get(i).isDead) {
                    mBubble.remove(i);
                    score +=100;
                    //여기다가 7segment 추가하여 score 변동할때마다 출력하기!
                    System.out.println(score);
                    sensor.show7Seg(score);

                    //sqlDB.execSQL("INSERT INTO groupTBL VALUES ('" + user_id + "');");
                    if(score==2500){
                        //점수 db에 저장
                        dbAdapter.open();
                        dbAdapter.clear();
                    dbAdapter.insert("1");
                        dbAdapter.close();}
                    else{}

                }
            }
        }

        // 파편
        synchronized (mSmall) {
            for (int i = mSmall.size() - 1; i >= 0; i--) {
                if (mSmall.get(i).isDead) {
                    mSmall.remove(i);
                }
            }
        }
    }

    //-----------------------------
    // Hit Test <-- Touch Event
    //-----------------------------
    private synchronized void hitTest(float x, float y) {
        for (Bubble tmp : mBubble) {
            if ( tmp.hitTest(x, y) ) break;
        }
    }

    //-----------------------------
    // Touch Event
    //-----------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            hitTest(event.getX(), event.getY());
        }

        return true;
    }

    //-----------------------------
    // Thread
    //-----------------------------
    class GameThread extends Thread {
        public volatile boolean canRun = true;
        @Override
        public void run() {
            while (canRun) {
                try {
                    Time.update();      // deltaTime 계산

                    makeBubble();
                    moveBubble();
                    removeDead();
                    postInvalidate();   // 화면 그리기
                    sleep(10);
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }
    } // Thread

} // GameView
