package com.k2.mobile.app.controller.activity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.k2.mobile.app.R;
import com.k2.mobile.app.common.config.BroadcastNotice;
import com.k2.mobile.app.common.config.LocalConstants;
import com.k2.mobile.app.common.exception.HttpException;
import com.k2.mobile.app.controller.activity.fragment.ApplicationFragment;
import com.k2.mobile.app.controller.activity.fragment.ApprovalFragment;
import com.k2.mobile.app.controller.activity.fragment.MyFragment;
import com.k2.mobile.app.controller.activity.login.LoginActivity;
import com.k2.mobile.app.controller.core.BaseApp;
import com.k2.mobile.app.controller.server.BackManagementServer;
import com.k2.mobile.app.model.bean.Contacts;
import com.k2.mobile.app.model.bean.LocalEmailBean;
import com.k2.mobile.app.model.bean.PublicBean;
import com.k2.mobile.app.model.bean.PublicRequestBean;
import com.k2.mobile.app.model.bean.ReqBodyBean;
import com.k2.mobile.app.model.bean.ReqMessage;
import com.k2.mobile.app.model.bean.WorkCountBean;
import com.k2.mobile.app.model.db.DBHelper;
import com.k2.mobile.app.model.db.table.Email;
import com.k2.mobile.app.model.http.ResponseInfo;
import com.k2.mobile.app.model.http.callback.RequestCallBack;
import com.k2.mobile.app.model.http.other.SendRequest;
import com.k2.mobile.app.utils.EncryptUtil;
import com.k2.mobile.app.utils.FastJSONUtil;
import com.k2.mobile.app.utils.LogUtil;
import com.k2.mobile.app.utils.NetWorkUtil;
import com.k2.mobile.app.utils.PublicResHeaderUtils;
import com.k2.mobile.app.view.widget.FloatView;

/**
 * 
 * @ClassName: MainActivity
 * @Description: TODO
 * @author linqijun
 * @date 2015-01-27 16:43:34
 * 
 */
public class MainActivity extends FragmentActivity implements OnClickListener {

	// ??????????????????
	private final int START_BACK_SERVER = 1;
	// ??????????????????
	private final int STOP_BACK_SERVER = 0;

	private LinearLayout ll_application, ll_my;
	private RelativeLayout rl_approval;
	private TextView tv_img_application, tv_img_approval, tv_img_my;
	private TextView tv_txt_application, tv_txt_approval, tv_txt_my;
	private TextView tv_approval_count, tv_email_count;

	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;
	// ????????????
	private ApplicationFragment application = null;
	// ??????
	private ApprovalFragment approval = null;
//	// ??????
//	private EmailFragment email = null;
	// ??????
	private MyFragment my = null;
	// ??????Server???????????????
	private Intent mIntent = null;
	// ????????????
	private IncomingReceiver iReceiver = null;

	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	private int emailCount = 0;
	private int approvalCount = 0;
	private boolean isDoubleClick_ap = false;
	private boolean isDoubleClick_em = false;
	// 1??????????????? 2?????????????????????
	private int operClass = 1;

	// ??????????????????????????????
	private long firstTime = 0;
	private long approvalTime = 0;
	private long emailTime = 0;
	private int eCount = 0;
	private int aCount = 0;
	// ?????????????????????0:?????????1:????????????
	private int operType = 1;
	// ??????????????????
	private final String EMAIL_COUNT_MOUDLE="2";
	// ??????????????????
	private final String APPROVAL_COUNT_MOUDLE="3";
	private ArrayList<FloatView> fvList = new ArrayList<FloatView>();

	private Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case 0: // ????????????
				exitLogin();
				startActivity(LoginActivity.class);
				MainActivity.this.finish();
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ????????????
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initView();
		initFragment();
		initListener();
		// ????????????
		createFilter();
		// ????????????
		startServer(START_BACK_SERVER);
		BaseApp.closeActivity();
	}

	private void initFragment() {

		fragmentManager = getSupportFragmentManager();
		transaction = fragmentManager.beginTransaction();

		application = new ApplicationFragment();
		approval = new ApprovalFragment();
//		email = new EmailFragment();
		my = new MyFragment();

		transaction.add(R.id.fr_content, application);
		transaction.add(R.id.fr_content, approval);
//		transaction.add(R.id.fr_content, email);
		transaction.add(R.id.fr_content, my);

		transaction.show(application).hide(approval).hide(my);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.commit();

		setBarBackground(1);
	}

	/**
	 * ?????????: initView() 
	 * ??? ??? : ????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initView() {
		
		ll_application = (LinearLayout) findViewById(R.id.ll_application);
		rl_approval = (RelativeLayout) findViewById(R.id.rl_approval);
		ll_my = (LinearLayout) findViewById(R.id.ll_my);

		tv_img_application = (TextView) findViewById(R.id.tv_img_application);
		tv_img_approval = (TextView) findViewById(R.id.tv_img_approval);
		tv_img_my = (TextView) findViewById(R.id.tv_img_my);

		tv_txt_application = (TextView) findViewById(R.id.tv_txt_application);
		tv_txt_approval = (TextView) findViewById(R.id.tv_txt_approval);
		tv_txt_my = (TextView) findViewById(R.id.tv_txt_my);

		tv_approval_count = (TextView) findViewById(R.id.tv_approval_count);
		

		sharedPreferences = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
		editor = sharedPreferences.edit();
		//2015/08/05??????????????????
//		DisplayMetrics dm = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(dm);
//		int widthPixels = dm.widthPixels; 	// ??????
//		int heightPixels = dm.heightPixels; // ??????
		
//		floatJobNumber((int) (widthPixels * 0.1), (int) (heightPixels * 0.04)); // ??????????????????
//		floatJobNumber((int) (widthPixels * 0.7), (int) (heightPixels * 0.04)); // ??????????????????

	}

	/**
	 * @Title: floatJobNumber
	 * @Description: ???????????????????????????????????????
	 * @param void
	 * @return void
	 */
	@SuppressLint("NewApi")
	private void floatJobNumber(float x, float y) {

		// ??????????????????
		final TextView tv_float = new TextView(getApplicationContext());
		tv_float.setText(BaseApp.user.getUserId());
		tv_float.setBackgroundColor(Color.argb(254, 200, 200, 200));
		tv_float.setTextColor(Color.argb(254, 134, 134, 134)); 		// ??????????????????
		FloatView fView = new FloatView(x, y);
		fView.show(this, this.getWindow(), tv_float);
		fvList.add(fView);

		Intent mIntent = new Intent(BroadcastNotice.FLOAT_JOB_NUMBER);
		sendBroadcast(mIntent);
	}

	/**
	 * ?????????: exitLogin() 
	 * ??? ??? : ???????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void exitLogin() {
		System.out.println("BaseApp.key"+BaseApp.key);
		BaseApp.key = null;
		BaseApp.isLogin = false;
		BaseApp.token = null;
		BaseApp.user = null;
	}

	/**
	 * ?????????: initListener() 
	 * ??? ??? : ????????????????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initListener() {
		ll_application.setOnClickListener(this);
		rl_approval.setOnClickListener(this);
		ll_my.setOnClickListener(this);
	}

	/**
	 * @Title: createFilter
	 * @Description: ??????IntentFilter
	 * @param void
	 * @return void
	 * @throws
	 */
	private void createFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BroadcastNotice.WIPE_USET);
		filter.addAction(BroadcastNotice.USER_EXIT);
		filter.addAction(BroadcastNotice.USER_LOGOUT);
		filter.addAction(BroadcastNotice.EMAIL_COUNT);
		filter.addAction(BroadcastNotice.APPROVAL_COUNT);
		iReceiver = new IncomingReceiver();
		registerReceiver(iReceiver, filter); // ????????????
	}

	/**
	 * ???????????? - ??????????????????
	 */
	private class IncomingReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();  
	        if(action.equals(BroadcastNotice.WIPE_USET)) {  
	        	System.out.println("IncomingReceiver = "+BroadcastNotice.WIPE_USET);
	        	editor.clear();
	        	editor.commit(); 
	    		if (null != BaseApp.db) {
	    			BaseApp.db = new DBHelper(MainActivity.this);
	    		}
	    		BaseApp.db.dateleTable(Contacts.class);
	    		BaseApp.db.dateleTable(LocalEmailBean.class);
    			BaseApp.db.dateleTable(Email.class);
        		// ????????????????????????
				if (NetWorkUtil.isNetworkAvailable(context)) {
					String wipe = userLoginOut();
					if (null != wipe) {
						operClass = 1;
						SendRequest.submitRequest(MainActivity.this, wipe, submitCallBack);
						BaseApp.closeActivity();
					}
				}
				BaseApp.closeActivity();
				exitLogin();
				startActivity(LoginActivity.class);
				MainActivity.this.finish();
			} else if (action.equals(BroadcastNotice.USER_EXIT)) { // ????????????
				BaseApp.closeActivity();
				exitLogin();
				startActivity(LoginActivity.class);
				MainActivity.this.finish();
	        } else if(action.equals(BroadcastNotice.USER_LOGOUT)) {// ????????????
	        	String info = userLoginOut();
				SendRequest.submitRequest(MainActivity.this, info, submitCallBack);
				BaseApp.closeActivity();
	        	exitLogin();
			} else if (action.equals(BroadcastNotice.EMAIL_COUNT)) {	// ??????EMAIL????????????
				int emailCount = intent.getIntExtra("emailCount", -1);
				if(-1 != emailCount){
					setEmailCount(emailCount);
				}else{
					operClass = 3;
					String email = getJson(BaseApp.user.getUserId(),EMAIL_COUNT_MOUDLE);
					SendRequest.sendSubmitRequest(getApplicationContext(), email, BaseApp.token, BaseApp.reqLang, 
		    				LocalConstants.WORK_COUNT_SERVER, BaseApp.key, submitCallBack);
				}
			} else if (action.equals(BroadcastNotice.APPROVAL_COUNT)) { // ???????????????????????????
				int workCount = intent.getIntExtra("approvalCount", -1);
				if(-1 != workCount){
					setApprovalCount(workCount);
				}else{
					operClass = 2;
					String json = getJson(BaseApp.user.getUserId() ,APPROVAL_COUNT_MOUDLE);
					SendRequest.sendSubmitRequest(getApplicationContext(), json, BaseApp.token, BaseApp.reqLang, 
							LocalConstants.WORK_COUNT_SERVER, BaseApp.key, submitCallBack); 
				}
			}
		}
	}
	
	void setEmailCount(int eCount){
		if (eCount > 0 && eCount < 100) {
			tv_email_count.setVisibility(View.VISIBLE);
			tv_email_count.setText(eCount + "");
		} else if (eCount >= 100) {
			tv_email_count.setVisibility(View.VISIBLE);
			tv_email_count.setText("99+");
		} else {
			tv_email_count.setVisibility(View.GONE);
		}
		emailCount = eCount;
	}
	
	void setApprovalCount(int aCount){
		if (aCount > 0 && aCount < 100) {
			tv_approval_count.setVisibility(View.VISIBLE);
			tv_approval_count.setText(aCount + "");
		} else if (aCount >= 100) {
			tv_approval_count.setVisibility(View.VISIBLE);
			tv_approval_count.setText("99+");
		} else {
			tv_approval_count.setVisibility(View.GONE);
		}
		approvalCount = aCount;
	}	

	/**
	 * @Title: getJson
	 * @Description: ????????????????????????
	 * @param void
	 * @return String ??????????????? 
	 * @throws
	 */
	private String getJson(String userCode,String module){
		PublicBean bean = new PublicBean();
		bean.setUserCode(userCode);
		bean.setModule(module);
		return JSON.toJSONString(bean);
	}
	
	/**
	 * @Title: resultFeedback
	 * @Description: ????????????????????????
	 * @param void
	 * @return String ???????????????
	 * @throws
	 */
	private String resultFeedback(int success) {

		PublicBean bean = new PublicBean();
		bean.setDeviceId(BaseApp.macAddr);
		bean.setUserId(BaseApp.user.getUserId());
		bean.setSuccess(success + "");

		return JSON.toJSONString(bean);
	}

	/**
	 * http????????????
	 */
	RequestCallBack<String> submitCallBack = new RequestCallBack<String>() {

		@Override
		public void onStart() {
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
					return;
				}else if ("1".equals(msg.getResCode())) {
					if(1 == operClass){
					exitLogin();
					startActivity(LoginActivity.class);
					MainActivity.this.finish();
					return;
				}else if(2 == operClass){
					// ?????????????????????????????????
					String decode = EncryptUtil.getDecodeData(msg.getMessage(), BaseApp.key);
					if(null != decode){
						WorkCountBean workCountBean = JSON.parseObject(decode, WorkCountBean.class);
						int workCount = 0;
						if(workCountBean.getCount() != null){
							try {
								workCount=Integer.parseInt(workCountBean.getCount());
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
						}
						setApprovalCount(workCount);
					}
				}else if(3 == operClass){
					// ?????????????????????????????????
					String decode = EncryptUtil.getDecodeData(msg.getMessage(), BaseApp.key);
					if(null != decode && !"".equals(decode.trim())){
						WorkCountBean workCountBean = JSON.parseObject(decode, WorkCountBean.class);
						int workCount = 0;
						if(workCountBean.getCount()!=null){
							try {
								workCount=Integer.parseInt(workCountBean.getCount());
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
						}
						setEmailCount(workCount);
					}
				 }
				}
			}
		}

		@Override
		public void onFailure(HttpException error, String msg) {
			
		}
	};

	/**
	 * @Title: startServer
	 * @Description: ??????????????????
	 * @param int flag: ????????????????????????????????????1?????????0??????
	 * @return void ????????????
	 * @throws
	 */
	private void startServer(int flag) {
		mIntent = new Intent(MainActivity.this, BackManagementServer.class);
		switch (flag) {
		// ????????????
		case START_BACK_SERVER:
			MainActivity.this.startService(mIntent);
			break;
		// ????????????
		case STOP_BACK_SERVER:
			mIntent = new Intent(BroadcastNotice.STOP_BACK_SERVER);
			MainActivity.this.sendBroadcast(mIntent);
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		// ??????server
		startServer(STOP_BACK_SERVER);
		// ??????????????????
		if(null != iReceiver){
			unregisterReceiver(iReceiver);
		}
		// ??????????????????
		for (int i = 0; i < fvList.size(); i++) {
			fvList.get(i).close(this);
		}
		
		super.onDestroy();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			boolean val = application.exitLongClick();
			if(val){
				long secondTime = System.currentTimeMillis();
				if (secondTime - firstTime > 2000) { // ????????????????????????????????????2??????????????????
					LogUtil.promptInfo(this, getString(R.string.click_again_exit));
					firstTime = secondTime; // ??????firstTime
					return true;
				} else {
					BaseApp.exit();
				}
			}else{
				return true;
			}
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_application:
			setBarBackground(1);
			transaction = fragmentManager.beginTransaction();
			transaction.show(application).hide(approval).hide(my);
			transaction.commit();
			isDoubleClick_ap = false;
			isDoubleClick_em = false;
			break;
		case R.id.rl_approval:
			application.exitLongClick();
			setBarBackground(2);
			transaction = fragmentManager.beginTransaction();
			transaction.show(approval).hide(application).hide(my);
			transaction.commit();
			
			eCount = 0;
			
			if(isDoubleClick_ap){
				long secondTime = System.currentTimeMillis();
				if (secondTime - approvalTime > 500) { // 500??????????????????????????????????????????
					approvalTime = secondTime; 
					aCount = 0;
				} else {
					if(1 < aCount){
						break;
					}
					approvalTime = secondTime;
					aCount++;
					MainActivity.this.sendBroadcast(new Intent(BroadcastNotice.APPROVAL_DOUBLE_CLICK));
				}
			}else{
				isDoubleClick_ap = true;
			}
			isDoubleClick_em = false;
			break;
		case R.id.ll_my:
			application.exitLongClick();
			setBarBackground(4);
			transaction = fragmentManager.beginTransaction();
			transaction.show(my).hide(application).hide(approval);
			transaction.commit();
			isDoubleClick_ap = false;
			isDoubleClick_em = false;
			break;
		}
	}

	/**
	 * @Title: setBarBackground
	 * @Description: ????????????????????????
	 * @param int val: ???????????????
	 * @return void
	 * @throws
	 */
	@SuppressLint("ResourceAsColor")
	private void setBarBackground(int val) {
		switch (val) {
		case 1:
			tv_img_application.setBackgroundResource(R.drawable.btm_application_che);
			tv_img_approval.setBackgroundResource(R.drawable.btm_approval);
			tv_img_my.setBackgroundResource(R.drawable.btm_contacts);

			tv_txt_application.setTextColor(0xff00925f);
			tv_txt_approval.setTextColor(0xff353636);
			tv_txt_my.setTextColor(0xff353636);
			break;
		case 2:
			tv_img_application.setBackgroundResource(R.drawable.btm_application);
			tv_img_approval.setBackgroundResource(R.drawable.btm_approval_che);
			tv_img_my.setBackgroundResource(R.drawable.btm_contacts);

			tv_txt_application.setTextColor(0xff353636);
			tv_txt_approval.setTextColor(0xff00925f);
			tv_txt_my.setTextColor(0xff353636);
			break;
		case 3:
			tv_img_application.setBackgroundResource(R.drawable.btm_application);
			tv_img_approval.setBackgroundResource(R.drawable.btm_approval);
			tv_img_my.setBackgroundResource(R.drawable.btm_contacts);

			tv_txt_application.setTextColor(0xff353636);
			tv_txt_approval.setTextColor(0xff353636);
			tv_txt_my.setTextColor(0xff353636);
			break;
		case 4:
			tv_img_application.setBackgroundResource(R.drawable.btm_application);
			tv_img_approval.setBackgroundResource(R.drawable.btm_approval);
			tv_img_my.setBackgroundResource(R.drawable.btm_contacts_che);

			tv_txt_application.setTextColor(0xff353636);
			tv_txt_approval.setTextColor(0xff353636);
			tv_txt_my.setTextColor(0xff00925f);
			break;
		}
	}

	/**
	 * @Title: startActivity
	 * @Description: ??????Activity
	 * @return void ????????????
	 * @throws
	 */
	public void startActivity(Class<?> activity) {
		Intent intent = new Intent(this, activity);
		startActivity(intent);
	}

	/**
	 * @Title: userMenuInfo
	 * @Description: ??????????????????????????????
	 * @param void
	 * @return String ???????????????
	 * @throws
	 */
	private String userLoginOut() {

		ReqBodyBean bBean = new ReqBodyBean();
		bBean.setInvokeFunctionCode("F10000006");
				
		PublicRequestBean bean = new PublicRequestBean();
		bean.setReqHeader(PublicResHeaderUtils.getReqHeader());
		bean.setReqBody(bBean);
		
		return JSON.toJSONString(bean);
	}
	
}
