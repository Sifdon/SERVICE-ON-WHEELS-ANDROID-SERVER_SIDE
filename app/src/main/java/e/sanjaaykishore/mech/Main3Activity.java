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
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Main3Activity extends AppCompatActivity {
    EditText t1, t2, t3, t4, t5, t6;
    FirebaseAuth fauth;
    DatabaseReference mData = FirebaseDatabase.getInstance().getReference().child("Mechanic");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        t1 = findViewById(R.id.sname);
        t2 = findViewById(R.id.name);
        t3 = findViewById(R.id.phone);
        t4 = findViewById(R.id.email);
        t5 = findViewById(R.id.pass);
        t6 = findViewById(R.id.addr);


    }

    public void sub(View view) {

        fauth = FirebaseAuth.getInstance();

        fauth.createUserWithEmailAndPassword(t4.getText().toString(),t5.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {


                    String user_id = fauth.getCurrentUser().getUid();
                    DatabaseReference data = mData.child(user_id);
                    data.setValue(true);

                    data.child("Name").setValue(t2.getText().toString());
                   data.child("Phone").setValue(t3.getText().toString());
                    data.child("Address").setValue(t6.getText().toString());
                    data.child("Shop Name").setValue(t1.getText().toString());
                    startActivity(new Intent(Main3Activity.this,MainActivity.class));
                } if(!task.isSuccessful()){
                    FirebaseAuthException e = (FirebaseAuthException)task.getException();
                    Toast.makeText(Main3Activity.this, "Failed Registration: "+e.getMessage(), Toast.LENGTH_SHORT).show();

                    return;
                }
            }
        });


    }
}
