package com.k2.mobile.app.controller.activity.menu.feedback;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.k2.mobile.app.R;
import com.k2.mobile.app.common.config.BroadcastNotice;
import com.k2.mobile.app.common.config.LocalConstants;
import com.k2.mobile.app.common.exception.HttpException;
import com.k2.mobile.app.controller.core.BaseApp;
import com.k2.mobile.app.model.adapter.GridViewShowImgAdapter;
import com.k2.mobile.app.model.bean.CategoryBean;
import com.k2.mobile.app.model.bean.FeedbackListBean;
import com.k2.mobile.app.model.bean.FileBean;
import com.k2.mobile.app.model.bean.PublicBean;
import com.k2.mobile.app.model.bean.QuestioClassBean;
import com.k2.mobile.app.model.bean.ReqMessage;
import com.k2.mobile.app.model.bean.ResPublicBean;
import com.k2.mobile.app.model.bean.SpinnerBean;
import com.k2.mobile.app.model.http.ResponseInfo;
import com.k2.mobile.app.model.http.callback.RequestCallBack;
import com.k2.mobile.app.model.http.other.SendRequest;
import com.k2.mobile.app.utils.DateUtil;
import com.k2.mobile.app.utils.DialogUtil;
import com.k2.mobile.app.utils.EncryptUtil;
import com.k2.mobile.app.utils.ErrorCodeContrast;
import com.k2.mobile.app.utils.FastJSONUtil;
import com.k2.mobile.app.utils.LogUtil;
import com.k2.mobile.app.utils.NetWorkUtil;
import com.k2.mobile.app.view.wheel.StrericWheelAdapter;
import com.k2.mobile.app.view.wheel.WheelView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 * @ClassName: FeedbackTreatActivity
 * @Description: ????????????
 * @author linqijun
 * @date 2015-6-08 14:24:00
 * 
 */
public class FeedbackTreatActivity extends FragmentActivity implements OnClickListener {
	
	// ??????
	private Button btn_back;
	// ????????????
	private TextView tv_title;
	private TextView tv_search;
	private Spinner sp_class;
	
	private TextView tv_number; 				// ??????
	private TextView tv_state;  				// ??????
	private TextView tv_job_number; 			// ??????
	private TextView tv_user_name;  			// ??????
	private TextView tv_department;	 			// ?????? 
	private TextView tv_contact_phone;			// ????????????
	
	private TextView tv_question_class; 		// ????????????
	private TextView tv_feedback_time;			// ????????????
	private TextView tv_question_summary; 		// ????????????
	private TextView tv_question_description; 	// ????????????
	private GridView gv_img_show; 				// ????????????
	
	private TextView tv_somebody;				// ?????????
	private TextView tv_somebody_time;			// ????????????
//	private TextView tv_question_type;			// ????????????
	private TextView tv_estimated_time_tip;		// ????????????
	private TextView tv_qcName;
	private TextView tv_d_people;
	private TextView tv_actual_time;
	private TextView tv_remarks;
	
	private TextView tv_now_time;
	private TextView tv_date_time;
	private TextView tv_estimated_time;
	private TextView tv_commit;
	private TextView tv_inextricability;
	private EditText ed_description;
	private LinearLayout ll_pretreatment;
	private LinearLayout ll_direct_treatment;
	private LinearLayout ll_estimated_time;
	private LinearLayout ll_btn;
	private LinearLayout ll_sper;
	private LinearLayout ll_qcName;
	private LinearLayout ll_remark;
	private LinearLayout ll_d_people;
	private LinearLayout ll_actual_time;
	private GridViewShowImgAdapter gvAdapter = null;
	
	private String flag = null;
	private String code = null;
	
	private WheelView yearWheel, monthWheel, dayWheel, hourWheel, minuteWheel, secondWheel;
	public static String[] yearContent = null;
	public static String[] monthContent = null;
	public static String[] dayContent = null;
	public static String[] hourContent = null;
	public static String[] minuteContent = null;
	public static String[] secondContent = null;
	
	private AlertDialog dialog = null;
	
	private TextView tv_confirm;
	private TextView tv_cancel;
	
	private int type = 1;
	private FeedbackListBean bean = null;
	
	private List<CategoryBean> sbList = null;
	private List<QuestioClassBean> qcList = null;
	private List<FileBean> fileList = null;
	private int status = 0;
	
	private int operType = 1;
	
	private Handler mHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			
			switch (msg.what) {
				case 1:
					String resed = (String) msg.obj;
					if (null != resed && !"".equals(resed.trim())) {
						FeedbackListBean bean = JSON.parseObject(resed, FeedbackListBean.class);
						if (null != bean) {
							if (null != bean.getQuestionCategorylist()) {
								sbList = JSON.parseArray(bean.getQuestionCategorylist(),CategoryBean.class);
							}
							
							if (null != bean.getAttachmentList()) {
								if(null != fileList){
									fileList.clear();
								}
								fileList.addAll(JSON.parseArray(bean.getAttachmentList(), FileBean.class));
								gvAdapter.notifyDataSetChanged();
							}

							if (null != bean.getQuestionClasslist()) {
								qcList = JSON.parseArray(bean.getQuestionClasslist(), QuestioClassBean.class);
								List<SpinnerBean> strList = new ArrayList<SpinnerBean>();
								for (int i = 0; i < qcList.size(); i++) {
									SpinnerBean sBean = new SpinnerBean(qcList.get(i).getId(), qcList.get(i).getQuestionClassName());
									strList.add(sBean);
								}
								
								setAdapter(strList, bean.getQuestionClassID());
							}
							
							tv_number.setText(bean.getQuestionFeedbackNumber());
							if(null != bean.getStatus() && !"".equals(bean.getStatus().trim())){
								status = Integer.parseInt(bean.getStatus());
								switch (status) {
									case 3:
										tv_state.setText(getString(R.string.prehandle));
										ll_estimated_time.setVisibility(View.GONE);
										tv_inextricability.setVisibility(View.GONE);
										if("0".equals(flag)){
											tv_commit.setText(getString(R.string.pretreatment));
										}else if("1".equals(flag)){
											tv_commit.setText(getString(R.string.complete_treatment));
										}
										break;
									case 4:
										tv_state.setText(getString(R.string.handling));
										tv_commit.setText(getString(R.string.complete_treatment));
										tv_inextricability.setText(getString(R.string.could_not_resolved));
										break;
									case 7:
										tv_qcName.setText(bean.getQuestionClassName());
										tv_remarks.setText(bean.getRemark());
										tv_d_people.setText(bean.getHandlerName());
										if(null != bean.getHandleFactTime() && !"".equals(bean.getHandleFactTime().trim())){
											String tmpTiem = bean.getHandleFactTime().replace("T", " ");
											tv_actual_time.setText(DateUtil.getDate_ymdhm(tmpTiem));
										}
										
										break;
								}
							}
							 
							tv_job_number.setText(bean.getCreatorCode());
							tv_user_name.setText(bean.getCreatorName());
							tv_department.setText(bean.getCreatorDepartment());
							tv_contact_phone.setText(bean.getCreatorPhoneNum());
							
							tv_question_class.setText(bean.getQuestionCategoryName());
							if(null != bean.getCreateDatetime() && !"".equals(bean.getCreateDatetime().trim())){
								String tmpTiem = bean.getCreateDatetime().replace("T", " ");
								tv_feedback_time.setText(DateUtil.getDate_ymdhm(tmpTiem));
							}
							tv_question_summary.setText(bean.getQuestionAbstract());
							tv_question_description.setText(bean.getQuestionDescription());
							
							if(null != bean.getFinishEstimateTime() && !"".equals(bean.getFinishEstimateTime().trim())){
								String tmpTiem = bean.getFinishEstimateTime().replace("T", " ");
								tv_estimated_time_tip.setText(DateUtil.getDate_ymdhm(tmpTiem));
							}
							
							tv_somebody.setText(bean.getReviewerName());
							if(null != bean.getReviewDatetime() && !"".equals(bean.getReviewDatetime().trim())){
								String tmpTiem = bean.getReviewDatetime().replace("T", " ");
								tv_somebody_time.setText(DateUtil.getDate_ymdhm(tmpTiem));
							}
						}
					}
					break;
				case 2:
					break;
				case 3:
					String resSave = (String) msg.obj;
					if (null != resSave) {
						ResPublicBean bean = JSON.parseObject(resSave, ResPublicBean.class);
						if (null != bean && "1".equals(bean.getSuccess())) {
							DialogUtil.showLongToast(FeedbackTreatActivity.this, R.string.deal_with_success);
							Intent mIntent = new Intent(BroadcastNotice.FEEDBACK_FINISH_UPDATE_TREATED);
							sendBroadcast(mIntent);
							finish();
						} else {
							DialogUtil.showLongToast(FeedbackTreatActivity.this, R.string.dealing_with_failure);
						}
					}
					break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ????????????
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_feedbcak_treat);
		initView();
		initListener();
		logic();
		BaseApp.addActivity(this);
	}

	/**
	 * ?????????: initView() 
	 * ??? ??? : ????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initView() {	
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_search = (TextView) findViewById(R.id.tv_sech);
		btn_back = (Button) findViewById(R.id.btn_back);
		
		tv_number = (TextView) findViewById(R.id.tv_list_title);
		tv_state = (TextView) findViewById(R.id.tv_state);
		
		tv_job_number = (TextView) findViewById(R.id.tv_job_number);
		tv_user_name = (TextView) findViewById(R.id.tv_user_name);
		tv_department = (TextView) findViewById(R.id.tv_department);	 			
		tv_contact_phone = (TextView) findViewById(R.id.tv_contact_phone);	 			
		tv_question_class = (TextView) findViewById(R.id.tv_question_class);
		tv_feedback_time = (TextView) findViewById(R.id.tv_feedback_time);
		tv_question_summary = (TextView) findViewById(R.id.tv_question_summary);
		tv_question_description = (TextView) findViewById(R.id.tv_question_description);
		
		tv_somebody = (TextView) findViewById(R.id.tv_somebody);
		tv_estimated_time_tip = (TextView) findViewById(R.id.tv_estimated_time_tip);
		tv_somebody_time = (TextView) findViewById(R.id.tv_somebody_time);
		tv_qcName = (TextView) findViewById(R.id.tv_qcName);
		tv_d_people = (TextView) findViewById(R.id.tv_d_people);
		tv_actual_time = (TextView) findViewById(R.id.tv_actual_time);
		tv_remarks = (TextView) findViewById(R.id.tv_remarks);
		
		gv_img_show = (GridView) findViewById(R.id.gv_img_show);
		
		ed_description = (EditText) findViewById(R.id.ed_description);
		tv_date_time = (TextView) findViewById(R.id.tv_date_time);
		tv_now_time = (TextView) findViewById(R.id.tv_now_time);
		tv_commit = (TextView) findViewById(R.id.tv_commit);
		tv_inextricability = (TextView) findViewById(R.id.tv_inextricability);
		
		tv_estimated_time = (TextView) findViewById(R.id.tv_estimated_time);
		ll_pretreatment = (LinearLayout) findViewById(R.id.ll_pretreatment);
		ll_direct_treatment = (LinearLayout) findViewById(R.id.ll_direct_treatment);
		ll_estimated_time = (LinearLayout) findViewById(R.id.ll_estimated_time);
		ll_btn = (LinearLayout) findViewById(R.id.ll_btn);
		ll_sper = (LinearLayout) findViewById(R.id.ll_sper);
		ll_qcName = (LinearLayout) findViewById(R.id.ll_qcName);
		ll_remark = (LinearLayout) findViewById(R.id.ll_remark);
		ll_d_people = (LinearLayout) findViewById(R.id.ll_d_people);
		ll_actual_time = (LinearLayout) findViewById(R.id.ll_actual_time);
		
		sp_class = (Spinner) findViewById(R.id.sp_class);
		
	}
	
	/**
	 * ?????????: initListener() 
	 * ??? ??? : ????????? ????????? 
	 * ??? ??? : void 
	 * ?????????: void  
	 */
	private void initListener() {
		btn_back.setOnClickListener(this);
		tv_search.setOnClickListener(this);
		tv_date_time.setOnClickListener(this);
		tv_now_time.setOnClickListener(this);
		tv_commit.setOnClickListener(this);
		tv_inextricability.setOnClickListener(this);
		gv_img_show.setOnItemClickListener(itemListener);
	}
	/**
	 * ?????????: logic() 
	 * ??? ??? : ???????????? 
	 * ??? ??? : void 
	 * ?????????: void  
	 */
	private void logic(){
		tv_title.setText(getString(R.string.feedback_info));
		tv_search.setVisibility(View.GONE);
		tv_date_time.setText(DateUtil.getDate_ymd());
		tv_now_time.setText(DateUtil.getDate_hms());
		
		initAdapter();
		
		flag = getIntent().getStringExtra("flag");
		code = getIntent().getStringExtra("code");
				
		if("0".equals(flag)){
			tv_estimated_time.setVisibility(View.VISIBLE);
			ll_direct_treatment.setVisibility(View.VISIBLE);
			ll_pretreatment.setVisibility(View.GONE);
		}else if("1".equals(flag)){
			tv_estimated_time.setVisibility(View.GONE);
			ll_direct_treatment.setVisibility(View.GONE);
			ll_pretreatment.setVisibility(View.VISIBLE);
		}else if("2".equals(flag)){
			ll_qcName.setVisibility(View.VISIBLE);
			ll_remark.setVisibility(View.VISIBLE);
			ll_d_people.setVisibility(View.VISIBLE);
			ll_actual_time.setVisibility(View.VISIBLE);
			tv_estimated_time.setVisibility(View.GONE);
			ll_btn.setVisibility(View.GONE);
			ll_sper.setVisibility(View.GONE);
		}
		
		initContent();
		
		String info = receiveMailQuest();
		if(null != info && !"".equals(info)){
			requestServer(LocalConstants.FEEDBACK_INFO_SERVER, info);
		}
	}
	
	/**
	 * ?????????: setAdapter() 
	 * ??? ??? : ?????????????????? 
	 * ??? ??? : list ????????? 
	 * ?????????: void
	 */
	private void setAdapter(List<SpinnerBean> list, String questionClassID) {
		if(null == list){
			return;
		}
		ArrayAdapter<SpinnerBean> adapter = new ArrayAdapter<SpinnerBean>(this, R.layout.spinner_user_check, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_class.setAdapter(adapter);
		int count = 0;
		for(;count < list.size(); count++){
			if(null != questionClassID && null != list.get(count).getValue() && questionClassID.equals(list.get(count).getValue())){
				break;
			}
		}
		
		sp_class.setSelection(count);
	}
	
	private void initAdapter(){
		if(null == fileList){
			fileList = new ArrayList<FileBean>();
		}
		gvAdapter = new GridViewShowImgAdapter(this, fileList);
		gv_img_show.setAdapter(gvAdapter);
	}
	
	OnItemClickListener itemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent mIntent = new Intent(FeedbackTreatActivity.this, ShowImgActivity.class);
			mIntent.putExtra("fileList", (Serializable)fileList);
			mIntent.putExtra("position", position);
			startActivity(mIntent);		
		}
	};
	/**
	 * ?????????: initContent() 
	 * ??? ??? : 
	 * ????????? ??? ??? : void 
	 * ?????????: void
	 */
	public void initContent() {
		// ?????????
		yearContent = new String[10];
		for (int i = 0; i < 10; i++)
			yearContent[i] = String.valueOf(i + 2013);
		// ?????????
		monthContent = new String[12];
		for (int i = 0; i < 12; i++) {
			monthContent[i] = String.valueOf(i + 1);
			if (monthContent[i].length() < 2) {
				monthContent[i] = "0" + monthContent[i];
			}
		}
		// ?????????
		dayContent = new String[31];
		for (int i = 0; i < 31; i++) {
			dayContent[i] = String.valueOf(i + 1);
			if (dayContent[i].length() < 2) {
				dayContent[i] = "0" + dayContent[i];
			}
		}
		// ????????????
		hourContent = new String[24];
		for (int i = 0; i < 24; i++) {
			hourContent[i] = String.valueOf(i);
			if (hourContent[i].length() < 2) {
				hourContent[i] = "0" + hourContent[i];
			}
		}
		// ?????????
		minuteContent = new String[60];
		for (int i = 0; i < 60; i++) {
			minuteContent[i] = String.valueOf(i);
			if (minuteContent[i].length() < 2) {
				minuteContent[i] = "0" + minuteContent[i];
			}
		}
		// ???
		secondContent = new String[60];
		for (int i = 0; i < 60; i++) {
			secondContent[i] = String.valueOf(i);
			if (secondContent[i].length() < 2) {
				secondContent[i] = "0" + secondContent[i];
			}
		}
	}
		
	private void setTime(int layouts){
		LayoutInflater inflater = LayoutInflater.from(FeedbackTreatActivity.this);
		View view = inflater.inflate(layouts, null);
		Calendar calendar = Calendar.getInstance();
		if(1 == type){
			int curYear = calendar.get(Calendar.YEAR);
			int curMonth = calendar.get(Calendar.MONTH) + 1;
			int curDay = calendar.get(Calendar.DAY_OF_MONTH);
			
			yearWheel = (WheelView) view.findViewById(R.id.yearwheel);
			monthWheel = (WheelView) view.findViewById(R.id.monthwheel);
			dayWheel = (WheelView) view.findViewById(R.id.daywheel);
			
			yearWheel.setAdapter(new StrericWheelAdapter(yearContent));
			yearWheel.setCurrentItem(curYear - 2013);
			yearWheel.setCyclic(true);
			yearWheel.setInterpolator(new AnticipateOvershootInterpolator());

			monthWheel.setAdapter(new StrericWheelAdapter(monthContent));

			monthWheel.setCurrentItem(curMonth - 1);

			monthWheel.setCyclic(true);
			monthWheel.setInterpolator(new AnticipateOvershootInterpolator());

			dayWheel.setAdapter(new StrericWheelAdapter(dayContent));
			dayWheel.setCurrentItem(curDay - 1);
			dayWheel.setCyclic(true);
			dayWheel.setInterpolator(new AnticipateOvershootInterpolator());
			
		}else if(2 == type){
			int curHour = calendar.get(Calendar.HOUR_OF_DAY);
			int curMinute = calendar.get(Calendar.MINUTE);
			
			hourWheel = (WheelView) view.findViewById(R.id.hourwheel);
			minuteWheel = (WheelView) view.findViewById(R.id.minutewheel);
			
			hourWheel.setAdapter(new StrericWheelAdapter(hourContent));
			hourWheel.setCurrentItem(curHour);
			hourWheel.setCyclic(true);
			hourWheel.setInterpolator(new AnticipateOvershootInterpolator());
	
			minuteWheel.setAdapter(new StrericWheelAdapter(minuteContent));
			minuteWheel.setCurrentItem(curMinute);
			minuteWheel.setCyclic(true);
			minuteWheel.setInterpolator(new AnticipateOvershootInterpolator());
		}
		
		tv_confirm = (TextView) view.findViewById(R.id.tv_confirm);
		tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
		
		tv_confirm.setOnClickListener(this);
		tv_cancel.setOnClickListener(this);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view);
		
		dialog = builder.show();
	}
	/**
	 * ?????????: receiveMailQuest() 
	 * ??? ??? : ????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private String receiveMailQuest(){
		PublicBean bean = new PublicBean();
		bean.setDeviceId(BaseApp.macAddr);
		bean.setUserAccount(BaseApp.user.getUserId());
		bean.setFlag("1");
		bean.setCode(code);
		
		return JSON.toJSONString(bean);
	}
	
	/**
	 * ?????????: receivePretreatment() 
	 * ??? ??? : ?????????????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private String receivePretreatment(int val, String handleType){

		String categoryId = ((SpinnerBean) sp_class.getSelectedItem()).getValue();
		String categoryName = ((SpinnerBean) sp_class.getSelectedItem()).getText();
		String description = ed_description.getText().toString();
		String startTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
		StringBuilder sb = new StringBuilder();
		sb.append(tv_date_time.getText().toString())
		  .append(" ")
		  .append(tv_now_time.getText().toString())
		  .append(":01");
		
		PublicBean bean = new PublicBean();
		bean.setDeviceId(BaseApp.macAddr);
		bean.setUserAccount(BaseApp.user.getUserId());
		bean.setCode(code);
		bean.setClassId(categoryId);
		bean.setClassName(categoryName);
		if(1 == val){
			int timeCheck = DateUtil.timeComparingSize(startTime, sb.toString());
			if (-1 == timeCheck) {
				DialogUtil.showLongToast(FeedbackTreatActivity.this, R.string.time_format_error);
				return null;
			} else if (0 == timeCheck) {
				DialogUtil.showLongToast(FeedbackTreatActivity.this, R.string.time_can_not);
				return null;
			} else if (1 == timeCheck) {
				DialogUtil.showLongToast(FeedbackTreatActivity.this, R.string.start_now_time_err);
				return null;
			}
			bean.setPtime(sb.toString());
		}else{
			bean.setHandleType(handleType);
			bean.setRemark(description);
		}
		 
		return JSON.toJSONString(bean);
	}
	
	/**
	* @Title: requestServer
	* @Description: ??????????????????
	* @param void
	* @return void
	* @throws
	*/
	private void requestServer(int server, String info) {
		// ????????????????????????
		if (!NetWorkUtil.isNetworkAvailable(FeedbackTreatActivity.this)) {
			DialogUtil.showLongToast(FeedbackTreatActivity.this, R.string.global_network_disconnected);
		}else{
			SendRequest.sendSubmitRequest(FeedbackTreatActivity.this, info, BaseApp.token, BaseApp.reqLang, server, BaseApp.key, submitCallBack);
		}
	}
	
	/**
	 * http????????????
	 */
	RequestCallBack<String> submitCallBack = new RequestCallBack<String>() {

		@Override
		public void onStart() {
			DialogUtil.showWithCancelProgressDialog(FeedbackTreatActivity.this, null, getResources().getString(R.string.global_prompt_message), null);
		}

		@Override
		public void onLoading(long total, long current, boolean isUploading) {

		}
		
		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {     
			DialogUtil.closeDialog();
			String result = responseInfo.result.toString();
			if (null != result && !"".equals(result.trim())) {
				ReqMessage msg = FastJSONUtil.getJSONToEntity(result, ReqMessage.class);
				// ????????????????????????????????????
				if (null == msg || null == msg.getResCode() || "".equals(msg.getResCode())) {				
					LogUtil.promptInfo(FeedbackTreatActivity.this, ErrorCodeContrast.getErrorCode("0", getResources()));
					return;
				// ???????????????
				}else if ("1103".equals(msg.getResCode()) || "1104".equals(msg.getResCode())) {
					LogUtil.promptInfo(FeedbackTreatActivity.this, ErrorCodeContrast.getErrorCode(msg.getResCode(), getResources()));
					Intent mIntent = new Intent(BroadcastNotice.USER_EXIT);
					FeedbackTreatActivity.this.sendBroadcast(mIntent);
					return;
				}else if("1210".equals(msg.getResCode())){
					LogUtil.promptInfo(FeedbackTreatActivity.this, ErrorCodeContrast.getErrorCode(msg.getResCode(), getResources()));	
					Intent mIntent = new Intent(BroadcastNotice.WIPE_USET);
					sendBroadcast(mIntent);
					return;
				}else if (!"1".equals(msg.getResCode())) {
					LogUtil.promptInfo(FeedbackTreatActivity.this, ErrorCodeContrast.getErrorCode(msg.getResCode(), getResources()));	
					return;
				}
				
				// ???????????????????????????
				if (null != msg.getMessage() && !"".equals(msg.getMessage().trim())) {		 
					// ?????????????????????????????????
					String decode = EncryptUtil.getDecodeData(msg.getMessage(), BaseApp.key);
					Message msgs = new Message();
					msgs.what = operType;
					msgs.obj = decode;
					mHandler.sendMessage(msgs);
				} 
			} else {
				LogUtil.promptInfo(FeedbackTreatActivity.this, ErrorCodeContrast.getErrorCode("0", getResources()));
			}
		}

		@Override
		public void onFailure(HttpException error, String msg) {
			DialogUtil.closeDialog();
			if(null != msg && msg.equals(LocalConstants.API_KEY)){
				LogUtil.promptInfo(FeedbackTreatActivity.this, ErrorCodeContrast.getErrorCode("1207", getResources()));
			}else{
				LogUtil.promptInfo(FeedbackTreatActivity.this, ErrorCodeContrast.getErrorCode("0", getResources()));
			}
		}
	};
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				this.finish();
				break;
			case R.id.tv_confirm:			// ????????????
				StringBuffer sb = new StringBuffer();
				if(1 == type){
					sb.append(yearWheel.getCurrentItemValue())
					.append("-")
					.append(monthWheel.getCurrentItemValue())
					.append("-")
					.append(dayWheel.getCurrentItemValue());
					tv_date_time.setText(sb);
				}else if(2 == type){
					sb.append(hourWheel.getCurrentItemValue())
					.append(":")
					.append(minuteWheel.getCurrentItemValue());
					tv_now_time.setText(sb);
				}
			case R.id.tv_cancel:
				if(null != dialog){
					dialog.dismiss();
				}
				break;
			case R.id.tv_now_time:
				type = 2;
				setTime(R.layout.time_hm);
				break;
			case R.id.tv_date_time:
				type = 1;
				setTime(R.layout.time_ymd);
				break;
			case R.id.tv_commit:
				String info = null;
				int server = 0;
				if(3 == status){
					info = receivePretreatment(1, "2");
					server = LocalConstants.FEEDBACK_PRETREATMENT_SERVER;
				}else if(4 == status){
					info = receivePretreatment(0, "0");
					server = LocalConstants.FEEDBACK_PROCESS_SERVER;
				}
				if(null == info || "".equals(info)){
					break;
				}
				operType = 3;
				requestServer(server, info);
				break;
			case R.id.tv_inextricability:
				String infos = receivePretreatment(0, "1");
				server = LocalConstants.FEEDBACK_PROCESS_SERVER;
				if(null == infos || "".equals(infos)){
					break;
				}
				operType = 3;
				requestServer(server, infos);
				break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	};
}