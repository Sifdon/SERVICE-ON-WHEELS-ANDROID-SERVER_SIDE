package e.sanjaaykishore.mech;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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


public class MainActivity extends AppCompatActivity {
EditText t1,t2;
FirebaseAuth fauth;
DatabaseReference mdata = FirebaseDatabase.getInstance().getReference().child("Mechanic");
    private FirebaseAuth.AuthStateListener authStateListener;
    private boolean firstocc=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         t1=(EditText)findViewById(R.id.usr);
         t2=(EditText)findViewById(R.id.pass);
         startService(new Intent(MainActivity.this,onAppKilled.class));
         fauth=FirebaseAuth.getInstance();


    }
    public void login(View view)
    {
         String username =t1.getText().toString();
         String password=t2.getText().toString();

          fauth.signInWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
              if(task.isSuccessful())
              {
                final String user_id=fauth.getCurrentUser().getUid();

                mdata.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                     if(dataSnapshot.hasChild(user_id))
                     {
                         if(!firstocc) {
                             Toast.makeText(MainActivity.this, "LOGIN SUCCESSFUL...", Toast.LENGTH_SHORT).show();
                              firstocc=true;
                         }
                         Intent intent = new Intent(MainActivity.this ,Main4Activity.class);
                         startActivity(intent);

                     }
                     else
                     {
                         Toast.makeText(MainActivity.this, "USER NOT REGISTERED PLEASE TRY AGAIN...", Toast.LENGTH_SHORT).show();
                     }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

              }
              else
              {
                  Toast.makeText(MainActivity.this, "USERNAME / PASSWORD IS INCORRECT", Toast.LENGTH_SHORT).show();

              }

              }
          });




    }
    public void register(View view)
    {

        Intent intent = new Intent(MainActivity.this ,Main3Activity.class);
        startActivity(intent);
    }


}
