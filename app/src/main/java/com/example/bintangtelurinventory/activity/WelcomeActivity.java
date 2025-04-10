package com.example.bintangtelurinventory.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.bintangtelurinventory.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class WelcomeActivity extends AppCompatActivity {
    Button btn_signin, btn_login, btn_googlelogin;
    EditText et_email, et_password;
    private FirebaseAuth mAuth;
    TextView btn_forgetpass;
    GoogleSignInClient googleSignInClient;
    ActivityResultLauncher<Intent> activityResultLauncher;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onStart() {
        super.onStart();
        // cek apakah sudah login kalo sudah maka langsung passing ke main activity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //buuka activity
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //ambil statusapp untuk memastikan apakah tombol signup diaktifkan atau tidak
        db.collection("statusApp").whereEqualTo("signup", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        boolean isEx = false;
                        for (QueryDocumentSnapshot document : value) {
                            isEx = true;
                        }
                        if(isEx == false){
                            btn_signin.setEnabled(false);
                        }else{
                            btn_signin.setEnabled(true);
                        }
                    }
                });

        //INIT
        btn_forgetpass = findViewById(R.id.btn_forgetpass);
        btn_signin = findViewById(R.id.btn_signin);
        btn_login = findViewById(R.id.btn_login);
        et_email = findViewById(R.id.et_email);
        btn_googlelogin = findViewById(R.id.btn_googlelogin);
        et_password = findViewById(R.id.et_password);
        mAuth = FirebaseAuth.getInstance();
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() { //untuk tangkap data result setelah user pilih account di intent signInIntent
            @Override
            public void onActivityResult(ActivityResult result) {
                int resultCode = result.getResultCode();
                Intent data = result.getData();
                if(resultCode == RESULT_OK && data != null){
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try { //jika login berhasil
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Toast.makeText(WelcomeActivity.this, "Berhasil Login!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                        //ambil data user yang login saat ini
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                        mAuth.signInWithCredential(authCredential)
                                .addOnCompleteListener(WelcomeActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            //dengan variabel user ini kita bisa ambil nama user di acc googlenya, emailnya dll.
                                        }else{

                                        }
                                    }
                                });
                    } catch (ApiException e) {
                        Toast.makeText(WelcomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });



        //EVENT
        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!et_email.getText().toString().equals("")  && !et_password.getText().toString().equals("") ) {
                    //sign up ke firebase
                    mAuth.createUserWithEmailAndPassword(et_email.getText().toString(), et_password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(WelcomeActivity.this, "Berhasil Sign Up, Silahkan Login!", Toast.LENGTH_SHORT).show();
                                        et_email.setText("");
                                        et_password.setText("");
                                    } else {
                                        Toast.makeText(WelcomeActivity.this, "Gagal Sign Up, Data Salah Atau Email Sudah Terdaftar!", Toast.LENGTH_SHORT).show();
                                        et_password.setText("");
                                    }
                                }
                            });
                }else{
                    Toast.makeText(WelcomeActivity.this, "Data Tidak Boleh Kosong!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //login dengan email passwword firebase
                mAuth.signInWithEmailAndPassword(et_email.getText().toString(), et_password.getText().toString())
                        .addOnCompleteListener(WelcomeActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    et_email.setText("");
                                    et_password.setText("");
                                    //buuka activity
                                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(WelcomeActivity.this, "Gagal Login, Data Salah Atau Tidak Terdaftar!", Toast.LENGTH_SHORT).show();
                                    et_password.setText("");
                                }
                            }
                        });
            }
        });

        btn_forgetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        WelcomeActivity.this);
                alertDialogBuilder.setTitle("Lupa Password?");
                alertDialogBuilder
                        .setMessage("Klik ya agar dapat email untuk reset password!")
                        .setIcon(R.mipmap.ic_launcher)
                        .setCancelable(false)
                        .setPositiveButton("Ya",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                if(!et_email.getText().toString().equals("")) {
                                    //kirim email ke user untuk reset password user dengan firebase
                                    mAuth.sendPasswordResetEmail(et_email.getText().toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(WelcomeActivity.this, "Silahkan Cek Email Dan Lakukan Reset Password!", Toast.LENGTH_SHORT).show();
                                                        et_email.setText("");
                                                        et_password.setText("");
                                                    } else {
                                                        Toast.makeText(WelcomeActivity.this, "Email Tidak Terdaftar Atau Salah!", Toast.LENGTH_SHORT).show();
                                                        et_email.setText("");
                                                        et_password.setText("");
                                                    }
                                                }
                                            });
                                }else{
                                    Toast.makeText(WelcomeActivity.this, "Email Tidak Ditemukan!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Tidak",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });

        btn_googlelogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //LOGIN DENGAN GOOGLE ACCOUNT
                //init gso
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("366260561772-fh4coef31t9foqqu852pve0cpjmnctrq.apps.googleusercontent.com") // client id ini capat dari app - build - generated - res - google setvices - debug - values - values.xml - tinggal copy paste
                        .requestEmail().build();
                googleSignInClient = GoogleSignIn.getClient(WelcomeActivity.this, gso);

                //launch intent signInIntent agar user bisa pilih account google yang mana
                Intent signInIntent = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(signInIntent);
            }
        });
    }
}