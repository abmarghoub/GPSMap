package com.example.mapapplication;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class GoogleMapActivity extends AppCompatActivity {

    private MapView map;
    private RequestQueue requestQueue;
    private String showUrl = "http://10.0.2.2/map_project/getPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSMDroid configuration
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("prefs", MODE_PRIVATE));

        setContentView(R.layout.activity_google_map);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        // Set initial map position
        map.getController().setZoom(15.0);
        map.getController().setCenter(new GeoPoint(37.272525, -122.12106)); // default coordinates

        // Initialize Volley
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Load markers from your API
        loadPositions();
    }

    private void loadPositions() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                showUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray positions = response.getJSONArray("positions");
                            for (int i = 0; i < positions.length(); i++) {
                                JSONObject position = positions.getJSONObject(i);
                                double lat = position.getDouble("latitude");
                                double lng = position.getDouble("longitude");

                                Marker marker = new Marker(map);
                                marker.setPosition(new GeoPoint(lat, lng));
                                marker.setTitle("Marker " + (i + 1));

                                Drawable original = getResources().getDrawable(R.drawable.marker);
                                Bitmap bitmap = ((BitmapDrawable) original).getBitmap();

                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false);
                                marker.setIcon(new BitmapDrawable(getResources(), scaledBitmap));

                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                                map.getOverlays().add(marker);

                                Toast.makeText(getApplicationContext(),"Lat : "+ lat+" lng : "+lng,Toast.LENGTH_SHORT).show();
                            }

                            // Refresh map
                            map.invalidate();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
}
