package com.example.saludable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private Button LoginButton;
    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccountLink;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "LoginActivity";
    private ImageView GoogleSingButton;
    private GoogleApiClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate ( savedInstanceState );
            setContentView ( R.layout.activity_login );

            mAuth = FirebaseAuth.getInstance ();
            NeedNewAccountLink = findViewById ( R.id.register_account_link );
            UserEmail = findViewById ( R.id.login_email );
            UserPassword = findViewById ( R.id.login_password );
            LoginButton = findViewById ( R.id.login_button );
            GoogleSingButton = findViewById ( R.id.google_signin_button );

            loadingBar = new ProgressDialog ( this );

            NeedNewAccountLink.setOnClickListener ( new View.OnClickListener () {
                @Override
                public void onClick(View view) {
                    SendUserToRegisterActivity ();
                }

            } );

            LoginButton.setOnClickListener ( new View.OnClickListener () {
                @Override
                public void onClick(View view) {

                    AllowingUserToLogin ();
                }
            } );

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder ( GoogleSignInOptions.DEFAULT_SIGN_IN )
                    .requestIdToken ( getString ( R.string.default_web_client_id ) )
                    .requestEmail ()
                    .build ();

            mGoogleSignInClient = new GoogleApiClient.Builder ( this ).
                    enableAutoManage ( this, new GoogleApiClient.OnConnectionFailedListener () {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Toast.makeText ( LoginActivity.this, "Conexión con Google fallida", Toast.LENGTH_SHORT ).show ();

                        }
                    } ).addApi ( Auth.GOOGLE_SIGN_IN_API, gso ).build ();

            GoogleSingButton.setOnClickListener ( new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    signIn ();
                }

            } );

        } catch (Exception e) {
        }
    }

    private void signIn() {
        try {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent ( mGoogleSignInClient );
            startActivityForResult ( signInIntent, RC_SIGN_IN );
        } catch (Exception e) {
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult ( requestCode, resultCode, data );
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {

                loadingBar.setTitle ( "Inicio de sesion con Google" );
                loadingBar.setMessage ( "Espere mientras lo autenticamos con la cuenta de Google" );
                loadingBar.setCanceledOnTouchOutside ( true );
                loadingBar.show ();

                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent ( data );

                if (result.isSuccess ()) {
                    GoogleSignInAccount account = result.getSignInAccount ();
                    firebaseAuthWithGoogle ( account );
                    Toast.makeText ( this, "Usuario autenticado correctamente con la cuenta de Google", Toast.LENGTH_SHORT ).show ();

                } else {
                    Toast.makeText ( this, "No se ha podido autenticar el usuario con la cuenta de Google.", Toast.LENGTH_SHORT ).show ();
                    loadingBar.dismiss ();
                }
            }
        } catch (Exception e) {
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        try {

            Log.d ( TAG, "firebaseAuthWithGoogle:" + acct.getId () );

            AuthCredential credential = GoogleAuthProvider.getCredential ( acct.getIdToken (), null );
            mAuth.signInWithCredential ( credential )
                    .addOnCompleteListener ( this, new OnCompleteListener<AuthResult> () {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful ()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d ( TAG, "signInWithCredential:success" );
                                SendUserToMainActivity ();
                                loadingBar.dismiss ();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w ( TAG, "signInWithCredential:failure", task.getException () );
                                String message = task.getException ().toString ();
                                SendUserToLoginActivity ();
                                Toast.makeText ( LoginActivity.this, "Not Authenticated: " + message, Toast.LENGTH_SHORT ).show ();
                                loadingBar.dismiss ();
                            }
                        }
                    } );
        } catch (Exception e) {
        }
    }

    @Override
    protected void onStart() {
        try {
            super.onStart ();
            FirebaseUser currentUser = mAuth.getCurrentUser ();

            if (currentUser != null) {
                SendUserToMainActivity ();
            }
        } catch (Exception e) {
        }
    }

    private void AllowingUserToLogin() {
        try {
            String email = UserEmail.getText ().toString ();
            String password = UserPassword.getText ().toString ();
            if (TextUtils.isEmpty ( email )) {
                Toast.makeText ( this, "Porfavor ingrese su correo", Toast.LENGTH_SHORT ).show ();
            } else if (TextUtils.isEmpty ( password )) {
                Toast.makeText ( this, "Porfavor ingrese su contrasena", Toast.LENGTH_SHORT ).show ();
            } else {
                loadingBar.setTitle ( "Login" );
                loadingBar.setMessage ( "Espero mientras autenticamos el usuario ingresado." );
                loadingBar.setCanceledOnTouchOutside ( true );
                loadingBar.show ();

                mAuth.signInWithEmailAndPassword ( email, password )
                        .addOnCompleteListener ( new OnCompleteListener<AuthResult> () {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful ()) {
                                    SendUserToMainActivity ();
                                    Toast.makeText ( LoginActivity.this, "Usted se autentico correctamente", Toast.LENGTH_SHORT ).show ();
                                    loadingBar.dismiss ();
                                } else {
                                    String message = task.getException ().getMessage ();
                                    Toast.makeText ( LoginActivity.this, "A ocurrido un error: " + message, Toast.LENGTH_SHORT ).show ();
                                    loadingBar.dismiss ();
                                }
                            }
                        } );
            }
        } catch (Exception e) {
        }

    }

    private void SendUserToMainActivity() {
        try {
            Intent mainIntent = new Intent ( LoginActivity.this, MainActivity.class );
            mainIntent.addFlags ( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity ( mainIntent );
            finish ();
        } catch (Exception e) {
        }
    }

    private void SendUserToLoginActivity() {
        try {
            Intent mainIntent = new Intent ( LoginActivity.this, LoginActivity.class );
            mainIntent.addFlags ( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity ( mainIntent );
            finish ();
        } catch (Exception e) {
        }

    }

    private void SendUserToRegisterActivity() {
        try {
            Intent registerIntent = new Intent ( LoginActivity.this, RegisterActivity.class );
            startActivity ( registerIntent );
        } catch (Exception e) {
        }
    }

}
