package e.sanjaaykishore.mech;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {
EditText t1,t2;
FirebaseAuth fauth;
TextToSpeech textToSpeech;
    private static CheckBox show_hide_password;
DatabaseReference mdata = FirebaseDatabase.getInstance().getReference().child("Mechanic");
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fauth=FirebaseAuth.getInstance();
        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR)
                {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }

            }
        });
        if(fauth.getCurrentUser()!=null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(), Main4Activity.class));
        }
         t1=(EditText)findViewById(R.id.usr);
         t2=(EditText)findViewById(R.id.pass);
         startService(new Intent(MainActivity.this,onAppKilled.class));
         fauth=FirebaseAuth.getInstance();
        show_hide_password = (CheckBox)findViewById(R.id.shp);
        show_hide_password.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {

                if (isChecked) {

                    show_hide_password.setText(R.string.hide_pwd);
                    t2.setInputType(InputType.TYPE_CLASS_TEXT);
                    t2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    show_hide_password.setText(R.string.show_pwd);
                    t2.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    t2.setTransformationMethod(PasswordTransformationMethod
                            .getInstance());

                }

            }
        });

    }
    public void login(View view)
    {
         String username =t1.getText().toString();
         String password=t2.getText().toString();

         if(!TextUtils.isEmpty(username)&&!TextUtils.isEmpty(password)) {
             fauth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(@NonNull Task<AuthResult> task) {
                     if (task.isSuccessful()) {
                         final String user_id = fauth.getCurrentUser().getUid();

                         mdata.addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(DataSnapshot dataSnapshot) {
                                 if (dataSnapshot.hasChild(user_id)) {
                                     Toast.makeText(MainActivity.this, "LOGIN SUCCESSFUL...", Toast.LENGTH_SHORT).show();
                                     Intent intent = new Intent(MainActivity.this, Main4Activity.class);
                                     startActivity(intent);

                                 } else {
                                     Toast.makeText(MainActivity.this, "USER NOT REGISTERED PLEASE TRY AGAIN...", Toast.LENGTH_SHORT).show();
                                     speakToast("user not registered please try again");
                                 }

                             }

                             @Override
                             public void onCancelled(DatabaseError databaseError) {

                             }
                         });

                     } else {
                         Toast.makeText(MainActivity.this, "USERNAME / PASSWORD IS INCORRECT", Toast.LENGTH_SHORT).show();
                         speakToast("username or password is incorrect");

                     }

                 }
             });
         }
         else
         {
             Toast.makeText(this, "Please make sure to fill all fields", Toast.LENGTH_SHORT).show();
             speakToast("please fill all the fields");
         }




    }
    public void register(View view)
    {

        Intent intent = new Intent(MainActivity.this ,Main3Activity.class);
        startActivity(intent);
    }

    public void forget (View V)
    {
        Intent intent=new Intent(MainActivity.this, Main5Activity.class);
        startActivity(intent);
    }


    public void speakToast(String text)
    {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onDestroy() {
        if(textToSpeech!=null)
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


}
