package com.k2.mobile.app.controller.activity.fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.alibaba.fastjson.JSON;
import com.k2.mobile.app.R;
import com.k2.mobile.app.common.config.BroadcastNotice;
import com.k2.mobile.app.common.config.LocalConstants;
import com.k2.mobile.app.common.exception.HttpException;
import com.k2.mobile.app.controller.activity.menu.personalCenter.EditHeadImgActivity;
import com.k2.mobile.app.controller.core.BaseApp;
import com.k2.mobile.app.model.bean.AutoUpdateProcedureBean;
import com.k2.mobile.app.model.bean.PublicRequestBean;
import com.k2.mobile.app.model.bean.PublicResultBean;
import com.k2.mobile.app.model.bean.ReqBodyBean;
import com.k2.mobile.app.model.bean.ReqMessage;
import com.k2.mobile.app.model.bean.User;
import com.k2.mobile.app.model.http.ResponseInfo;
import com.k2.mobile.app.model.http.callback.RequestCallBack;
import com.k2.mobile.app.model.http.other.SendRequest;
import com.k2.mobile.app.utils.DialogUtil;
import com.k2.mobile.app.utils.EncryptUtil;
import com.k2.mobile.app.utils.ErrorCodeContrast;
import com.k2.mobile.app.utils.ImageLoaderUtil;
import com.k2.mobile.app.utils.ImageUtil;
import com.k2.mobile.app.utils.LogUtil;
import com.k2.mobile.app.utils.NetWorkUtil;
import com.k2.mobile.app.utils.PublicResHeaderUtils;
import com.k2.mobile.app.utils.StringUtil;
import com.k2.mobile.app.utils.SystemUtil;
import com.k2.mobile.app.utils.UpdateProcedureUtil;
import com.k2.mobile.app.view.widget.PopwinPerCenter;

/**
 * @Title ContactsFragment.java
 * @Package com.oppo.mo.controller.activity.fragment
 * @Description ??????????????????
 * @Company  K2
 * 
 * @author linqijun
 * @date 2015-04-22 20:45:03
 * @version V1.0
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MyFragment extends Fragment implements OnClickListener {
	
	private View view;
	// ??????
	private Button btn_back;
	// ?????????, ??????, ??????, ??????, ????????????, ????????????, E-mail, ????????????, ?????????
	private TextView tv_name, tv_dep, tv_office, tv_land_num, tv_mob_num, tv_email, 
					  tv_address, tv_feedback, tv_upgrade, tv_en_name;
	
	private ImageView iv_land_pho,iv_mob,iv_mail,iv_addre, iv_en_name;
	// ?????????, ??????????????????????????????E-mail???????????????
	private RelativeLayout rl_en_name, rl_land_num, rl_address, rl_version, rl_logout;
	// ??????
	private TextView tv_logout,tv_version_show;
	// ??????
	private ImageView iv_head_img;
	// ????????????????????????
	private PopwinPerCenter mPopwinPerCenter;
	// ??????????????????
	public static final File PHOTO_DIR = new File(LocalConstants.LOCAL_FILE_PATH );
	// ??????
	private File tempFile;
	// ????????????
	private Uri tempuri;
	// ???????????????
	private String content;
	// ???????????????????????????
	private User userBean;
	// ?????????????????????????????? 
	private static final int CAMERA_WITH_DATA = 100;
	// ??????????????????gallery 
	private static final int PHOTO_PICKED_WITH_DATA = 110;	// 4.4????????????
	private static final int SELECT_PIC_KITKAT = 111;		// 4.4??????
	// ?????????????????????
	private static final int CROP_PHOTO = 120;
	// ????????????
	private static final int UPDATE_IMG = 130;
	// ???????????????1???????????????2???????????????3E-mail???4????????????, 5?????????
	private int typeClass = 1;
	// ????????????
	private int operType = 1;
	private Bitmap bitmap = null;
	// ????????????
	private IncomingReceiver iReceiver = null;
	// ??????????????????
	public ImageLoaderUtil imageLoader;   
	protected Resources res;
	
	private Handler mHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
				case 1:
					String json = (String) msg.obj;
					System.out.println("my = " + json);
					if (null != json) {
						userBean = JSON.parseObject(json, User.class);
						if(null != userBean) {
							tv_dep.setText(userBean.getRealityOrgName());
//							tv_office.setText("("+userBean.getJobDesc()+")");
							
							if(null == userBean.getMobilePhone() || "".equals(userBean.getMobilePhone().trim())){
								tv_land_num.setText(getString(R.string.my_telephone));
								tv_land_num.setAlpha(0.5f);
								tv_land_num.setTextColor(getResources().getColor(R.color.color_gray));
								iv_land_pho.setBackgroundResource(R.drawable.pre_land_pho_disable);
							}else{
								iv_land_pho.setBackgroundResource(R.drawable.pre_land_pho);
								tv_land_num.setText(userBean.getMobilePhone());
							}
							
							if(null == userBean.getEmail() || "".equals(userBean.getEmail().trim())){
								tv_email.setText(getString(R.string.my_emails));
								tv_email.setAlpha(0.5f);
								tv_email.setTextColor(getResources().getColor(R.color.color_gray));
								iv_mail.setBackgroundResource(R.drawable.pre_email_disable);
							}else{
								iv_mail.setBackgroundResource(R.drawable.pre_email);
								tv_email.setText(userBean.getEmail());
							}
							
							if(null == userBean.getOfficeAddress() || "".equals(userBean.getOfficeAddress().trim())){
								tv_address.setText(getString(R.string.my_address));
								tv_address.setAlpha(0.5f);
								tv_address.setTextColor(getResources().getColor(R.color.color_gray));
								iv_addre.setBackgroundResource(R.drawable.pre_address_disable);
							}else{
								tv_address.setText(userBean.getOfficeAddress());
								iv_addre.setBackgroundResource(R.drawable.pre_address);
							}
							
							if(null != userBean.getUserEnName() && !"".equals(userBean.getUserEnName().trim())){
								tv_en_name.setText(userBean.getUserEnName());
							}else{
								tv_en_name.setAlpha(0.5f);
								tv_en_name.setTextColor(getResources().getColor(R.color.color_gray));
							}
							
							imageLoader.DisplayImage(userBean.getFileUrl(), iv_head_img,1, userBean.getSex()); // ??????????????????
						}
					}
					break;
				case 2:
					String jsons = (String) msg.obj;
					if(null != jsons && "1".equals(jsons)){
						DialogUtil.showLongToast(getActivity(), R.string.global_edit_status_success);
						switch (typeClass) {
							case 1:
								tv_land_num.setText(content);
								userBean.setOfficeTel(content);
								if(null == content || "".equals(content.trim())){
									tv_land_num.setAlpha(0.5f);
									tv_land_num.setTextColor(getResources().getColor(R.color.color_gray));
									iv_land_pho.setBackgroundResource(R.drawable.pre_land_pho_disable);
								}else{
									tv_land_num.setAlpha(1f);
									tv_land_num.setTextColor(getResources().getColor(R.color.bar_home_bg));
									iv_land_pho.setBackgroundResource(R.drawable.pre_land_pho);
								}
								break;
							case 2:
								tv_mob_num.setText(content);
//								if(null == content || "".equals(content.trim())){
//									iv_mob.setBackgroundResource(R.drawable.pre_phone_disable);
//								}else{
//									iv_mob.setBackgroundResource(R.drawable.pre_phone);
//								}
								userBean.setMobilePhone(content);
								break;
							case 3:
								tv_email.setText(content);
//								if(null == content || "".equals(content.trim())){
//									iv_mail.setBackgroundResource(R.drawable.pre_email_disable);
//								}else{
//									iv_mail.setBackgroundResource(R.drawable.pre_email);
//								}
								userBean.setEmail(content);
								break;
							case 4:
								tv_address.setText(content);
								if(null == content || "".equals(content.trim())){
									iv_addre.setBackgroundResource(R.drawable.pre_address_disable);
									tv_address.setAlpha(0.5f);
									tv_address.setTextColor(getResources().getColor(R.color.color_gray));
								}else{
									tv_address.setAlpha(1f);
									tv_address.setTextColor(getResources().getColor(R.color.bar_home_bg));
									iv_addre.setBackgroundResource(R.drawable.pre_address);
								}
								
								userBean.setOfficeAddress(content);
								break;
							case 5:
								tv_en_name.setText(content);
								if(null == content || "".equals(content.trim())){
									tv_en_name.setAlpha(0.5f);
									tv_en_name.setTextColor(getResources().getColor(R.color.color_gray));
								}else{
									tv_en_name.setAlpha(1f);
									tv_en_name.setTextColor(getResources().getColor(R.color.bar_home_bg));
								}
								userBean.setUserEnName(content);
								break;
						}
					}else{
						DialogUtil.showLongToast(getActivity(), R.string.edit_failed);
					}
					break;
				case 3:
					DialogUtil.showLongToast(getActivity(), R.string.unnecessary_update);
					break;
				case 4:
					DialogUtil.showLongToast(getActivity(), R.string.cancle_update);
					break;
			}
		};
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.activity_personal_center, null);
		initView();
		initListener();
		createFilter();
		imageLoader = new ImageLoaderUtil(getActivity()); 
		if (savedInstanceState == null) {
	        getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
	    }
		
		String info = getInfo();
		operProfile(info);
		
		return view;
	}
	
	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(iReceiver);
		super.onDestroy();
	}
	
	private void createFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BroadcastNotice.UPDATE_PROCEDURE);
		filter.addAction(BroadcastNotice.CANCLE_UPDATE_PROCEDURE);
		iReceiver = new IncomingReceiver();
		// ????????????
		getActivity().registerReceiver(iReceiver, filter);
	}
	
	private class IncomingReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();  
	        if (action.equals(BroadcastNotice.UPDATE_PROCEDURE)) {
	        	mHandler.sendEmptyMessage(3);
	        } else if(action.equals(BroadcastNotice.CANCLE_UPDATE_PROCEDURE)){
	        	mHandler.sendEmptyMessage(4);
	        }
		}
	}

	/**
	 * ?????????: initView() 
	 * ??? ??? : ????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initView() {
		btn_back = (Button) view.findViewById(R.id.btn_back);
		rl_en_name = (RelativeLayout) view.findViewById(R.id.rl_en_name);
		rl_land_num = (RelativeLayout) view.findViewById(R.id.rl_land_num);
		rl_address = (RelativeLayout) view.findViewById(R.id.rl_address);
		rl_version = (RelativeLayout) view.findViewById(R.id.rl_version);
		rl_logout = (RelativeLayout) view.findViewById(R.id.rl_logout);
		tv_logout = (TextView) view.findViewById(R.id.tv_logout);
		
		tv_name = (TextView) view.findViewById(R.id.tv_name); 
		tv_dep = (TextView) view.findViewById(R.id.tv_dep);
		tv_office = (TextView) view.findViewById(R.id.tv_office);
		tv_land_num = (TextView) view.findViewById(R.id.tv_land_num);
		tv_mob_num = (TextView) view.findViewById(R.id.tv_mob_num);
		tv_email = (TextView) view.findViewById(R.id.tv_email);
		tv_address = (TextView) view.findViewById(R.id.tv_address);
		tv_feedback = (TextView) view.findViewById(R.id.tv_feedback);
		tv_version_show = (TextView) view.findViewById(R.id.tv_version_show);
		tv_upgrade = (TextView) view.findViewById(R.id.tv_upgrade);
		tv_en_name = (TextView) view.findViewById(R.id.tv_en_name);
		
		iv_en_name = (ImageView) view.findViewById(R.id.iv_en_name);
		iv_land_pho = (ImageView) view.findViewById(R.id.iv_land_pho);
		iv_mob = (ImageView) view.findViewById(R.id.iv_mob);
		iv_mail = (ImageView) view.findViewById(R.id.iv_mail);
		iv_addre = (ImageView) view.findViewById(R.id.iv_addre);
		
		try {
			tv_version_show.setText(SystemUtil.getVersionName(getActivity()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		iv_head_img = (ImageView) view.findViewById(R.id.iv_head_img);
		
		tv_name.setText(BaseApp.user.getUserName());
		tv_feedback.setText(getString(R.string.use_feedback));
		
		if(BaseApp.upgrade){
			tv_upgrade.setVisibility(View.VISIBLE);
		}else{
			tv_upgrade.setVisibility(View.GONE);
		}
		
		res = getResources();
	}
	
	/**
	 * @Title: operProfile
	 * @Description: ??????????????????
	 * @return void ??????????????? 
	 * @throws
	 */
	private void operProfile(String info) {
		if (!NetWorkUtil.isNetworkAvailable(getActivity())) {            // ????????????????????????
			DialogUtil.showLongToast(getActivity(), R.string.global_network_disconnected);
		}
		if (null != info) {
			SendRequest.submitRequest(getActivity(), info, submitCallBack);
		}
	}	

	/**
	 * @Title: getInfo
	 * @Description: ??????????????????
	 * @param void
	 * @return String ??????????????? 
	 * @throws
	 */
	private String getInfo() {
		ReqBodyBean bBean = new ReqBodyBean();
		bBean.setInvokeFunctionCode("F30000002");
		bBean.setInvokeParameter("[\""+BaseApp.user.getUserAccount()+"\"]");
		
		PublicRequestBean bean = new PublicRequestBean();
		bean.setReqHeader(PublicResHeaderUtils.getReqHeader());
		bean.setReqBody(bBean);
		
		return JSON.toJSONString(bean);
	}
	
	/**
	 * @Title: updateInfo
	 * @Description: ??????????????????
	 * @param void
	 * @return String ??????????????? 
	 * @throws
	 */
	private String updateInfo(int checkVal) {
		ReqBodyBean bBean = new ReqBodyBean();
		bBean.setInvokeFunctionCode("F30000004");
		bBean.setInvokeParameter("[\""+checkVal+"\",\""+content+"\"]");
				
		PublicRequestBean bean = new PublicRequestBean();
		bean.setReqHeader(PublicResHeaderUtils.getReqHeader());
		bean.setReqBody(bBean);
		
		return JSON.toJSONString(bean);
	}
	
	/**
	 * ?????????: initListener() 
	 * ??? ??? : ??????????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initListener() {
		btn_back.setOnClickListener(this);
		//iv_head_img.setOnClickListener(this);//???????????????????????? ljw20160128
		rl_land_num.setOnClickListener(this);
		rl_address.setOnClickListener(this);
		rl_version.setOnClickListener(this);
		tv_feedback.setOnClickListener(this);
		rl_logout.setOnClickListener(this);
		rl_en_name.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_logout:	// ??????????????????
			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setTitle(getString(R.string.logout));
			dialog.setMessage(R.string.is_logout);
			dialog.setPositiveButton(getString(R.string.global_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// ???????????????
					Intent mIntent = new Intent(BroadcastNotice.USER_EXIT);
					getActivity().sendBroadcast(mIntent);
				}
			}).setNegativeButton(getString(R.string.global_cancel), null);
			
			dialog.show(); 
			break;
		case R.id.iv_head_img:		// ???????????? //???????????????????????? ljw20160128
			// ?????????SelectPicPopupWindow
			//mPopwinPerCenter = new PopwinPerCenter(getActivity(), itemsOnClick);
			// ?????????????????????layout???PopupWindow??????????????????
			//mPopwinPerCenter.showAtLocation(getView().findViewById(R.id.rl_show), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); 
			break;
		case R.id.rl_land_num:	// ??????????????????
			typeClass = 1;
			openDialog();
			break;
		case R.id.rl_address:	// ??????????????????
			typeClass = 4;
			openDialog();
			break;
		case R.id.rl_version:	// ????????????
			SendRequest.getSubmitRequest1(getActivity(), "MO", "1", submitVersionCallBack, LocalConstants.GET_APP_VERSION_SERVER);
			break;
		case R.id.rl_en_name:	// ???????????????
			typeClass = 5;
			openDialog();
			break;
	}
	}
	
	/** ???????????? */
	private void openDialog() {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_per_center, null);
		final EditText et_edit = (EditText) view.findViewById(R.id.et_edit);
		String titleName = "";
		switch (typeClass) {
		case 1:
			titleName = getString(R.string.modify_office_phone);
			if (null != userBean) {
				et_edit.setText(userBean.getOfficeTel());
			}
			break;
		case 2:
			titleName = getString(R.string.modify_mobile_number);
			if (null != userBean) {
				et_edit.setText(userBean.getMobilePhone());
			}
			break;
		case 3:
			titleName = getString(R.string.modify_email);
			if (null != userBean) {
				et_edit.setText(userBean.getEmail());
			}
			break;
		case 4:
			titleName = getString(R.string.modify_address);
			if (null != userBean) {
				et_edit.setText(userBean.getOfficeAddress());
			}
			break;
		case 5:
			titleName = getString(R.string.modify_en_name);
			if (null != userBean) {
				et_edit.setText(userBean.getUserEnName());
			}
			break;
		}
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setTitle(titleName);
		dialog.setView(view);
		dialog.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				content = et_edit.getText().toString();
//				if(null == content || "".equals(content.trim())){
//					LogUtil.promptInfo(getActivity(), getString(R.string.content_null));
//					return;
//				}
//				
				boolean bl = false;
				if (typeClass == 1) {
					if (3 < content.length() && 13 > content.length() && TextUtils.isDigitsOnly(content)) {
						bl = true;
					}else if(null == content || "".equals(content.trim())){
						bl = true;
					}
				} else if (typeClass == 2) {
					if (StringUtil.checkMobilePhone(content)) {
						bl = true;
					}else if(null == content || "".equals(content.trim())){
						bl = true;
					}
				} else if (typeClass == 3) {
					if (StringUtil.checkEmail(content)) {
						bl = true;
					}else if(null == content || "".equals(content.trim())){
						bl = true;
					}
				} else if (typeClass == 4) {
					if (null != content && !"".equals(content.trim())) {
						bl = true;
					}else if(null == content || "".equals(content.trim())){
						bl = true;
					}
				}else if (typeClass == 5) {
					if (null != content || !"".equals(content.trim())) {
						bl = true;
					}else if(null == content || "".equals(content.trim())){
						bl = true;
					}
				}
				
				if (!bl) {
					LogUtil.promptInfo(getActivity(), getString(R.string.global_input_format_illgal));
				}else{
					operType = 2;
					int checkVal = 0;
					if(5 == typeClass){
						checkVal = 1;
					}else if(4 == typeClass){
						checkVal = 3;
					}else if(1 == typeClass){
						checkVal = 2;
					}
					String info = updateInfo(checkVal);
					operProfile(info);
				}
			}
		}).setNegativeButton(getString(R.string.global_cancel), null);
		
		dialog.show(); 
	}

	/** ?????????????????????????????? */
	private OnClickListener itemsOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//mPopwinPerCenter.dismiss();
			switch (v.getId()) {
				case R.id.btn_take_photo:		// ??????
					Intent intent_tp = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					// ???????????????????????????????????????
					if (!PHOTO_DIR.exists()) {
						PHOTO_DIR.mkdirs();	// ???????????????????????????
					}
					tempFile = new File(PHOTO_DIR, getPhotoFileName());
					tempuri = Uri.fromFile(tempFile);
					intent_tp.putExtra(MediaStore.EXTRA_OUTPUT, tempuri);
					startActivityForResult(intent_tp, CAMERA_WITH_DATA);
					break;
				case R.id.btn_pick_photo:		// ??????
					chooseImage();
					break;
			}            
		}
	};
	
	/** ?????????????????????????????????????????????????????????*/
	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}
	
	/**
	 * http????????????
	 */
	RequestCallBack<String> submitVersionCallBack = new RequestCallBack<String>() {

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
				ReqMessage msg = JSON.parseObject(result, ReqMessage.class);
				if (null != msg) {
					if (null != msg.getResCode() && "1".equals(msg.getResCode().trim())) {
						if (null != msg.getMessage()) {
							AutoUpdateProcedureBean bean = JSON.parseObject(msg.getMessage(), AutoUpdateProcedureBean.class);
							UpdateProcedureUtil mUpdate = new UpdateProcedureUtil(getActivity());
							boolean val = false;
							if(null != bean.getIsNeedUpdate() && !"".equals(bean.getIsNeedUpdate().trim()) && "true".equals(bean.getIsNeedUpdate().trim())){
								val = true;
							}
							boolean bl = mUpdate.checkUpdateInfo(bean.getDownloadUrl(), bean.getVersionCode(), val);
							if(bl){
								tv_upgrade.setVisibility(View.VISIBLE);
							}else{
								tv_upgrade.setVisibility(View.GONE);
							}
							return;
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
	 * http????????????
	 */
	RequestCallBack<String> submitCallBack = new RequestCallBack<String>() {
		
		@Override
		public void onStart() {
			DialogUtil.showWithCancelProgressDialog(getActivity(), null, getString(R.string.global_prompt_message), null);
		}

		@Override
		public void onLoading(long total, long current, boolean isUploading) {

		}
		
		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {                                     
			DialogUtil.closeDialog();
			String result = responseInfo.result.toString();
			byte[] ebase64 = EncryptUtil.decodeBase64(result);
			
			if (null != result && !"".equals(result.trim())) {
				PublicResultBean resMsg = JSON.parseObject(new String(ebase64), PublicResultBean.class);
				// ????????????????????????????????????
				if (null == resMsg || null == resMsg.getResHeader().getStateCode() || "".equals(resMsg.getResHeader().getStateCode())) {				
					LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", res));
					return;
				// ???????????????
				}else if (!"1".equals(resMsg.getResHeader().getStateCode())) {
					LogUtil.promptInfo(getActivity(), resMsg.getResHeader().getReturnMsg());	
					return;
				}
				// ???????????????????????????
				if (null == resMsg.getResBody().getResultString() || "".equals(resMsg.getResBody().getResultString().trim())) {		 
					LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", res));
				} else {
					if(null != resMsg.getResHeader().getSectetKey() && !"".equals(resMsg.getResHeader().getSectetKey().trim())){
						BaseApp.key = resMsg.getResHeader().getSectetKey();
					}
					// ?????????????????????????????????
					Message msgs = new Message();
					msgs.what = operType;
					msgs.obj = resMsg.getResBody().getResultString();
					mHandler.sendMessage(msgs);
				}
			} else {
				LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", res));
			}		
		}

		@Override
		public void onFailure(HttpException error, String msg) {
			DialogUtil.closeDialog();
			if(null != msg && msg.equals(LocalConstants.API_KEY)){
				LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("1207", getActivity().getResources()));
			}else{
				LogUtil.promptInfo(getActivity(), ErrorCodeContrast.getErrorCode("0", getActivity().getResources()));
			}
		}
	};
	/**
	 * ?????????: editPoto() 
	 * ??? ??? : ????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void editPoto(Uri u){
		Intent intent = new Intent("com.android.camera.action.CROP"); // ??????
		intent.setDataAndType(u, "image/*");
		intent.putExtra("scale", true);
		// ??????????????????
		intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // ????????????????????????
        intent.putExtra("outputX", 300);
	    intent.putExtra("outputY", 300);
	    intent.putExtra("return-data", true);
	    
		startActivityForResult(intent, CROP_PHOTO); //???????????????????????????????????????
	}
	
	/**
	 * ?????????: chooseImage() 
	 * ??? ??? : ?????????????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	public void chooseImage() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
        	Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    		intent.addCategory(Intent.CATEGORY_OPENABLE);  
    		intent.setType("image/*");
            startActivityForResult(intent, SELECT_PIC_KITKAT);		// 4.4??????
        } else {
        	Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
    		intent.addCategory(Intent.CATEGORY_OPENABLE);  
    		intent.setType("image/*");
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA); 	// 4.4????????????
        }
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		;
		getActivity();
		if(resultCode != Activity.RESULT_OK){
			return;
		}
		
		switch (requestCode) {
			case CAMERA_WITH_DATA:			// ????????????
				//?????????????????? 
				Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				intentBc.setData(tempuri);     
				getActivity().sendBroadcast(intentBc);  
				editPoto(tempuri);
				break;
			case PHOTO_PICKED_WITH_DATA:	// ?????????????????????
				Uri u = data.getData();  
				 if(null != u){
					 editPoto(u);
				 }
				break;
			case SELECT_PIC_KITKAT:	// 4.4???????????????
                    Uri selectedImage = data.getData();
                    String imagePath = ImageUtil.getPath(getActivity(), selectedImage);  // ???????????????????????????
                    Uri newUri = Uri.parse("file:///" + imagePath); 			// ????????????????????????URL
                    editPoto(newUri);
                    break;
			case CROP_PHOTO:				// ??????
				Bundle extras = data.getExtras();
				try {	 
					if (null != extras) {
						bitmap = extras.getParcelable("data");
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);	// (0-100)????????????
						byte[] datas = baos.toByteArray();
						// ????????????
						Intent mIntent = new Intent(getActivity(), EditHeadImgActivity.class);
						mIntent.putExtra("bitmap", datas);
						startActivityForResult(mIntent, UPDATE_IMG);
					}
					
				} catch(Exception e) {
					e.printStackTrace();
				}
				break;
			case UPDATE_IMG:				// ????????????
				iv_head_img.setImageBitmap(bitmap);
				break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
		
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        	
        }

		@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_peronal, container, false);
            return rootView;
        }
    }
}
