package kr.studiows.findkindergarten;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

public class Panel extends FrameLayout {
    private final int DIRECTION_DOWN = 1;
    private final int DIRECTION_UP = -1;

    private final int STATE_IDLE = 1;
    private final int STATE_DRAG = 2;
    private final int STATE_ANIMATION = 3;
    private volatile int VIEW_STATE = STATE_IDLE;

    private final int MODE_DEFAULT = 3;
    private final int MODE_DETAIL = 2;
    private final int MODE_FULLSCREEN = 1;

    private int PANEL_VIEW_MODE = MODE_DEFAULT;

    private float DEFAULT_MODE_Y_OFFSET;
    private float DETAIL_MODE_Y_OFFSET;
    private float FULLSCREEN_MODE_Y_OFFSET = 0f;

    private volatile boolean idle = true;   //TODO 리팩터 할것
    private volatile boolean isDraw = false;
    private volatile boolean aniRunning = false;

    private volatile boolean isDraggable = true;
    private volatile long GestureStartTime;

    //드래그 Y좌표
    private final float GESTURE_TRIGGER_VELOCITY = 1.3f;
    private volatile float yPos;    //터치 무브로 받아오는 Y좌표
    private volatile float Dy = 0f;
    private volatile float Sy = 0f;          //터치 다운 시작 Y좌표
    private volatile float pivotY = 0f;           //최종 계산된 패널 Y좌표

    //백그라운드 정보
    private float width;
    private float height;
    private float RoundTip = 25f;
    private final int COLOR_BACKGROUND = Color.WHITE;
    private Paint PanelStyle ;

    //오브젝트
    private LinearLayout container;
    private LinearLayout menuContainer;
    private LinearLayout filterContainer;

    private Thread slideAnimation;
    private Button b;

    public Panel(Context context) {
        super(context);
        PanelStyle = new Paint();
        PanelStyle.setShadowLayer(10f, 0, 0, Color.argb(100, 0, 0,0));
        PanelStyle.setStyle(Paint.Style.FILL);
        PanelStyle.setColor(COLOR_BACKGROUND);
        setLayerType(LAYER_TYPE_SOFTWARE, PanelStyle);
        init(context);
    }

    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.filter_panel,this, true);
        container = findViewById(R.id.panel_inner_container);
        menuContainer = findViewById(R.id.menu_container);
        filterContainer = findViewById(R.id.filter_container);

        b = findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d("Panel", "onClick: button");
                Log.d("Panel", "container h : " + container.getMeasuredHeight() + " menuContainer h : " + menuContainer.getMeasuredHeight());

            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        super.onLayout(changed, l, t, r, b);
        Log.d("Panel", "<onLayout> container h : " + container.getMeasuredHeight() + " menuContainer h : " + menuContainer.getMeasuredHeight());
        //@@@@@@@ 콜백 맨마지막~~~~~~~~~~~

        //기본 오프셋 값 설정
        DEFAULT_MODE_Y_OFFSET = container.getMeasuredHeight() - menuContainer.getMeasuredHeight();
        //TODO 값 나중에 화면 비율에 따라 수정할것
        DETAIL_MODE_Y_OFFSET = DEFAULT_MODE_Y_OFFSET - filterContainer.getMeasuredHeight();
        FULLSCREEN_MODE_Y_OFFSET = 0f;
        //패널 Y오프셋 설정
        setPanelY(MODE_DEFAULT);

    }

    public void setPanelY(int MODE){
        // 모든 작업의 맨 처음 또는 맨 끝에만 호출할것.
        isDraw = false;
        switch (MODE) {
            case MODE_DEFAULT:
                setY(DEFAULT_MODE_Y_OFFSET);
                break;

            case MODE_DETAIL:
                setY(DETAIL_MODE_Y_OFFSET);
                break;

            case MODE_FULLSCREEN:
                setY(FULLSCREEN_MODE_Y_OFFSET);
                break;
        }
        Log.d("Panel", "setY");
        invalidate();
    }

    public void changePanelState(int from, int to){
        // **** 초기 state idle
        // idle = T
        // isDraw = F
        // aniRunning = F
        switch(to){
            case STATE_IDLE:
                switch (from){
                    case STATE_DRAG:
                        idle = true; //
                        isDraw = false;
                        aniRunning = false;

                        container.setY(0f);
                        setPanelY(PANEL_VIEW_MODE);
                        pivotY -= getOffsetY(PANEL_VIEW_MODE);
                        Log.d("Panel", "!!  STATE CHANGED  !! DRAG --->> IDLE");
                        break;

                    case STATE_ANIMATION:
                        aniRunning = true;
                        setPanelY(PANEL_VIEW_MODE);
                        pivotY = 0f;
                        //(thisFunc)fft -> (dispatchDraw)tff
                        Log.d("Panel", "!!  STATE CHANGED  !! ANIMATION --->> IDLE");
                        break;
                }
                break;
            case STATE_DRAG:
                idle = false; // *
                setPanelY(MODE_FULLSCREEN);
                pivotY += getOffsetY(PANEL_VIEW_MODE); //Dy = 0 이므로
                //stete : ftf
                Log.d("Panel", "!!  STATE CHANGED  !! IDLE --->> DRAG");
                break;

            case STATE_ANIMATION:
                break;
        }
    }
    public void changePanelViewMode(int Direction){
        //성공시 true 실패시 false리턴

        // 1 : FULL_SCREEN
        // 2 : DETAIL
        // 3 : DEFAULT
        if(isPanelViewModeChangeable(Direction)){
            PANEL_VIEW_MODE += Direction;
            Log.d("Panel", "Panel View Mode <<<CHANGED>>> Mode : " + PANEL_VIEW_MODE);
        }else{
            Log.d("Panel", "Panel View Mode over!!!!!!! Mode : " + (PANEL_VIEW_MODE + Direction));
        }
    }

    public boolean isPanelViewModeChangeable(int Direction){
        if(PANEL_VIEW_MODE + Direction >= 1 && PANEL_VIEW_MODE + Direction <=3) return true;
        else return false;
    }

    public float getOffsetY(int MODE){
        switch (MODE){
            case MODE_DEFAULT:
                return DEFAULT_MODE_Y_OFFSET;

            case MODE_DETAIL:
                return DETAIL_MODE_Y_OFFSET;

            case MODE_FULLSCREEN:
                return FULLSCREEN_MODE_Y_OFFSET;
        }

        Log.d("Panel", "getOffsetY : PANEL_VIEW_MODE value is over the range");
        return -1000;
    }

    public void slideUpToShow(){
        Log.d("Panel", "slideUpToShow, height : " + container.getHeight());
        //setPanelY(container.getHeight());
        slideAnimation = new Thread(new SlideAnimation(container.getY(), 0f, DIRECTION_UP));
        slideAnimation.start();
    }
    public void slideDownToHide(){
        Log.d("Panel", "slideDownToHide, height : " + container.getHeight());
        //setPanelY(0f);
        slideAnimation = new Thread(new SlideAnimation(container.getY(), height, DIRECTION_DOWN));
        slideAnimation.start();
    }
    public void hidePanel(){
        ViewGroup panelContainer = (ViewGroup)getParent();
        panelContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        super.onInterceptTouchEvent(event);
        //TODO 로직 리팩터 할것.!!!!!!!!
        // 드래그 하는경우
        // (onInterceptTouch)DOWN -> MOVE ->(onTouch) MOVE -> UP
        // 단순 터치만 하는경우
        // (onInterceptTouch)DOWN -> MOVE -> UP
        // (onInterceptTouch)DOWN -> UP
        if(isDraggable){
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("Panel", "intercept ACTION_DONW");
                    Sy = event.getRawY();

                    //일반->드래그
                    changePanelState(STATE_IDLE, STATE_DRAG);

                    GestureStartTime = System.currentTimeMillis();
                    return false;

                case MotionEvent.ACTION_MOVE:
                    Log.d("Panel", "intercept ACTION_MOVE Y : " + event.getRawY() + ", Sy : " + Sy);
                    //드래그 감지될때만 onTouchEvent로 넘겨주기
                    if(Math.abs(event.getRawY() - Sy) > 10){
                        return true;
                    }else {
                        Log.d("Panel", "Action : just touched ");
                        return false;
                    }

                case MotionEvent.ACTION_UP:
                    Log.d("Panel", "intercept ACTION_UP Y : " + event.getRawY() + ", Sy : " + Sy);

                    //드래그 -> 일반
                    changePanelState(STATE_DRAG, STATE_IDLE);
                    return false;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        //Log.d("Panel", "onTouchEvent");
        if(isDraggable)
        {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    //호출 안되는 케이스
                    //Log.d("Panel", "ACTION_DONW");
                    break;

                case MotionEvent.ACTION_MOVE:
                    yPos = event.getRawY();

                    if(PANEL_VIEW_MODE == MODE_DEFAULT && (yPos - Sy > 0) || PANEL_VIEW_MODE == MODE_FULLSCREEN && (yPos - Sy) < 0){
                        Log.d("Panel", "slide down, up gesture is prohibited in Mode Default, Fullscreen");
                        //메뉴모드에서 아래로 드래그 || 풀스크린 모드에서 위로 드래그 하려고 하는경우 금지
                        Dy = 0f;
                        break;
                    }

                    Dy = yPos - Sy;
                    pivotY = getOffsetY(PANEL_VIEW_MODE) + Dy;
                    invalidate();

                    //Log.d("Panel", "RawY : " + yPos + ", Dy : " + Dy);
                    break;

                case MotionEvent.ACTION_UP:
                    isDraggable = false;
                    /*          UP   -T   DOWN   0    UP    T    DOWN
                            ----------|----------0----------|-----------> Velocity
                    */
                    long DeltaTime = (System.currentTimeMillis() - GestureStartTime);
                    float velocity = Dy / DeltaTime;
                    if(velocity == 0){
                        //Dy=0 --> velocity=0, 금지된 방향으로 스크롤업 한 경우
                        //드래그 -> 일반
                        changePanelState(STATE_DRAG, STATE_IDLE);
                        isDraggable = true;
                        break;
                    }

                    Log.d("Panel", "Dy : " + Dy + ", DeltaTime : " + DeltaTime + ", velocity : " + velocity);

                    int dir;
                    if(Math.abs(velocity) > GESTURE_TRIGGER_VELOCITY){
                        // v가 트리거 이상일때. 패널 모드전환 일어나는 상황
                        Log.d("Panel", "Gesture Trigger Activated!!");
                        if(velocity < 0 )
                            dir = DIRECTION_UP;     //위로
                        else
                            dir = DIRECTION_DOWN;   //아래로
                        //DEFALUT(1)에서 아래로 스와이프하거나 FULLSCREEN(3)에서 위로 스와이프 하는경우 뷰모드 체인지 거절
                        //TODO 밑에 로직 else 부분 호출 되는건지? 확인
                        if(isPanelViewModeChangeable(dir)){
                            changePanelViewMode(dir);
                        }else{
                            isDraggable = true;
                            break;
                        }
                    }else{
                        if(velocity < 0)
                            dir = DIRECTION_DOWN;   //위로 드래그했지만 다시 아래로 애니메이션
                        else
                            dir = DIRECTION_UP;     //아래로 드래그했지만 다시 위로 애니메이션
                    }
                    Log.d("Panel", "dir : " + dir);
                    slideAnimation = new Thread(new SlideAnimation(container.getY(), getOffsetY(PANEL_VIEW_MODE), dir));
                    slideAnimation.start();
                    break;
            }
        }else{
            Log.d("Panel", "Panel isn't Draggable");
        }
        return true;
    }

    @Override
    public synchronized void invalidate(){
        //Log.d("Panel", "invalidate");
        super.invalidate();
    }
    @Override
    protected synchronized void dispatchDraw(Canvas canvas){
        width = canvas.getWidth();
        height = canvas.getHeight();

        if(idle){
            //상대좌표
            Log.d("Panel", "draw <idle>! pivotY : " + pivotY + "/ offsetY : " + getY());
            container.setY(0f);
            canvas.drawRoundRect(10f,  10f, width - 10f, height+30f, RoundTip, RoundTip, PanelStyle);
            super.dispatchDraw(canvas);
            Log.d("Panel", "-------------------------------------------------------------------------------------------------");
        }else{
            if(isDraw){
                //드래그 & 애니메이션 드로잉 모드
                //절대좌표

                Log.d("Panel", "draw <isDraw True>! pivotY : " + pivotY + "/ offsetY : " + getY());
                //이전 Y 좌표에서 Dy 만큼 이동
                container.setY(pivotY);
                //백그라운드 드로잉
                canvas.drawRoundRect(10f, pivotY + 10f, width - 10f, height+30f, RoundTip, RoundTip, PanelStyle);
                super.dispatchDraw(canvas);
            }else{
                Paint p = new Paint();
                p.setStyle(Paint.Style.FILL);

                if(!aniRunning){//
                    Log.d("Panel", "draw < idle -> drag > !!!!!! container offsetY : " + getOffsetY(PANEL_VIEW_MODE));
                    //일반 -> 드래그
                    //상대좌표 -> 절대좌표
                    container.setY(getOffsetY(PANEL_VIEW_MODE));
                    canvas.drawRoundRect(10f, getOffsetY(PANEL_VIEW_MODE) + 10f, width - 10f, height+30f, RoundTip, RoundTip, PanelStyle);

                    isDraw = true;
                }else{
                    //애니메이션 -> 일반
                    //절대좌표 -> 상대좌표
                    Log.d("Panel", "draw < animation -> idle > !!!!!!  offsetY : " + getY());
                    container.setY(0f);
                    canvas.drawRoundRect(10f, 10f, width - 10f, height+30f, RoundTip, RoundTip, PanelStyle);
                    idle = true;
                    isDraw = false; //이미 false fft -> tff
                    aniRunning = false;
                    isDraggable = true;

                }

                super.dispatchDraw(canvas);
                invalidate();
            }
        }
    }

    class SlideAnimation implements Runnable{
        private final float FIRST_STRIDE = 0.21f;
        private final float PRODUCT_VALUE = 0.8f;

        private float Destination;
        private float start;
        private int Direction;
        private float Delta;
        private float DeltaInitialY;
        private float Dy = 0f;

        public SlideAnimation(){

        }
        public SlideAnimation(float From, float To, int Direction){
            this.Destination = To;
            this.Direction = Direction;
            //  1 아래로
            // -1 위로
            this.start = From;
            this.DeltaInitialY = Math.abs(To - From);
            this.Delta = Direction*(DeltaInitialY*FIRST_STRIDE);

            //this.Delta = Direction*Math.abs(Velocity)*20f;
        }
        public void setAnimation(float From, float To, int Direction){
            this.Destination = To;
            this.Direction = Direction;
            //  1 아래로
            // -1 위로
            this.start = From;
            this.DeltaInitialY = Math.abs(To - From);
            this.Delta = Direction*(DeltaInitialY*FIRST_STRIDE);

            //this.Delta = Direction*Math.abs(Velocity)*20f;
        }

        @Override
        public void run(){
            Log.d("Panel", "Animation Started, From : " + this.start + ", To : " + this.Destination + ", Delta : " + this.Delta + ",  DIRECTION : " + this.Direction);
            while(true){
                //Log.d("Panel", "Animation in Operate. pivotY : " + pivotY + "/ Delta : " + this.Delta);
                long AnimationStartTime = System.currentTimeMillis();

                if(this.Direction == DIRECTION_DOWN && pivotY >= this.Destination || this.Direction == DIRECTION_UP && pivotY <= this.Destination){
                    //아래로 or 위로
                    break;
                }
                this.Dy = this.Dy + this.Delta;
                pivotY = this.start + this.Dy;
                if(Math.abs(this.Delta) > 1)
                    this.Delta *= PRODUCT_VALUE;
                invalidate();

                //62.5Frame/Second
                long SleepTime = AnimationStartTime - System.currentTimeMillis() + 16; //16
                if (SleepTime > 0) {
                    try {
                        Thread.sleep(SleepTime);
                    }catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            //         <<----- 애니메이션 종료 ----->>
            Log.d("Panel", "Animation Finished, Delta : " + this.Delta);

            changePanelState(STATE_ANIMATION, STATE_IDLE);
        }
    }
}
/*

<LinearLayout
            android:id="@+id/menu_container"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_alignParentBottom="true"
            android:layout_marginVertical="5dp"
            android:gravity="center_horizontal">

            <Button
                android:id="@+id/change_map_center"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="현위치" />
            <Button
                android:id="@+id/search_filter"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="필터" />

            <Button
                android:id="@+id/favorite"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="즐겨찾기" />

            <Button
                android:id="@+id/toggle_view_type"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="리스트" />

        </LinearLayout>

                        if(!idle){
                            changePanelState(STATE_DRAG, STATE_IDLE);
                            /*
                            //TODO 중요!!
                            // ACTION_MOVE는 여러번 호출될 수 있기때문에
                            // 드래그 -> 일반 모드 변환이 한번만 호출되도록 하기위함
                            idle = true; //
                            isDraw = false;
                            aniRunning = false;

                            container.setY(0f);
                            setPanelY(PANEL_VIEW_MODE);
                            //Sy -= getOffsetY(PANEL_VIEW_MODE);
                            pivotY -= getOffsetY(PANEL_VIEW_MODE);
                        }
 */