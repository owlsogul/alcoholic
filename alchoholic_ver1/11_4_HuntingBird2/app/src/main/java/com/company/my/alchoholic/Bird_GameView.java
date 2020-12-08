package com.company.my.alchoholic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.company.my.alchoholic.sensor.ButtonCallback;
import com.company.my.alchoholic.sensor.Sensor;
import com.company.my.alchoholic.sensor.SensorInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Bird_GameView extends View {
    private Context context;      // Context 보존용
    private GameThread mThread;   // Thread

    // 배경과 화면 크기
    private Bitmap imgBack;
    private int w, h;

    // 배경음악, 효과음, 사운드 id
    private MediaPlayer mPlayer;
    private SoundPool mSound;
    private int soundId;

    // 점수 표시용
    private int hit = 0;
    private int miss = 0;

    // 참새 생성 시간과 Paint
    private float makeTimer = 0;
    private Paint paint = new Paint();

    // 참새
    private List<Bird_Sparrow> mSparrow;
    //sqlite
    myDBAdapter dbAdapter;
    //센서
    final Sensor sensor = SensorInstance.getInstance();

    private ButtonCallback bc6, bc7, bc8;

    //--------------------------------
    // 생성자
    //---------------------------------
    public Bird_GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        // 참새
        mSparrow = Collections.synchronizedList( new ArrayList<Bird_Sparrow>() );

        // 배경 음악
        mPlayer = MediaPlayer.create(context, R.raw.rondo);
        mPlayer.setLooping(true);
        mPlayer.start();
        

       // sensor.readySensor();

        dbAdapter = new myDBAdapter(context);

        bc6 = new ButtonCallback() {
            @Override
            public void onButtonClick(int value) {
                if (value==1){System.out.println("6 번 클릭");
                    sensor.startAnimatedDot(1);
                    fireBullet( 500, 100 );}   // 발사
            }
        };

        bc7 = new ButtonCallback() {
            @Override
            public void onButtonClick(int value) {
                if (value==1){System.out.println("7 번 클릭");
                    sensor.startAnimatedDot(1);
                    fireBullet( 300, 100 );}   // 발사
            }
        };

        bc8 = new ButtonCallback() {
            @Override
            public void onButtonClick(int value) {
                if (value==1) {
                    System.out.println("8 번 클릭" + value);
                    sensor.startAnimatedDot(1);
                    fireBullet(100, 100);
                }// 발사
            }
        };

        sensor.registerButtonCallback(6, bc6);

        sensor.registerButtonCallback(7, bc7);

        sensor.registerButtonCallback(8, bc8);

        // 롤리팝 이전 버전인가?
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mSound = new SoundPool(5, AudioManager.STREAM_MUSIC, 1);
        } else {
            AudioAttributes attributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

            mSound = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(5).build();
        }

        // Sound 파일 읽기
        soundId = mSound.load(context, R.raw.fire, 1);

        // 점수의 글자 크기와 색
        paint.setTextSize(60);
        paint.setColor(Color.WHITE);

        // 참새 이미지 분리
        Bird_CommonResources.set(context);
    }

    //--------------------------------
    // View의 해상도 구하기
    //---------------------------------
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        this.w = w;        // 화면의 폭과 높이
        this.h = h;

        // 배경 이미지 확대
        imgBack = BitmapFactory.decodeResource(getResources(), R.drawable.back);
        imgBack = Bitmap.createScaledBitmap(imgBack, w, h, true);

        // 스레드 기동
        if (mThread == null) {
            mThread = new GameThread();
            mThread.start();
        }
    }

    //--------------------------------
    // View의 종료
    //---------------------------------
    @Override
    protected void onDetachedFromWindow() {
        mThread.canRun = false;
        super.onDetachedFromWindow();
    }

    //--------------------------------
    // 화면 그리기
    //---------------------------------
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(imgBack, 0, 0, null);

        // 참새 그리기
        synchronized (mSparrow) {
            for (Bird_Sparrow tmp : mSparrow) {
                canvas.rotate(tmp.ang, tmp.x, tmp.y);
                canvas.drawBitmap(tmp.bird, tmp.x - tmp.w, tmp.y - tmp.w, null);
                canvas.rotate(-tmp.ang, tmp.x, tmp.y);
            }
        }

        // 점수 출력
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("score : " + hit, 100, 100, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Miss : " + miss, w - 100, 100, paint);
    }

    //--------------------------------
    // 참새 만들기
    //---------------------------------
    private void makeSparrow() {
        makeTimer -= Time.deltaTime;

        if (makeTimer <= 0) {
            makeTimer = 0.5f;
            synchronized (mSparrow) {
                mSparrow.add( new Bird_Sparrow(w, h) );
            }
        }
    }

    //--------------------------------
    // 참새 이동
    //---------------------------------
    private void moveSparrow() {
        synchronized (mSparrow) {
            for (Bird_Sparrow tmp : mSparrow) {
                tmp.update();
            }
        }
    }

    //--------------------------------
    // 사망한 참새 제거
    //---------------------------------
    private void removeDead() {
        synchronized (mSparrow) {
            for (int i = mSparrow.size() - 1; i >= 0; i--) {
                if (mSparrow.get(i).isDead) {
                    mSparrow.remove(i);
                }
            }
        }
    }

    //--------------------------------
    // 총알 발사 <-- TouchEvent
    //---------------------------------
    private void fireBullet (float x, float y) {
        //터치된 포인트 가져와서 그 포인트에 해당하는 참새 kill
        //버튼 클릭시 그 버튼에 해당하는 넓이 가져오고 해당 영역에 있는 참새 kill
        boolean isHit = false;
        mSound.play(soundId, 1, 1, 1, 0, 1);

        for ( Bird_Sparrow tmp : mSparrow ) {
            if (tmp.hitTest(x, y)) {
                isHit = true;
                break;
            }
        }

        hit = isHit ? hit + 100 : hit;
        miss = isHit ? miss : miss + 1;
        //여기다가 7segment 추가하여 score 변동할때마다 출력하기!
        System.out.println(hit+"수만큼 hit");
        sensor.show7Seg(hit);

        //점수 db에 저장
        dbAdapter.open();
        dbAdapter.clear();
        //sqlDB.execSQL("INSERT INTO groupTBL VALUES ('" + user_id + "');");
        if(hit>=1000){
            dbAdapter.insert("1");
            System.out.println("1 접근"); }
        else{dbAdapter.insert("0");
            System.out.println("0 접근"); }
        dbAdapter.close();
    }

    //--------------------------------
    // 게임 초기화 <-- MainActivity
    //--------------------------------
    public void initGame() {
        synchronized (mSparrow) {
            mSparrow.clear();
        }

        hit = miss = 0;
        invalidate();
    }

    //--------------------------------
    // Touch Event
    //---------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            fireBullet( event.getX(), event.getY() );   // 발사
        }
        return true;
    }

    /**
     * 게임 끝났을 때 호출되는 메소드
     * @author jomingyu
     */
    public void gameDone() {
        sensor.unregisterButtonCallback(6, bc6);
        sensor.unregisterButtonCallback(7, bc7);
        sensor.unregisterButtonCallback(8, bc8);
    }

    //-----------------------------
    // Thread
    //-----------------------------
    class GameThread extends Thread {
        public boolean canRun = true;

        @Override
        public void run() {
            while (canRun) {
                try {
                    Time.update();      // deltaTime 계산

                    makeSparrow();       // 참새 만들기
                    moveSparrow();       // 이동
                    removeDead();        // 사망한 참새 제거
                    postInvalidate();   // 화면 그리기
                    sleep(10);
                } catch (Exception e) {
                    //
                }

            }
        }
    } // Thread

} // GameView
