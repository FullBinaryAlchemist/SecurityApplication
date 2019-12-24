package com.example.securityapplication;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
<<<<<<< HEAD
=======
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
<<<<<<< HEAD
import android.graphics.Color;
=======
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
import android.net.Uri;
import android.os.Build;

import android.os.Bundle;
import android.os.PatternMatcher;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
<<<<<<< HEAD
=======
import android.widget.ImageButton;
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.securityapplication.Helper.FirebaseHelper;
import com.example.securityapplication.Helper.InternalStorage;
import com.example.securityapplication.model.Device;
import com.example.securityapplication.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
<<<<<<< HEAD

=======
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

<<<<<<< HEAD
=======
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.support.v4.content.ContextCompat.getSystemService;
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

    private ImageView profile_pic;
<<<<<<< HEAD
    private Button chooseImgBtn;
    private Uri filePath;

    private final int PICK_IMAGE_REQUEST = 71;
    private  ProgressDialog progressDialog;

=======
    private ImageButton chooseImgBtn;
    private Uri filePath;

    private final int PICK_IMAGE_REQUEST = 71;
    private final int TAKE_PICTURE = 81;
    private  ProgressDialog progressDialog;

    // InternalStorage
    private InternalStorage internalStorage;

>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_profile,container,false);

        String [] values =
                {"Male","Female","Others"};
        spinner = (Spinner) v.findViewById(R.id.text_Gender);
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

<<<<<<< HEAD
        deviceId();
    }

    private void initObjects() {

//        user = getIntent().getParcelableExtra("User");
        user = UserObject.user;
        mydb = SQLiteDBHelper.getInstance(getContext());
=======
        internalStorage = InternalStorage.getInstance();
        internalStorage.initContext(getContext());
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
    }

    private void initListeners() {
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

                                            if (IsInternet.isNetworkAvaliable(getContext())) {

                                                chooseImgBtn.setVisibility(View.VISIBLE);

                                                if(btn_edit.getText().equals("edit"))
                                                {btn_edit.setText("Save");
                                                    enable();
                                                    alphaa(1.0f);}
                                                else {
                                                    if(!validate())
                                                    {
                                                        Toast.makeText(getContext(), "Please Enter Valid Information", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else {
                                                        // start progress bar
                                                        progressDialog = new ProgressDialog(getContext());
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


                                                        // check mobile number in firebase
                                                        checkMobileInFirebase(textPhone.getText().toString());
                                                    }
                                                }
                                            }//Sending Data to EditProfileActivity
                                            else {
                                                Toast.makeText(getContext(), "Please check your Internet Connectivity", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                    }
        );

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
                Log.d("signout","signout happen");
<<<<<<< HEAD
                // mydb.delete_table();
=======
               // mydb.delete_table();
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
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
<<<<<<< HEAD
                // choose img from gallery
                chooseImg();
=======
                pictureChoice();
                // choose img from gallery
                chooseImg("storage");
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
            }
        });
    }

    private boolean validate() {
        if(textName.getText().toString().trim().length()>1 && textAddress.getText().toString().length()>1 && textPhone.getText().toString().length()==10) {
            if(Pattern.matches("[ a-zA-Z]+",textName.getText().toString().trim()))
                return true;
            else
                return false;
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
        File imgPath = internalStorage.getImagePathFromStorage(user.getEmail());
        try{
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(imgPath));
            profile_pic = getActivity().findViewById(R.id.profile_pic);
            profile_pic.setImageBitmap(b);
        }catch (IOException e){
            Toast.makeText(getContext(), "Profile picture not found", Toast.LENGTH_SHORT).show();
        }

        Log.d("Profile","DATA displayed on profile Successfully");
    }
    private void disable(){
        textName.setEnabled(false);
        spinner.setEnabled(false);
        textEmail.setEnabled(false);
        textPhone.setEnabled(false);
        textAddress.setEnabled(false);
        textDob.setEnabled(false);
        chooseImgBtn.setVisibility(View.GONE);
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
        chooseImgBtn.setVisibility(View.VISIBLE);
        textPhone.setEnabled(true);
        textAddress.setEnabled(true);
        textDob.setEnabled(true);
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

<<<<<<< HEAD
        profile_pic = getActivity().findViewById(R.id.img_profile);
=======
        profile_pic = getActivity().findViewById(R.id.profile_pic);
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
        chooseImgBtn = getActivity().findViewById(R.id.btn_choose_img);

        disable();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1){
            if (resultCode == 110){
                user = data.getParcelableExtra("ResultUser");
                Log.d("Profile","User object returned"+user.getEmail());
                mydb.updateUser(user);
                DisplayData();
            }
        }

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
<<<<<<< HEAD
=======
                try {
                    internalStorage.saveImageToInternalStorage(bitmap, user.getEmail());
                    profile_pic.setImageBitmap(bitmap);
                    deleteExistingProfilePic();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Unable to store image",Toast.LENGTH_SHORT).show();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        if(requestCode == TAKE_PICTURE && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
                profile_pic.setImageBitmap(bitmap);
                deleteExistingProfilePic();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
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

        while(!checkSMSPermission());
    }

    public  boolean checkSMSPermission(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getContext(), "Permission Required for sending SMS in case of SOS", Toast.LENGTH_LONG).show();
            Log.d("MainActivity", "PERMISSION FOR SEND SMS NOT GRANTED, REQUESTING PERMSISSION...");
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.SEND_SMS}, RC);
        }
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED;
    }

    private void closeNow(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            //finishAffinity();
        }
        else{
            //finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    deviceId();
                } else {
                    closeNow();
                    Toast.makeText(getContext(), "Without permission we check", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
            Log.d("Profile Fr","Service sosplayer new startIntent...");
            Toast.makeText(getContext(),"Service sosplayer stopping...",Toast.LENGTH_SHORT).show();
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

    private void checkMobileInFirebase(final String newMobile){
        firebaseHelper.getUsersDatabaseReference().child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userDataSnapshot) {
                final User oldUser = userDataSnapshot.getValue(User.class);
                if (oldUser.getMobile().equals(newMobile)){
                    // update user in sqlite and firebase
                    updateUser();
                }else {
                    firebaseHelper.getMobileDatabaseReference().child(newMobile).setValue(FirebaseAuth.getInstance().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // delete previous mobile number and add new number
                            firebaseHelper.getMobileDatabaseReference().child(oldUser.getMobile()).setValue(null);
                            updateUser();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // stop progress bar
                            progressDialog.dismiss();
                            // prompt user to enter different mobile number
                            Toast.makeText(getActivity(), "Mobile number is registered to another account",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG,databaseError.getDetails());
                Toast.makeText(getActivity(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser(){

        Log.d(TAG,"Updating user...");

        user.setMobile(textPhone.getText().toString());
        mydb.updateUser(user);
        firebaseHelper.updateuser_infirebase(FirebaseAuth.getInstance().getUid(),user);

        // stop progress bar
        progressDialog.dismiss();

        btn_edit.setText("edit");
        alphaa(0.6f);
        disable();
    }

    private void changePassword(String email){
        if (!IsInternet.checkInternet(getContext()))
            return;

<<<<<<< HEAD
        //pgbarshow();
=======
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Sending Email...");
        progressDialog.show();
        progressDialog.setCancelable(false);
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
        try {
            firebaseHelper.getFirebaseAuth().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        Toast.makeText(getActivity(),"EMAIL SENT. PLEASE CHECK YOUR MAIL TO CHANGE PASSWORD",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        try {
                            throw task.getException();
                        }catch (Exception e){
                            Log.d(TAG,e.getMessage());
                            Toast.makeText(getActivity(),"You need to sign in again to change password",Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }catch (Exception e){
<<<<<<< HEAD
=======
            progressDialog.dismiss();
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
            Log.d(TAG, e.getMessage());
            Toast.makeText(getActivity(),"You need to sign in again to change password",Toast.LENGTH_LONG).show();
        }
    }

<<<<<<< HEAD
    private void chooseImg(){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    private void uploadProfilePicToFirebase(){
=======
    private void pictureChoice(){
        final AlertDialog.Builder a_builder = new AlertDialog.Builder(getContext());
        a_builder.setTitle("Profile Photo")
                .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        chooseImg("camera");
                    }
                })
                .setNeutralButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        chooseImg("gallery");
                    }
                });
        AlertDialog alert = a_builder.create();
        alert.show();
    }

    private void chooseImg(String choice){
        switch (choice) {
            case "gallery":
                Intent pickImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickImageIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(pickImageIntent, "Select Picture"), PICK_IMAGE_REQUEST);
                break;

            case "camera":
                Intent captureImgIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //captureImgIntent.setType("image/*");
                //Uri output = Uri.fromFile(new File(filePath));
                //captureImgIntent.putExtra(MediaStore.EXTRA_OUTPUT, output);
                if (captureImgIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivityForResult(captureImgIntent, TAKE_PICTURE);
                    break;
                }
        }
    }

    private void uploadProfilePicToFirebase(){

>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            progressDialog.setCancelable(false);

<<<<<<< HEAD
            StorageReference ref = firebaseHelper.getStorageReference().child("images/profile_pic");
            ref.putFile(filePath)
=======
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
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
<<<<<<< HEAD
                            Toast.makeText(getContext(),"Uploaded", Toast.LENGTH_SHORT).show();
=======
                            Toast.makeText(getContext(),"Image uploaded successfully", Toast.LENGTH_SHORT).show();
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
<<<<<<< HEAD
                            Toast.makeText(getContext(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
=======
                            Toast.makeText(getContext(), "Failed to upload image"+e.getMessage(), Toast.LENGTH_SHORT).show();
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
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
<<<<<<< HEAD
=======
        else
            Toast.makeText(getActivity(), "File not found", Toast.LENGTH_SHORT).show();
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
    }

    private void deleteExistingProfilePic(){
        if (filePath == null)
            return;
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
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
<<<<<<< HEAD
                    Toast.makeText(getContext(), "Unable to delete img",Toast.LENGTH_LONG).show();
=======
                    Toast.makeText(getContext(), "Failed to upload image"+exception.getMessage(),Toast.LENGTH_LONG).show();
>>>>>>> fe4914adc93adb59e7c8a071e5d5b03dd6514ede
            }
        });
    }
}