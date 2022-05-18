package com.example.redessocialesmaster;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    Button compartir ;
    Bitmap imagen ;
    CallbackManager callbackManager;
    TextView txtNombre;
    ProgressDialog mDialog;
    ImageView imgAvatar;
    LoginButton loginButton;

    Context context;
    String cadena;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode,data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            PackageInfo info=getPackageManager().getPackageInfo("com.example.redessocialesmaster" ,
                    PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures){
                MessageDigest md=MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(),Base64.DEFAULT));
                System.out.println(Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        callbackManager = CallbackManager.Factory.create();
        txtNombre = (TextView) findViewById(R.id.txtNombre);
        imgAvatar = (ImageView) findViewById(R.id.avatar);
        loginButton =(LoginButton) findViewById(R.id.login_button);

        // Checking the Access Token.
        if (AccessToken.getCurrentAccessToken() != null) {

            GraphLoginRequest(AccessToken.getCurrentAccessToken());

            // If already login in then show the Toast.
            Toast.makeText(MainActivity.this, "Inicio de Sesión Listo", Toast.LENGTH_SHORT).show();

        } else {

            // If not login in then show the Toast.
            Toast.makeText(MainActivity.this, "Usuario no logeado", Toast.LENGTH_SHORT).show();
        }

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                // Calling method to access User Data After successfully login.
                GraphLoginRequest(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "Login cancelado", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(MainActivity.this, "Login fallado", Toast.LENGTH_SHORT).show();
            }
        });
        if (AccessToken.getCurrentAccessToken() == null){
            txtNombre.setText("");
        }
        compartir = (Button) findViewById(R.id.btnCompartir);

        imagen = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
        compartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharePhoto picture = new SharePhoto.Builder().setBitmap(imagen).build();
                SharePhotoContent cont1 = new SharePhotoContent.Builder().addPhoto(picture).build();
                ShareLinkContent cont2 = new ShareLinkContent.Builder().setContentUrl(Uri.parse("https://developers.facebook.com/")).build();
                ShareDialog.show(MainActivity.this,cont2);
            }
        });
    }
    protected void GraphLoginRequest(AccessToken accessToken) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                        try {
                            // Adding all user info one by one into TextView.
                            Profile profile = Profile.getCurrentProfile();
                            String profileImagen = profile.getProfilePictureUri(150,150).toString();
                            Picasso.with(getApplicationContext()).load(profileImagen).into(imgAvatar);
                            txtNombre.setText("\nPerfil: " + jsonObject.getString("name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle bundle = new Bundle();
        bundle.putString(
                "fields",
                "id,name,feed"
        );
        graphRequest.setParameters(bundle);
        graphRequest.executeAsync();
    }
}
