package com.k2.mobile.app.controller.activity.menu.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.k2.mobile.app.R;
import com.k2.mobile.app.common.config.BroadcastNotice;
import com.k2.mobile.app.common.config.LocalConstants;
import com.k2.mobile.app.common.exception.HttpException;
import com.k2.mobile.app.controller.core.BaseActivity;
import com.k2.mobile.app.controller.core.BaseApp;
import com.k2.mobile.app.model.adapter.CalendarGridViewAdapter;
import com.k2.mobile.app.model.adapter.CreateCalendarAdapter;
import com.k2.mobile.app.model.bean.CalendarBean;
import com.k2.mobile.app.model.bean.PublicBean;
import com.k2.mobile.app.model.bean.ReqMessage;
import com.k2.mobile.app.model.bean.ResCalendarBean;
import com.k2.mobile.app.model.http.ResponseInfo;
import com.k2.mobile.app.model.http.callback.RequestCallBack;
import com.k2.mobile.app.model.http.other.SendRequest;
import com.k2.mobile.app.utils.CalendarUtil;
import com.k2.mobile.app.utils.DateUtil;
import com.k2.mobile.app.utils.DialogUtil;
import com.k2.mobile.app.utils.EncryptUtil;
import com.k2.mobile.app.utils.ErrorCodeContrast;
import com.k2.mobile.app.utils.FastJSONUtil;
import com.k2.mobile.app.utils.LogUtil;
import com.k2.mobile.app.utils.NetWorkUtil;
import com.k2.mobile.app.utils.NumberHelper;
import com.k2.mobile.app.view.widget.CalendarGridView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.content.Context;
import android.content.Intent;

/**
 * @ClassName: MyCalendarActivity
 * @Description: ????????????
 * @author linqijun
 * @date 2015-3-12 ??????8:56:46
 *
 */
public class MyCalendarActivity extends BaseActivity implements OnClickListener, OnTouchListener {

	// ????????????ID
    private static final int CAL_LAYOUT_ID = 55;
    //???????????????
    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 120;
    
    // ??????
    private Animation slideLeftIn;
    private Animation slideLeftOut;
    private Animation slideRightIn;
    private Animation slideRightOut;
    private ViewFlipper viewFlipper;
    
    GestureDetector mGesture = null;
    // ????????????
    private Button mTodayBtn;
    // ?????????????????????
    private TextView tv_month;
 // ?????????????????????
    private TextView tv_year;
    // ?????????????????????View
    private RelativeLayout mCalendarMainLayout;
    // ????????????
    private Context mContext = MyCalendarActivity.this;
    // ????????????View
    private GridView firstGridView;
    // ?????????View
    private GridView currentGridView;
    // ????????????View
    private GridView lastGridView;
    // ?????????????????????
    private Calendar calStartDate = Calendar.getInstance();
    // ???????????????
    private Calendar calSelected = Calendar.getInstance();
    // ???????????????????????????
    private Calendar calDay = Calendar.getInstance();
    // ??????
    private Calendar calToday = Calendar.getInstance();
    // ??????????????????????????????
    private CalendarGridViewAdapter currentGridAdapter;
    // ???????????????????????????????????????
    private CalendarGridViewAdapter firstGridAdapter;
    // ???????????????????????????????????????
    private CalendarGridViewAdapter lastGridAdapter;
    //???????????????
    private int mMonth = 0;
    // ???????????????
    private int mYear = 0;
    // ?????????
    private int iFirstDayOfWeek = Calendar.MONDAY;
    // ??????????????????
    private int daysPerMonth = 0;
    // ?????????
    private String week;
	// ??????
	private Button btn_back;
	// ??????
	private Button btn_schedule;
	// ????????????
	private TextView tv_title_My_Calendar;
	// ??????
	private TextView tv_search;
	// ????????????
	private ListView lv_work_calendar;
	// ?????????
	private CreateCalendarAdapter ccAdapter;
	
	// ???????????????????????????
	private List<ResCalendarBean> calendarBean = null;
	private List<CalendarBean> calBean = null;

	private Handler mHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
				case 1:
					calBean.clear();
					daysPerMonth = CalendarUtil.DAYPERMONTH[mMonth];
					String decode = (String) msg.obj;
					if (null == decode) {
				        calBean = new ArrayList<CalendarBean>();
				        for(int i=0; i<daysPerMonth; i++){
				        	int day = i+1;
							CalendarBean cBean = getBean(day, null);
							calBean.add(cBean);
				        }
					}else{
						// ??????JSON
						calendarBean = JSON.parseArray(decode, ResCalendarBean.class);
						for(int i=0; i<daysPerMonth; i++){
							// ???
							int day = i+1;
							List<ResCalendarBean> resBean = new ArrayList<ResCalendarBean>();
							// ?????????????????????
							String nowDay = mYear + "-" + (mMonth+1) + "-"+ day + " 01:01:01";
							if(calendarBean != null){
								for(int j=0;j<calendarBean.size();j++){
									String startTime = calendarBean.get(j).getStartTime().replaceAll("T", " ");
									// ??????????????????????????????
									if(DateUtil.timeComparison(nowDay, startTime)){
										resBean.add(calendarBean.get(j));
									}
								}
							}
							
							if(resBean.size() < 1){
								resBean = null;
							}
							
							CalendarBean cBean = getBean(day, resBean);
							calBean.add(cBean);
						}
					}
					ccAdapter.notifyDataSetChanged();
					break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_menu_workbench_my_calendar);
		initView();
		initListener();
		BaseApp.addActivity(this);
		updateStartDateForMonth();
		
		generateContetView(mCalendarMainLayout);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);

        slideLeftIn.setAnimationListener(animationListener);
        slideLeftOut.setAnimationListener(animationListener);
        slideRightIn.setAnimationListener(animationListener);
        slideRightOut.setAnimationListener(animationListener);

        mGesture = new GestureDetector(this, new GestureListener());
        
        initAdapter();
        // ??????????????????
		String nowMonth = calStartDate.get(Calendar.YEAR)+ "-" + NumberHelper.LeftPad_Tow_Zero(calStartDate.get(Calendar.MONTH) + 1);
        String calendarInfo = getCalendar(nowMonth);
        operCalendar(calendarInfo);
        // ????????????????????????
        if((mMonth == calSelected.get(Calendar.MONTH)) && (mYear == calSelected.get(Calendar.YEAR))){
        	lv_work_calendar.setSelection(calSelected.get(Calendar.DAY_OF_MONTH) - 1);
        }
	}

	/**
	 * ?????????: initView() 
	 * ??? ??? : ????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initView() {		
		tv_title_My_Calendar = (TextView) findViewById(R.id.tv_title);
		tv_search = (TextView) findViewById(R.id.tv_sech);
		btn_back = (Button) findViewById(R.id.btn_back);
		lv_work_calendar = (ListView) findViewById(R.id.lv_work_calendar);
		btn_schedule = (Button) findViewById(R.id.btn_schedule);
		
		mTodayBtn = (Button) findViewById(R.id.btn_today);
        tv_month = (TextView) findViewById(R.id.tv_message);
        tv_year = (TextView) findViewById(R.id.tv_year);
        mCalendarMainLayout = (RelativeLayout) findViewById(R.id.calendar_main);

        tv_search.setVisibility(View.GONE);
        tv_title_My_Calendar.setText(getString(R.string.my_calendar));
	}
	/**
	 * ?????????: getMonthViewCurrent() 
	 * ??? ??? : ????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initAdapter() {
		// ????????????????????????
		daysPerMonth = CalendarUtil.DAYPERMONTH[calStartDate.get(Calendar.MONTH)];
		
        calBean = new ArrayList<CalendarBean>();
        
        for(int i=0; i<daysPerMonth; i++){
        	int day = i+1;
			CalendarBean cBean = getBean(day, null);
			calBean.add(cBean);
        }
        
        ccAdapter = new CreateCalendarAdapter(this, calBean);
        lv_work_calendar.setAdapter(ccAdapter);
	}
	
	/**
	 * ?????????: getBean() 
	 * ??? ??? : ???????????????
	 * ??? ??? : void 
	 * ?????????: CalendarBean ?????????
	 */
	private CalendarBean getBean(int day, List<ResCalendarBean> resBean){
		CalendarBean cBean = new CalendarBean();
		
		setDay(mYear, mMonth, day);
		
		cBean.setYear(mYear+"");
		cBean.setMonth((mMonth+1)+"");
		cBean.setDay(day + "");
		cBean.setWeek(week);
		if (null != resBean) {
			cBean.setResBean(resBean);
		}
		
		return cBean;
	}

	/**
	 * ?????????: initListener() 
	 * ??? ??? : ????????? ????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initListener() {
		btn_back.setOnClickListener(this);
		btn_schedule.setOnClickListener(this);
		mTodayBtn.setOnClickListener(this);
//        mPreMonthImg.setOnClickListener(this);
//        mNextMonthImg.setOnClickListener(this);
	}
	/**
     * ???????????????????????????????????????View
     *
     * @param layout ??????????????????????????????
     */
    private void generateContetView(RelativeLayout layout) {
        // ???????????????????????????????????????????????????
        viewFlipper = new ViewFlipper(this);
        viewFlipper.setId(CAL_LAYOUT_ID);
        calStartDate = getCalendarStartDate();
        createGirdView();
        RelativeLayout.LayoutParams params_cal = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.addView(viewFlipper, params_cal);

        LinearLayout br = new LinearLayout(this);
        RelativeLayout.LayoutParams params_br = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, 1);
        params_br.addRule(RelativeLayout.BELOW, CAL_LAYOUT_ID);
//        br.setBackgroundColor(getResources().getColor(R.color.calendar_background));
        layout.addView(br, params_br);
    }
    /**
	 * @Title: operCalendar
	 * @Description: ???????????????????????????
	 * @return void ??????????????? 
	 * @throws
	 */
    private void operCalendar(String calendarInfo){
    	
    	if (!NetWorkUtil.isNetworkAvailable(this)) {
 			DialogUtil.showLongToast(MyCalendarActivity.this, R.string.global_network_disconnected);
 			return;
 		} 
    			
		if (null != calendarInfo) {
			SendRequest.sendSubmitRequest(MyCalendarActivity.this, calendarInfo, BaseApp.token, BaseApp.reqLang, 
					LocalConstants.MY_CALENDAR_QUERY_SERVER, BaseApp.key, submitCallBack);
		}
    }
    
    /**
	 * @Title: getCalendar
	 * @Description: ????????????
	 * @param nowMonth ??????
	 * @return String ??????????????? 
	 * @throws
	 */
	private String getCalendar(String nowMonth){
		
		PublicBean bean = new PublicBean();
		bean.setDeviceId(BaseApp.macAddr);
		bean.setUserAccount(BaseApp.user.getUserId());
		bean.setMonth(nowMonth);
		
		return JSON.toJSONString(bean);
	}
	
	/**
	 * http????????????
	 */
	RequestCallBack<String> submitCallBack = new RequestCallBack<String>() {

		@Override
		public void onStart() {
			DialogUtil.showWithCancelProgressDialog(MyCalendarActivity.this, null, res.getString(R.string.global_prompt_message), null);
		}

		@Override
		public void onLoading(long total, long current, boolean isUploading) {

		}
		
		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {                                     
			String result = responseInfo.result.toString();
			if (null != result && !"".equals(result.trim())) {
				ReqMessage msg = FastJSONUtil.getJSONToEntity(result, ReqMessage.class);
				// ????????????????????????????????????
				if (null == msg || null == msg.getResCode() || "".equals(msg.getResCode())) {				
					LogUtil.promptInfo(MyCalendarActivity.this, ErrorCodeContrast.getErrorCode("0", res));
					return;
				// ???????????????
				}else if ("1103".equals(msg.getResCode()) || "1104".equals(msg.getResCode())) {
					LogUtil.promptInfo(MyCalendarActivity.this, ErrorCodeContrast.getErrorCode(msg.getResCode(), res));	
					Intent mIntent = new Intent(BroadcastNotice.USER_EXIT);
					sendBroadcast(mIntent);
					return;
				}else if("1210".equals(msg.getResCode())){
					LogUtil.promptInfo(MyCalendarActivity.this, ErrorCodeContrast.getErrorCode(msg.getResCode(), res));	
					Intent mIntent = new Intent(BroadcastNotice.WIPE_USET);
					sendBroadcast(mIntent);
					return;
				}else if (!"1".equals(msg.getResCode())) {
					LogUtil.promptInfo(MyCalendarActivity.this, ErrorCodeContrast.getErrorCode(msg.getResCode(), res));	
					return;
				}
				// ???????????????????????????
				if (null == msg.getMessage() || "".equals(msg.getMessage().trim())) {		 
					LogUtil.promptInfo(MyCalendarActivity.this, ErrorCodeContrast.getErrorCode("0", res));
				} else {
					// ?????????????????????????????????
					Message msgs = new Message();
					String decode = EncryptUtil.getDecodeData(msg.getMessage(), BaseApp.key);
					msgs.obj = decode;
					msgs.what = 1;
					mHandler.sendMessage(msgs);
				}
			} else {
				LogUtil.promptInfo(MyCalendarActivity.this, ErrorCodeContrast.getErrorCode("0", res));
			}
			
			DialogUtil.closeDialog();
		}

		@Override
		public void onFailure(HttpException error, String msg) {
			DialogUtil.closeDialog();
			if(null != msg && msg.equals(LocalConstants.API_KEY)){
				LogUtil.promptInfo(MyCalendarActivity.this, ErrorCodeContrast.getErrorCode("1207", res));
			}else{
				LogUtil.promptInfo(MyCalendarActivity.this, ErrorCodeContrast.getErrorCode("0", res));
			}
		}
	};	
    
	/**
     * ???????????????????????????????????????View
     */
    private void createGirdView() {

        Calendar firstCalendar = Calendar.getInstance(); 	 
        Calendar currentCalendar = Calendar.getInstance();  
        Calendar lastCalendar = Calendar.getInstance(); 
        
        firstCalendar.setTime(calStartDate.getTime());
        currentCalendar.setTime(calStartDate.getTime());
        lastCalendar.setTime(calStartDate.getTime());

        firstGridView = new CalendarGridView(mContext);
        firstCalendar.add(Calendar.MONTH, -1);
        firstGridAdapter = new CalendarGridViewAdapter(this, firstCalendar);
        firstGridView.setAdapter(firstGridAdapter);// ????????????Adapter
        firstGridView.setId(CAL_LAYOUT_ID);

        currentGridView = new CalendarGridView(mContext);
        currentGridAdapter = new CalendarGridViewAdapter(this, currentCalendar);
        currentGridView.setAdapter(currentGridAdapter);// ????????????Adapter
        currentGridView.setId(CAL_LAYOUT_ID);

        lastGridView = new CalendarGridView(mContext);
        lastCalendar.add(Calendar.MONTH, 1);
        lastGridAdapter = new CalendarGridViewAdapter(this, lastCalendar);
        lastGridView.setAdapter(lastGridAdapter);// ????????????Adapter
        lastGridView.setId(CAL_LAYOUT_ID);

        currentGridView.setOnTouchListener(this);
        firstGridView.setOnTouchListener(this);
        lastGridView.setOnTouchListener(this);

        if (viewFlipper.getChildCount() != 0) {
            viewFlipper.removeAllViews();
        }

        viewFlipper.addView(currentGridView);
        viewFlipper.addView(lastGridView);
        viewFlipper.addView(firstGridView);

        tv_month.setText(NumberHelper.LeftPad_Tow_Zero(calStartDate.get(Calendar.MONTH) + 1)+"???");
        tv_year.setText(calStartDate.get(Calendar.YEAR)+getString(R.string.year));
    }
    /**
     * ????????????
     */
    private void setPrevViewItem() {
        mMonth--;// ???????????????--
        // ?????????????????????????????????????????????
        if (mMonth == -1) {
            mMonth = 11;
            mYear--;
        }
        calStartDate.set(Calendar.DAY_OF_MONTH, 1); 	// ??????????????????1???
        calStartDate.set(Calendar.MONTH, mMonth); 		// ?????????
        calStartDate.set(Calendar.YEAR, mYear); 		// ?????????
        String month = "";
        if((mMonth+1) < 10){
        	month = "0" + (mMonth+1);
        }else{
        	month = "" + (mMonth+1);
        }
        
        String calendarInfo = getCalendar(mYear + "-" + month);
        operCalendar(calendarInfo);
    }

    /**
     * ????????????
     */
    private void setNextViewItem() {
        mMonth++;
        if (mMonth == 12) {
            mMonth = 0;
            mYear++;
        }
        calStartDate.set(Calendar.DAY_OF_MONTH, 1);
        calStartDate.set(Calendar.MONTH, mMonth);
        calStartDate.set(Calendar.YEAR, mYear);
        
        String month = "";
        if((mMonth+1) < 10){
        	month = "0" + (mMonth+1);
        }else{
        	month = "" + (mMonth+1);
        }
        
        String calendarInfo = getCalendar(mYear + "-" + month);
        operCalendar(calendarInfo);
    }
    /**
     * ??????????????????
     * */
    private void setDay(int year, int month, int day){
    	
    	calDay.set(Calendar.YEAR, year);	
    	calDay.set(Calendar.MONTH, month);	
    	calDay.set(Calendar.DATE, day);		
     	week = CalendarUtil.getWeek(calDay);     	
    }
    
	/**
     * ?????????????????????????????????
     * ?????????????????????
     */
    private void updateStartDateForMonth() {
        calStartDate.set(Calendar.DATE, 1); 		// ????????????????????????
        mMonth = calStartDate.get(Calendar.MONTH);	// ??????????????????????????????
        mYear = calStartDate.get(Calendar.YEAR);	// ??????????????????????????????

        tv_month.setText(NumberHelper.LeftPad_Tow_Zero(calStartDate.get(Calendar.MONTH) + 1)+"???");
        tv_year.setText(calStartDate.get(Calendar.YEAR)+getString(R.string.year));
        // ????????????2 ????????????1 ??????????????????
        int iDay = 0;
        int iFirstDayOfWeek = Calendar.MONDAY;
        int iStartDay = iFirstDayOfWeek;
        if (iStartDay == Calendar.MONDAY) {
            iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
            if (iDay < 0)
                iDay = 6;
        }
        if (iStartDay == Calendar.SUNDAY) {
            iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
            if (iDay < 0)
                iDay = 6;
        }
        
        calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);
    }
    /**
     * ???????????????????????????????????????
     *
     * @return ???????????????????????????
     */
    private Calendar getCalendarStartDate() {
        calToday.setTimeInMillis(System.currentTimeMillis());
        calToday.setFirstDayOfWeek(iFirstDayOfWeek);

        if (calSelected.getTimeInMillis() == 0) {
            calStartDate.setTimeInMillis(System.currentTimeMillis());
            calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
        } else {
            calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
            calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
        }

        return calStartDate;
    }

    AnimationListener animationListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            //????????????????????????
            createGirdView();
        }
    };

    class GestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	setNextViewItem();
                	viewFlipper.setInAnimation(slideLeftIn);
                    viewFlipper.setOutAnimation(slideLeftOut);
                    viewFlipper.showNext();
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	setPrevViewItem();
                	viewFlipper.setInAnimation(slideRightIn);
                    viewFlipper.setOutAnimation(slideRightOut);
                    viewFlipper.showPrevious();
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //??????????????????????????????????????????
            int pos = currentGridView.pointToPosition((int) e.getX(), (int) e.getY());
            LinearLayout txtDay = (LinearLayout) currentGridView.findViewById(pos + 5000);
            if (txtDay != null) {
                if (txtDay.getTag() != null) {
                    Date date = (Date) txtDay.getTag();
                    calSelected.setTime(date);
                    currentGridAdapter.setSelectedDate(calSelected);
                    currentGridAdapter.notifyDataSetChanged();
                    firstGridAdapter.setSelectedDate(calSelected);
                    firstGridAdapter.notifyDataSetChanged();

                    lastGridAdapter.setSelectedDate(calSelected);
                    lastGridAdapter.notifyDataSetChanged();
                    
                    if((mMonth == calSelected.get(Calendar.MONTH)) && (mYear == calSelected.get(Calendar.YEAR))){
                    	lv_work_calendar.setSelection(calSelected.get(Calendar.DAY_OF_MONTH) - 1);
                    }
//                    String week = CalendarUtil.getWeek(calSelected);
//                    String message = CalendarUtil.getDay(calSelected) + " ??????" + new CalendarUtil(calSelected).getDay() + " " + week;
//                    Toast.makeText(getApplicationContext(), "?????????????????????:" + message+", day = "+calSelected.get(Calendar.DAY_OF_MONTH), Toast.LENGTH_SHORT).show();
                }
            }

            return false;
        }
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				this.finish();
				break;
			case R.id.btn_today:
				// ???????????????????????????????????????
				calStartDate = Calendar.getInstance();
	            calSelected = Calendar.getInstance();
	            updateStartDateForMonth();
	            generateContetView(mCalendarMainLayout);
	            // ??????????????????
	    		String nowMonth = calStartDate.get(Calendar.YEAR)+ "-" + NumberHelper.LeftPad_Tow_Zero(calStartDate.get(Calendar.MONTH) + 1);
	            String calendarInfo = getCalendar(nowMonth);
	            operCalendar(calendarInfo);
	            
	            if((mMonth == calSelected.get(Calendar.MONTH)) && (mYear == calSelected.get(Calendar.YEAR))){
                	lv_work_calendar.setSelection(calSelected.get(Calendar.DAY_OF_MONTH) - 1);
                }
	            
				break;
			case R.id.btn_schedule:
				Intent mIntent = new Intent(MyCalendarActivity.this, CalendarListActivity.class);
				startActivityForResult(mIntent, 1021);
				break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mGesture.onTouchEvent(event);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(requestCode == 1020 && resultCode == 1020){
			// ??????????????????
			String nowMonth = calStartDate.get(Calendar.YEAR)+ "-" + NumberHelper.LeftPad_Tow_Zero(calStartDate.get(Calendar.MONTH) + 1);
	        String calendarInfo = getCalendar(nowMonth);
	        operCalendar(calendarInfo);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}