package e.sanjaaykishore.mech;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.ghyeok.stickyswitch.widget.StickySwitch;

public class Main4Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,RoutingListener {


    String user_id;
    String user_name, name;
    String user_area;
    DatabaseReference reference;
    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    private LocationRequest request;
    LatLng latLng, summaLatLong;
    Location lastLocation;
    private StickySwitch stickySwitch;
    LatLng preLocation;
    private boolean logedout = false;
    FirebaseAuth fauth = FirebaseAuth.getInstance();
    private String CustAllocId = "";
    private int sel=1;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    Dialog myDialog;
    //R.colors.primary_dark,R.color.primary,R.color.primary_light,R.color.accent,


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        polylines = new ArrayList<>();
        myDialog = new Dialog(this);

        NavListen();

        stickySwitch = (StickySwitch)findViewById(R.id.switchavai);
        stickySwitch.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(StickySwitch.Direction direction, String s) {
                if(sel==1)
                {
                    switchToAvailable();
                    Toast.makeText(Main4Activity.this, "You are now avalilable for service", Toast.LENGTH_SHORT).show();
                    sel=3;
                }
                else
                {
                    if(FirebaseAuth.getInstance().getCurrentUser().getUid()!=null&&FirebaseDatabase.getInstance().getReference().child("AvaillableMechanic")!=null) {
                        removeGeo();
                    }
                    removeGeo1();



                    Toast.makeText(Main4Activity.this, "You are now disabled from service", Toast.LENGTH_SHORT).show();
                    sel=1;
                }
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                showpop();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        getAllocCustomerId();


    }
    private boolean firstoc=true;

    protected void getAllocCustomerId() {
        String mech_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Mechanic").child(mech_id).child("custAllocId");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    CustAllocId = dataSnapshot.getValue().toString();
                    try {
                        Thread.sleep(4000);
                        getBookedCustomerPickLocation();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }
                else
                {
                    eraseRoute();
                    CustAllocId="";
                    if(remMark!=null) {
                        remMark.remove();
                    }
                    if(DatabaseReferenceList!=null) {
                        refc.removeEventListener(DatabaseReferenceList);
                    }
                    if(!firstoc) {
                        Toast.makeText(Main4Activity.this, "Customer Cancelled The Request..", Toast.LENGTH_SHORT).show();
                        firstoc=false;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void showpop()
    {
        TextView txtclose;
        Button btnCancel,btnFinish;
        myDialog.setContentView(R.layout.popup);
        txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
        txtclose.setText("M");
        btnCancel = (Button) myDialog.findViewById(R.id.btncancel);
        btnFinish = (Button) myDialog.findViewById(R.id.btnfinish);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    private Marker remMark;
    private DatabaseReference refc;
    private ValueEventListener DatabaseReferenceList;
    private void getBookedCustomerPickLocation() {
        refc = FirebaseDatabase.getInstance().getReference().child("RequestedCustomers").child(CustAllocId).child("l");
        DatabaseReferenceList = refc.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !CustAllocId.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double lat = 0;
                    double lng = 0;
                    if (map.get(0) != null) {
                        lat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        lng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng pickLoc = new LatLng(lat, lng);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pickLoc));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                    remMark=mMap.addMarker(new MarkerOptions().position(pickLoc).title("Customer"));
                        routeMaking(pickLoc);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void routeMaking(LatLng pickLoc) {
        if(pickLoc!=null && lastLocation!=null) {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), pickLoc)
                    .build();
            if(routing!=null)
            {
            routing.execute();}
        }
    }


    private void NavListen() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {

            googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));

        } catch (Resources.NotFoundException e) {
        }


        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        googleApiClient.connect();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);

        }

@Override
public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
        drawer.closeDrawer(GravityCompat.START);
        } else {
        super.onBackPressed();
        }
        }




@SuppressLint("RestrictedApi")
@Override
public void onConnected(@Nullable Bundle bundle) {
        request = new LocationRequest().create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(3000);
        request.setFastestInterval(3000);

        }

        private void switchToAvailable()
        {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, request, this);
        }
@Override
public void onConnectionSuspended(int i) {

        }

@Override
public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }

@Override
public void onLocationChanged(Location location) {

        //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        lastLocation=location;

       /* if(location==null)
        {
            Toast.makeText(getApplicationContext(),"Location could nor be found",Toast.LENGTH_SHORT).show();
        }
        else
        {*/
    latLng = new LatLng(location.getLatitude(),location.getLongitude());
    if(summaLatLong==null)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        summaLatLong=latLng;
    }

         //MarkerOptions options = new MarkerOptions();


        String userid = fauth.getInstance().getCurrentUser().getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("AvailableMechanic");
      DatabaseReference referenceBook = FirebaseDatabase.getInstance().getReference("BookedMechanics");
    GeoFire geo = new GeoFire(reference);
    GeoFire geoBook = new GeoFire(referenceBook);

    switch (CustAllocId) {
        case "":
            geoBook.removeLocation(userid);
            geo.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()));
            break;
        default:
            geo.removeLocation(userid);
            geoBook.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()));
            break;
    }
}







@Override
protected void onStop() {
        super.onStop();

        if(!logedout)
        {
        removeGeo();
        }

        }

protected  void removeGeo()
        {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("AvailableMechanic");


        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);


        }
    protected  void removeGeo1()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BookedMechanics");


        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);


    }


        @Override
public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
        case R.id.nav_send:
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
        logedout=true;
        removeGeo();
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(Main4Activity.this,MainActivity.class);
        startActivity(i);

        }
        else
        {
        Toast.makeText(this, "USER ALREADY LOGGED OUT", Toast.LENGTH_SHORT).show();
        }
        break;

            case R.id.nav_camera:
                finish();
                startActivity(getIntent());





        }



        return true;



}

    @Override
    protected void onStart() {
        super.onStart();

}

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

        Toast.makeText(this, "DRAWING SHORTEST ROUTE", Toast.LENGTH_SHORT).show();
}

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int shortestrouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <arrayList.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(arrayList.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ arrayList.get(i).getDistanceValue()+": duration - "+ arrayList.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onRoutingCancelled() {

    }

    private void eraseRoute()
    {
        for(Polyline line : polylines)
        {
            line.remove();
        }
        polylines.clear();
    }


}
