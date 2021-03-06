package com.k2.mobile.app.controller.activity.menu.personalCenter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.k2.mobile.app.R;
import com.k2.mobile.app.common.config.BroadcastNotice;
import com.k2.mobile.app.common.config.LocalConstants;
import com.k2.mobile.app.common.exception.HttpException;
import com.k2.mobile.app.controller.activity.menu.feedback.FeedbackActivity;
import com.k2.mobile.app.controller.core.BaseActivity;
import com.k2.mobile.app.controller.core.BaseApp;
import com.k2.mobile.app.model.bean.PublicBean;
import com.k2.mobile.app.model.bean.ReqMessage;
import com.k2.mobile.app.model.bean.ResPublicBean;
import com.k2.mobile.app.model.bean.User;
import com.k2.mobile.app.model.http.ResponseInfo;
import com.k2.mobile.app.model.http.callback.RequestCallBack;
import com.k2.mobile.app.model.http.other.SendRequest;
import com.k2.mobile.app.utils.DialogUtil;
import com.k2.mobile.app.utils.EncryptUtil;
import com.k2.mobile.app.utils.ErrorCodeContrast;
import com.k2.mobile.app.utils.FastJSONUtil;
import com.k2.mobile.app.utils.ImageLoaderUtil;
import com.k2.mobile.app.utils.ImageUtil;
import com.k2.mobile.app.utils.LogUtil;
import com.k2.mobile.app.utils.NetWorkUtil;
import com.k2.mobile.app.utils.StringUtil;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
* @Title PersonalCenterActivity.java
* @Package com.oppo.mo.controller.activity.menu;
* @Description ????????????
* @Company  K2
* 
* @author linqijun
* @date 2015-3-31 ??????9:00:57
* @version V1.0
*/
@SuppressLint("NewApi")
public class PersonalCenterActivity extends BaseActivity implements OnClickListener {
	
	// ?????????????????????
	private TextView tv_title, tv_sech;
	// ??????
	private Button btn_back;
	// ?????????, ??????, ??????, ??????, ????????????, ????????????, E-mail, ????????????, tv_feedback
	private TextView tv_name, tv_dep, tv_office, tv_land_num, tv_mob_num, tv_email, tv_address, tv_feedback;
	// ??????????????????????????????E-mail???????????????
	private RelativeLayout rl_land_num, rl_address;
	// ??????
	private TextView tv_logout;
	// ??????
	private ImageView iv_head_img;
	// ????????????????????????
	//private PopwinPerCenter mPopwinPerCenter;
	// ??????????????????
	public static final File PHOTO_DIR = new File(LocalConstants.LOCAL_FILE_PATH + "/oppo/mo/poto");
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
	
	// ???????????????1???????????????2???????????????3E-mail???4????????????
	private int typeClass = 1;
	// ????????????
	private int operType = 1;
	private Bitmap bitmap = null;
	
	// ??????????????????
	public ImageLoaderUtil imageLoader;   
	
	private Handler mHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
				case 1:
					String json = (String) msg.obj;
					if (null != json) {
						userBean = JSON.parseObject(json, User.class);
						if(null != userBean) {
							tv_dep.setText(userBean.getRealityOrgName());
							tv_office.setText("("+userBean.getJobDesc()+")");
							tv_land_num.setText(userBean.getOfficeTel());
							tv_mob_num.setText(userBean.getMobilePhone());
							tv_email.setText(userBean.getEmail());
							tv_address.setText(userBean.getOfficeAddress());
							imageLoader.DisplayImage(userBean.getFileUrl(), iv_head_img,1, userBean.getSex()); // ??????????????????
						}
					}
					break;
					
				case 2:
					String jsons = (String) msg.obj;
					if(null != jsons){
						ResPublicBean reBean = JSON.parseObject(jsons, ResPublicBean.class);
						if(null != reBean && "1".equals(reBean.getSuccess())){
							DialogUtil.showLongToast(PersonalCenterActivity.this, R.string.global_edit_status_success);
							switch (typeClass) {
								case 1:
									tv_land_num.setText(content);
									break;
								case 2:
									tv_mob_num.setText(content);
									break;
								case 3:
									tv_email.setText(content);
									break;
								case 4:
									tv_address.setText(content);
									break;
							}
						}else{
							DialogUtil.showLongToast(PersonalCenterActivity.this, R.string.edit_failed);
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
		setContentView(R.layout.activity_personal_center);
		initView();
		initListener();
		BaseApp.addActivity(this);
		
		imageLoader = new ImageLoaderUtil(this); 
		if (savedInstanceState == null) {
	        getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
	    }
		
		String info = getInfo();
		operProfile(1, info);
	}

	/**
	 * ?????????: initView() 
	 * ??? ??? : ????????? 
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initView() {
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_sech = (TextView) findViewById(R.id.tv_sech);
		btn_back = (Button) findViewById(R.id.btn_back);
		rl_land_num = (RelativeLayout) findViewById(R.id.rl_land_num);
		rl_address = (RelativeLayout) findViewById(R.id.rl_address);
		tv_logout = (TextView) findViewById(R.id.tv_logout);
		
		tv_name = (TextView) findViewById(R.id.tv_name); 
		tv_dep = (TextView) findViewById(R.id.tv_dep);
		tv_office = (TextView) findViewById(R.id.tv_office);
		tv_land_num = (TextView) findViewById(R.id.tv_land_num);
		tv_mob_num = (TextView) findViewById(R.id.tv_mob_num);
		tv_email = (TextView) findViewById(R.id.tv_email);
		tv_address = (TextView) findViewById(R.id.tv_address);
		tv_feedback = (TextView) findViewById(R.id.tv_feedback);
		
		iv_head_img = (ImageView) findViewById(R.id.iv_head_img);
		
		tv_title.setText(getString(R.string.personal_center));
		tv_title.setVisibility(View.GONE);
		tv_sech.setVisibility(View.GONE);
		
		tv_name.setText(BaseApp.user.getUserName()+"/"+BaseApp.user.getUserId());
		tv_feedback.setText(getString(R.string.use_feedback));
	}
	
	/**
	 * @Title: operProfile
	 * @Description: ??????????????????
	 * @param operType ???????????????1?????????2??????
	 * @return void ??????????????? 
	 * @throws
	 */
	private void operProfile(int _operType, String info) {
		if (!NetWorkUtil.isNetworkAvailable(PersonalCenterActivity.this)) {            // ????????????????????????
			DialogUtil.showLongToast(PersonalCenterActivity.this, R.string.global_network_disconnected);
		}
		operType = _operType;
		int type = 0;
		switch (_operType) {
			case 1:		// ??????????????????
				info = getInfo();
				type = LocalConstants.PERSONAL_CENTER_DETAILS_SERVER;
				break;
			case 2:		// ??????????????????
				type = LocalConstants.PERSONAL_CENTER_MODIFY_DATA_SERVER;
				break;
		}
		if (null != info) {
			SendRequest.sendSubmitRequest(PersonalCenterActivity.this, info, BaseApp.token, BaseApp.reqLang, 
					type, BaseApp.key, submitCallBack);
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
		PublicBean bean = new PublicBean();
		bean.setDeviceId(BaseApp.macAddr);
		bean.setUserAccount(BaseApp.user.getUserId());
		String json = JSON.toJSONString(bean);
		return json;
	}
	
	/**
	 * @Title: updateInfo
	 * @Description: ??????????????????
	 * @param void
	 * @return String ??????????????? 
	 * @throws
	 */
	private String updateInfo() {
		PublicBean bean = new PublicBean();
		bean.setDeviceId(BaseApp.macAddr);
		bean.setUserAccount(BaseApp.user.getUserId());
		bean.setUpdateType(typeClass+"");
		bean.setContent(content);
		String json = JSON.toJSONString(bean);
		return json;
	}
	
	/**
	 * ?????????: initListener() 
	 * ??? ??? : ??????????????????
	 * ??? ??? : void 
	 * ?????????: void
	 */
	private void initListener() {
		btn_back.setOnClickListener(this);
		iv_head_img.setOnClickListener(this); 
		rl_land_num.setOnClickListener(this);
		rl_address.setOnClickListener(this);
		tv_feedback.setOnClickListener(this);
		tv_logout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:	// ??????
			finish();
			break;
		case R.id.tv_logout:
			Intent mIntent = new Intent(BroadcastNotice.USER_LOGOUT);
			this.sendBroadcast(mIntent);
			break;
		case R.id.iv_head_img:		// ????????????
			// ?????????SelectPicPopupWindow
			 //mPopwinPerCenter = new PopwinPerCenter(this, itemsOnClick);
			// ?????????????????????layout???PopupWindow??????????????????
			 //mPopwinPerCenter.showAtLocation(this.findViewById(R.id.rl_show), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); 
			break;
		case R.id.rl_land_num:	// ??????????????????
			typeClass = 1;
			openDialog();
			break;
		case R.id.rl_address:	// ??????????????????
			typeClass = 4;
			openDialog();
			break;
		case R.id.tv_feedback:	// ????????????????????????
			startActivity(FeedbackActivity.class);
			break;
		}
	}
	
	/** ???????????? */
	private void openDialog() {
		View view = LayoutInflater.from(this).inflate(R.layout.dialog_per_center, null);
		final EditText et_edit = (EditText) view.findViewById(R.id.et_edit);
		String titleName = "";
		switch (typeClass) {
		case 1:
			titleName = getResources().getString(R.string.modify_office_phone);
			if (null != userBean) {
				et_edit.setText(userBean.getOfficeTel());
			}
			break;
		case 2:
			titleName = getResources().getString(R.string.modify_mobile_number);
			if (null != userBean) {
				et_edit.setText(userBean.getMobilePhone());
			}
			break;
		case 3:
			titleName = getResources().getString(R.string.modify_email);
			if (null != userBean) {
				et_edit.setText(userBean.getEmail());
			}
			break;
		case 4:
			titleName = getResources().getString(R.string.modify_address);
			if (null != userBean) {
				et_edit.setText(userBean.getOfficeAddress());
			}
			break;
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(titleName);
		dialog.setView(view);
		dialog.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				content = et_edit.getText().toString();
				boolean bl = false;
				if (typeClass == 1) {
					if (StringUtil.checkTelephone(content)) {
						bl = true;
					}
				} else if (typeClass == 2) {
					if (StringUtil.checkMobilePhone(content)) {
						bl = true;
					}
				} else if (typeClass == 3) {
					if (StringUtil.checkEmail(content)) {
						bl = true;
					}
				} else if (typeClass == 4) {
					if (null != content && !"".equals(content.trim())) {
						bl = true;
					}
				}
				if (!bl) {
					LogUtil.promptInfo(PersonalCenterActivity.this, getResources().getString(R.string.global_input_format_illgal));
				}else{
					String info = updateInfo();
					operProfile(2, info);
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
	RequestCallBack<String> submitCallBack = new RequestCallBack<String>() {
		
		@Override
		public void onStart() {
			DialogUtil.showWithCancelProgressDialog(PersonalCenterActivity.this, null, getResources().getString(R.string.global_prompt_message), null);
		}

		@Override
		public void onLoading(long total, long current, boolean isUploading) {

		}
		
		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {
			DialogUtil.closeDialog();
			Message msgs = new Message();
			String result = responseInfo.result.toString();
			if (null != result && !"".equals(result.trim())) {
				ReqMessage msg = FastJSONUtil.getJSONToEntity(result, ReqMessage.class);
				// ????????????????????????????????????
				if (null == msg || null == msg.getResCode() || "".equals(msg.getResCode())) {				
					LogUtil.promptInfo(PersonalCenterActivity.this, ErrorCodeContrast.getErrorCode("0", getResources()));
				}else if ("1103".equals(msg.getResCode()) || "1104".equals(msg.getResCode())) {
					LogUtil.promptInfo(PersonalCenterActivity.this, ErrorCodeContrast.getErrorCode(msg.getResCode(), getResources()));	
					Intent mIntent = new Intent(BroadcastNotice.USER_EXIT);
					sendBroadcast(mIntent);
					return;
				}else if("1210".equals(msg.getResCode())){
					LogUtil.promptInfo(PersonalCenterActivity.this, ErrorCodeContrast.getErrorCode(msg.getResCode(), getResources()));	
					Intent mIntent = new Intent(BroadcastNotice.WIPE_USET);
					sendBroadcast(mIntent);
					return;
				}else if("1".equals(msg.getResCode()) && null != msg.getMessage()){
					// ?????????????????????????????????
					String decode = EncryptUtil.getDecodeData(msg.getMessage(), BaseApp.key);
					msgs.what = operType;
					msgs.obj = decode;
					mHandler.sendMessage(msgs);
				}else {
					LogUtil.promptInfo(PersonalCenterActivity.this, ErrorCodeContrast.getErrorCode(msg.getResCode(), getResources()));
				}
			} else {
				LogUtil.promptInfo(PersonalCenterActivity.this, ErrorCodeContrast.getErrorCode("0", getResources()));
			}
		}

		@Override
		public void onFailure(HttpException error, String msg) {
			DialogUtil.closeDialog();
			if(null != msg && msg.equals(LocalConstants.API_KEY)){
				LogUtil.promptInfo(PersonalCenterActivity.this, ErrorCodeContrast.getErrorCode("1207", getResources()));
			}else{
				LogUtil.promptInfo(PersonalCenterActivity.this, ErrorCodeContrast.getErrorCode("0", getResources()));
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode != RESULT_OK){
			return;
		}
		switch (requestCode) {
			case CAMERA_WITH_DATA:			// ????????????
				//?????????????????? 
				Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				intentBc.setData(tempuri);     
				this.sendBroadcast(intentBc);  
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
                    String imagePath = ImageUtil.getPath(this, selectedImage);  // ???????????????????????????
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
						Intent mIntent = new Intent(PersonalCenterActivity.this, EditHeadImgActivity.class);
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
