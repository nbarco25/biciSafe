package com.example.bicisafe;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.RoutingListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private static final int ZOOM_PREDETERMINADO = 15;
    private static final int PERMISO_PETICION_LOCALIZACION = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mUltimaLocalizacion;
    private final LatLng mLocalizacionPredeterminada = new LatLng(-33.8523341, 151.2106085);
    private Polyline varPolilinea;
    private Marker marcador;

    double lat=0.0;
    double lng=0.0;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Estas dos asignaciones son necesarias para activar los servicios del mapa.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Se pide permizo para la geolocalización.
        getPermisoLocalizacion();
        //Con este metodo se configuran los controles que tendrá el mapa, en este caso solo será el de ubicación.
        actualizarControlesMapa();
        //Se obtiene la localización actual y se pone un marcador en ese punto.
        getLocalizacionActual();

        //Se crean un PolylineOptions para poder graficar los trazos del recorrido
        final PolylineOptions pOptions = new PolylineOptions().clickable(true);
        //Con el metodo movCamera se enfoca la camara del mapa al iniciar en algún punto especifico
        //y con un nivel de zoom dado.
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(2.438236, -76.60818), ZOOM_PREDETERMINADO));
        //Se activa un Listener a la localización para poder hacer que cada vez que el punto de localización
        //actual se mueva, grafique un trazo desde el punto anterior hasta el nuevo punto.
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                //Los objetos de coordenadas se instancian con la clase LatLng, que recibe como parametros
                //los valores correspondientes a la Latitud y la Longitud.
                LatLng varPosicionNueva = new LatLng(location.getLatitude(), location.getLongitude());
                //Se le dice al PolylineOptions que agregue un nuevo punto para graficar.
                pOptions.add(varPosicionNueva);
                //Se invoca el metodo addPolyline de nuestro mapa que recibe como parametro el PolylineOptions
                //que se creó anteriormente, lo cual retorna un objeto de tipo Polyline, que se lo asignamos
                //al varPolilinea que se declaró en los atributos de la clase.
                varPolilinea = mMap.addPolyline(pOptions);
                //En este metodo se personaliza nuestra Polilinea para cambiarle color, tamaño o inscripción.
                personalizarPoliLinea();
            }
        });

        //getLocalizacionActual();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISO_PETICION_LOCALIZACION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        //actualizarControlesMapa();
    }

    private void getLocalizacionActual() {
    //Para obtener la localización actual se hace uso de la variable mFusedLocationProvider
        // invocando el metodo getLastLocalization
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Una vez la llamada al metodo o "tarea" esté completada
                            // se crea una variable de coordenadas tipo LatLeng y obtenemos
                            // las coordenadas de la localización actual.
                            mUltimaLocalizacion = task.getResult();
                            LatLng coordenada = new LatLng(mUltimaLocalizacion.getLatitude(),
                                    mUltimaLocalizacion.getLongitude());
                            //Con el metodo movCamera se enfoca la camara del mapa al iniciar en algún punto especifico
                            //y con un nivel de zoom dado.
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    coordenada, ZOOM_PREDETERMINADO));
                            //Ponemos un marcador inicial para indicar la posición desde la que se inició
                            // el recorrido, por eso el comentario que aparece ahí.
                            ponerMarcador(coordenada, "Aquí empiezas tu recorrido!");
                        } else {
                            //Log.d(TAG, "Current location is null. Using defaults.");
                            //Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mLocalizacionPredeterminada, ZOOM_PREDETERMINADO));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void ponerMarcador(LatLng parCoordenada, String parComentario) {
        mMap.addMarker(new MarkerOptions()
                .position(parCoordenada)
                .title(parComentario)
        );
    }

    private void actualizarControlesMapa() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mUltimaLocalizacion = null;
                getPermisoLocalizacion();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getPermisoLocalizacion() {
        //Aqui simplemente está la logica con la que se piden permisos de localización.
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISO_PETICION_LOCALIZACION);
        }
    }

    private void personalizarPoliLinea() {
        //En este metodo establecemos el color, el grosor y el comentario de los recorridos graficados
        // en el mapa, probablemente se le puedan personalizar más opciones.
        varPolilinea.setColor(Color.BLUE);
        varPolilinea.setWidth(8);
        varPolilinea.setTag("Tu primer recorrido!");

    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {

    }

    @Override
    public void onRoutingCancelled() {

    }
}
