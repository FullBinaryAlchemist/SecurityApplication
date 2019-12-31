package com.example.securityapplication;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.DrawableWrapper;
import android.net.Uri;
import android.os.Build;

import android.os.Bundle;
import android.os.Environment;

import android.os.StrictMode;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.example.securityapplication.Helper.FirebaseHelper;
import com.example.securityapplication.Helper.InternalStorage;
import com.example.securityapplication.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;


import java.io.File;
import java.util.ArrayList;
import java.util.List;


import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.securityapplication.R.layout.spinner_layout;
import static com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND;

public class profile_fragment extends Fragment {

    private EditText textName,textEmail,textPhone,textDob;
    private AutoCompleteTextView textAddress;
    private Button btn_edit;
    private Button btn_logout;
    private TextView text_changePassword;
    SQLiteDBHelper mydb ;
    User user;
    private TelephonyManager telephonyManager;
    private String mImeiNumber;
    private int RC;
    private String TAG = "ProfileActivity";
    Spinner spinner;
    DatePickerDialog datePickerDialog;
    private FirebaseHelper firebaseHelper;

    private CircleImageView profile_pic;
    private ImageButton chooseImgBtn;
    File f;

    private Uri filePath, camerafilepath;
    private  Bitmap bitmappic,bit_image;
    Uri imguri;

    private  Intent CropIntent;
    private final int PICK_IMAGE_REQUEST = 71;
    private final int TAKE_PICTURE = 81;
    private  ProgressDialog progressDialog;
    // InternalStorage
    private InternalStorage internalStorage;
    StrictMode.VmPolicy.Builder builder;

    Uri picUri;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 107;
    private final static int IMAGE_RESULT = 200;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_profile,container,false);

        String [] values =
                {"Male","Female","Others"};
        spinner = v.findViewById(R.id.text_Gender);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), spinner_layout, values);
        adapter.setDropDownViewResource(spinner_layout);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //builder = new StrictMode.VmPolicy.Builder();
        //StrictMode.setVmPolicy(builder.build());
        LinearLayout linearLayout = getActivity().findViewById(R.id.anim_back);
        AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
        initObjects();
        initviews();
//        FetchAllData();
        DisplayData();
        initListeners();
        deviceId();




    }

    private void initObjects() {

//        user = getIntent().getParcelableExtra("User");
        user = UserObject.user;
        mydb = SQLiteDBHelper.getInstance(getContext());
        /**  Get FirebaseHelper Instance **/
        firebaseHelper = FirebaseHelper.getInstance();
        firebaseHelper.initFirebase();
        firebaseHelper.initContext(getActivity());
        firebaseHelper.initGoogleSignInClient(getString(R.string.server_client_id));

        deviceId();
        internalStorage = InternalStorage.getInstance();
        internalStorage.initContext(getContext());
    }

    private void initListeners() {


        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    /*Dialog builder = new Dialog(this);
                    builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    builder.getWindow().setBackgroundDrawable(
                            new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            //nothing;
                        }
                    });

                    ImageView imageView = new ImageView(this);
                    imageView.setImageURI(imageUri);
                    builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    builder.show();*/
                final ImagePopup imagePopup = new ImagePopup(getContext());
                imagePopup.setWindowHeight(800); // Optional
                imagePopup.setWindowWidth(800); // Optional
                imagePopup.setBackgroundColor(Color.BLACK);  // Optional
                imagePopup.setFullScreen(true); // Optional
                imagePopup.setHideCloseIcon(true);  // Optional
                imagePopup.setImageOnClickClose(true);  // Optional
                imagePopup.setFadingEdgeLength(1000);
                imagePopup.setClickable(true);
                imagePopup.setKeepScreenOn(true);
                imagePopup.setPressed(true);
                imagePopup.setTop(100);
                imagePopup.setBottom(100);
                imagePopup.setEnabled(true);





                imagePopup.initiatePopup(profile_pic.getDrawable()); // Load Image from Drawable
                imagePopup.viewPopup();


            }
        });




        textDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c= java.util.Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                textDob.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
                datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
                                        @Override public void onClick(View view) {
                                            Animation edit_anim= AnimationUtils.loadAnimation(getContext(),R.anim.btn_anim);
                                            btn_edit.startAnimation(edit_anim);

                                            if (IsInternet.isNetworkAvaliable(getContext())) {

                                                if(btn_edit.getText().equals("edit"))
                                                {btn_edit.setText("Save");
                                                    enable();
                                                    btn_edit.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                                alphaa(1.0f);}
                                                else {
                                                    if(!validate())
                                                    {
                                                        Toasty.error(getContext(), "Please Enter Valid Information", Toast.LENGTH_SHORT, true).show();

                                                        //Toast.makeText(getContext(), "Please Enter Valid Information", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else {
                                                        // start progress bar
                                                        progressDialog = new ProgressDialog(getContext(),R.style.MyAlertDialogStyle);
                                                        progressDialog.setTitle("Saving data...");
                                                        progressDialog.show();
                                                        progressDialog.setMessage("validating....");
                                                        progressDialog.setCancelable(false);

                                                        //save code will come here
                                                        user.setName(textName.getText().toString().trim());
                                                        textName.setText(textName.getText().toString().trim());
                                                        user.setDob(textDob.getText().toString());
                                                        user.setLocation(textAddress.getText().toString().trim());
                                                        textAddress.setText(textAddress.getText().toString().trim());
                                                        if (spinner.getSelectedItemPosition() == 0)
                                                            user.setGender("male");
                                                        else if (spinner.getSelectedItemPosition() == 1)
                                                            user.setGender("female");
                                                        else
                                                            user.setGender("others");
                                                        user.setMobile(textPhone.getText().toString());
                                                        updateUser();
                                                    }
                                                }
                                            }//Sending Data to EditProfileActivity
                                            else {
                                                Toasty.error(getContext(), "Please check your Internet Connectivity", Toast.LENGTH_LONG, true).show();

                                                //Toast.makeText(getContext(), "Please check your Internet Connectivity", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                    }
        );

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Animation log_anim=AnimationUtils.loadAnimation(getContext(),R.anim.btn_anim);
                btn_logout.startAnimation(log_anim);
                //Toasty.info(getContext(), "clicked", Toast.LENGTH_SHORT, true).show(); //REMOVED

                Log.d("signout","signout happen");
                signOut();
            }
        });

        text_changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword(firebaseHelper.getFirebaseAuth().getCurrentUser().getEmail());
            }
        });

        chooseImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivityForResult(getPickImageChooserIntent(), IMAGE_RESULT);
            }
        });
        permissions.add(CAMERA);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(READ_EXTERNAL_STORAGE);
        permissionsToRequest = findUnAskedPermissions(permissions);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
    }




    public Intent getPickImageChooserIntent() {

        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getActivity().getPackageManager();

        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }


    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getActivity().getExternalFilesDir("");
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }


    private boolean validate() {
        if(textName.getText().toString().trim().length()>1 && textAddress.getText().toString().length()>1 && textPhone.getText().toString().length()==10) {
            return Pattern.matches("[ a-zA-Z]+", textName.getText().toString().trim());
        }
        else
            return false;
    }

//    private void FetchAllData(){
//        int i =0;
//        Cursor res;
//        res = mydb.getAllData();
//        if (res.getCount() == 0){
//            Toast toast = Toast.makeText(getContext(),
//                    "No User Data Found",
//                    Toast.LENGTH_LONG);
//            toast.show();
//            Log.d("Profile","No Data found");
//        }
////        StringBuffer buffer = new StringBuffer();
//        while (res.moveToNext()){
//            user.setName(res.getString(1));
//            user.setEmail(res.getString(2));
//            user.setGender(res.getString(3));
//           user.setMobile(res.getString(4));
////            ansAadhaar = res.getString(6);
//            user.setLocation(res.getString(5));
//            user.setDob(res.getString(6));
//            i++;
//            Log.d("Profile Activity","User Object set in Profile activity successfully" +i);
//        }
//    }

    private void DisplayData() {

        textName.setText(user.getName());
//        textAadhaar.setText(ansAadhaar);
        textDob.setText(user.getDob());
        int kk=0;
        if(user.getGender().equalsIgnoreCase("male"))
            kk=0;
        else if(user.getGender().equalsIgnoreCase("female"))
            kk=1;
        else
            kk=2;
        spinner.setSelection(kk);
        textAddress.setText(user.getLocation());
        textEmail.setText(user.getEmail());
        textPhone.setText(user.getMobile());

        // display image from internal storage
        //File imgPath = internalStorage.getImagePathFromStorage(user.getEmail());
        f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Trata/"+user.getEmail()+"/");
        File imgPath = new File(f.getAbsolutePath(), "profile.jpeg");
       // File imgPath = new File(f.getAbsolutePath(), "profile.jpeg");
        try{
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(imgPath));
            profile_pic = getActivity().findViewById(R.id.profile_pic);
            //ImageButton imageButton = (ImageButton) profile_pic;
            profile_pic.setImageBitmap(b);

        }catch (IOException e){
            //Toast.makeText(getContext(), "Profile picture not found", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Profile picture not found");
        }

        Log.d("Profile","DATA displayed on profile Successfully");
    }
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    private void disable(){
        textName.setEnabled(false);
        spinner.setEnabled(false);
        textEmail.setEnabled(false);
        textPhone.setEnabled(false);
        textAddress.setEnabled(false);
        chooseImgBtn.setVisibility(View.INVISIBLE);
        textDob.setEnabled(false);
        textName.setBackgroundColor(Color.TRANSPARENT);
        textEmail.setBackgroundColor(Color.TRANSPARENT);
        textPhone.setBackgroundColor(Color.TRANSPARENT);
        textAddress.setBackgroundColor(Color.TRANSPARENT);
        textDob.setBackgroundColor(Color.TRANSPARENT);
    }
    private void alphaa(float k){
        spinner.setAlpha(k);
        textName.setAlpha(k);
        textPhone.setAlpha(k);
        textAddress.setAlpha(k);
        textDob.setAlpha(k);
        textName.setBottom(Color.BLACK);
    }
    private void enable(){
        spinner.setEnabled(true);
        textName.setEnabled(true);
        //chooseImgBtn.setVisibility(View.VISIBLE);
        textPhone.setEnabled(true);
        textAddress.setEnabled(true);
        textDob.setEnabled(true);

        chooseImgBtn.setVisibility(View.VISIBLE);
        textEmail.setBackgroundResource(R.drawable.blackborder);
        textPhone.setBackgroundResource(R.drawable.blackborder);
        textAddress.setBackgroundResource(R.drawable.blackborder);
        textDob.setBackgroundResource(R.drawable.blackborder);
    }

    private void initviews() {
        textName =getActivity().findViewById(R.id.text_Name);
        textEmail = getActivity().findViewById(R.id.text_Email);
        textPhone = getActivity().findViewById(R.id.text_Phone);
        textAddress = getActivity().findViewById(R.id.text_Address);
        //textGender = getActivity().findViewById(R.id.text_Gender);
        textDob = getActivity().findViewById(R.id.text_DOB);
        btn_edit = getActivity().findViewById(R.id.btn_Edit);
        btn_logout = getActivity().findViewById(R.id.btn_Logout);
        Resources res = getResources();
        String[] Locality = res.getStringArray(R.array.Locality);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,Locality);
        textAddress.setAdapter(adapter);
        text_changePassword = getActivity().findViewById(R.id.text_changePassword);
        profile_pic = getActivity().findViewById(R.id.profile_pic);
        chooseImgBtn = getActivity().findViewById(R.id.btn_choose_img);

        disable();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == Activity.RESULT_OK) {



            if (requestCode == IMAGE_RESULT) {

                String filePath = getImageFilePath(data);
                if (filePath != null) {
                    Bitmap d = BitmapFactory.decodeFile(filePath);
//                    int m=d.getWidth();
//                    int k=d.getWidth();
//                    int l=d.getHeight();
//                    if(k<l)
//                        m=k;
//                    else
//                        m=l;
//
//                    d=Bitmap.createBitmap(d,0,0,d.getWidth(),m);
                    Bitmap output;
                    try {
                        if (d.getWidth() >= d.getHeight())
                            output = Bitmap.createBitmap(d, d.getWidth() / 2 - d.getHeight() / 2, 0, d.getHeight(), d.getHeight());
                        else
                            output = Bitmap.createBitmap(d, 0, d.getHeight() / 2 - d.getWidth() / 2, d.getWidth(), d.getWidth());
                        //Bitmap output=getResizedBitmap(d,100);
                        Bitmap bit2 = ExifUtils.rotateBitmap(filePath, output);
                        int nh = (int) (output.getHeight() * (512.0 / output.getWidth()));
                        bit2 = Bitmap.createScaledBitmap(bit2, 512, nh, true);
                        profile_pic.setImageBitmap(bit2);
                    }catch (Exception e){
                        Toast.makeText(getContext(), "Please  upload image of less size", Toast.LENGTH_SHORT).show();
                    }
                    //save to internal storage code come here



                    uploadProfilePicToFirebase();
                }
            }

        }

    }
    private String getImageFromFilePath(Intent data) {
        boolean isCamera = data == null || data.getData() == null;

        if (isCamera) return getCaptureImageOutputUri().getPath();
        else return getPathFromURI(data.getData());

    }

    public String getImageFilePath(Intent data) {
        return getImageFromFilePath(data);
    }

    private String getPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }



    private void deviceId() {
        telephonyManager = (TelephonyManager) getActivity().getSystemService(getContext().TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 101);
            return;
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mImeiNumber = telephonyManager.getImei(0);
                Log.d("IMEI", "IMEI Number of slot 1 is:" + mImeiNumber);
            }
            else {
                mImeiNumber = telephonyManager.getDeviceId();
            }
        }

        //Log.d("MAinActivity","SMS intent");
        //check permissions
        checkCameraPermission();
        checkSMSPermission();
    }

    public  boolean checkSMSPermission(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            Toasty.error(getContext(), "Permission Required for sending SMS in case of SOS", Toast.LENGTH_SHORT, true).show();

            //Toast.makeText(getContext(), "Permission Required for sending SMS in case of SOS", Toast.LENGTH_LONG).show();
            Log.d("MainActivity", "PERMISSION FOR SEND SMS NOT GRANTED, REQUESTING PERMSISSION...");
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.SEND_SMS}, RC);
        }

        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED;

    }

    public  boolean checkCameraPermission(){

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;

    }



    private void signOut(){
        Log.d(TAG,"Inside signout");
        if (mImeiNumber == null) {
            deviceId();
            return;
        }

        if (!IsInternet.checkInternet(getContext()))
            return;

        firebaseHelper.firebaseSignOut(mImeiNumber);
        firebaseHelper.googleSignOut(getActivity());
        //delete user records from SQLite
        mydb.deleteDatabase(Objects.requireNonNull(getContext()).getApplicationContext());

        try{
            Intent mStopSosPlayer=new Intent(getContext(),SosPlayer.class);
            mStopSosPlayer.putExtra("stop",1);
            getActivity().startService(mStopSosPlayer); //previously was stopService(). Now using startService() to use the stop extra in onStartCommand()
            Log.d("Profile Fragment","Service sosplayer new startIntent...");

            //to stop GetGPS Service upon logout
            Intent MStopGPSService = new Intent(getContext(),GetGPSCoordinates.class);
            getActivity().stopService(MStopGPSService);
            Log.d("Profile Fragment","GPS Service Stopped");
            Toasty.warning(getContext(), "Services Stopped", Toast.LENGTH_SHORT, true).show(); //TODO:Remove toasty

           // Toast.makeText(getContext(),"Service sosplayer stopping...",Toast.LENGTH_SHORT).show();
        }
        catch(Exception e) {
            Log.d("Profile Fr","Service SOSplayer is not running");
        }
        //finishing the navigation activity
        getActivity().finish();
        //Clear the back stack and re-directing to the sign-up page
        Intent mLogOutAndRedirect= new Intent(getContext(),MainActivity.class);
        mLogOutAndRedirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mLogOutAndRedirect);
    }

    private void updateUser(){

        Log.d(TAG,"Updating user...");
        mydb.updateUser(user);
        firebaseHelper.updateuser_infirebase(FirebaseAuth.getInstance().getUid(),user);

        // stop progress bar
        progressDialog.dismiss();

        btn_edit.setText("edit");
        alphaa(0.6f);
        btn_edit.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_edit_black_24dp, 0, 0, 0);
        disable();
    }

    private void changePassword(String email){
        if (!IsInternet.checkInternet(getContext()))
            return;

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Sending Email...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        try {
            firebaseHelper.getFirebaseAuth().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        try{Toasty.success(getActivity(), "EMAIL SENT. PLEASE CHECK YOUR MAIL TO CHANGE PASSWORD", Toast.LENGTH_SHORT, true).show();}
                        catch(Exception e){Log.d(TAG,"toast exception:"+e.getMessage());}
                    }
                    else
                    {
                        try {
                            throw task.getException();
                        }catch (Exception e){
                            Log.d(TAG,e.getMessage());
                            try{Toasty.info(getActivity(), "You need to sign in again to change password", Toast.LENGTH_LONG, true).show();}
                            catch(Exception e1){Log.d(TAG, "toast exception:"+e1.getMessage());}
                        }
                    }
                }
            });
        }catch (Exception e){
            progressDialog.dismiss();
            Log.d(TAG, e.getMessage());
            Toasty.info(getActivity(), "You need to sign in again to change password", Toast.LENGTH_LONG, true).show();

//            Toast.makeText(getActivity(),"You need to sign in again to change password",Toast.LENGTH_LONG).show();
        }
    }

    private void uploadProfilePicToFirebase(){

        final ProgressDialog progressDialog = new ProgressDialog(getContext(),R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        // Get the data from an ImageView as bytes
        profile_pic.setDrawingCacheEnabled(true);
        profile_pic.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profile_pic.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,40, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = firebaseHelper.getStorageReference().child("images/profile_pic");
        UploadTask uploadTask = ref.putBytes(data);
        uploadTask
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(),"Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to upload image"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100f*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    }
                });
    }

    private void deleteExistingProfilePic(){
        final ProgressDialog progressDialog = new ProgressDialog(getContext(),R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Processing...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        // Create a storage reference from our app
        StorageReference storageRef = firebaseHelper.getStorageReference();

        // Create a reference to the file to delete
        StorageReference imgRef = storageRef.child("images/profile_pic");

        // Delete the file
        imgRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                progressDialog.dismiss();
                uploadProfilePicToFirebase();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                if (((StorageException) exception).getErrorCode() == ERROR_OBJECT_NOT_FOUND){
                    progressDialog.dismiss();
                    uploadProfilePicToFirebase();
                }
                else
                    Toasty.error(getContext(), "Failed to upload image"+exception.getMessage(), Toast.LENGTH_LONG, true).show();
                //Toast.makeText(getContext(), "Failed to upload image"+exception.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }


}