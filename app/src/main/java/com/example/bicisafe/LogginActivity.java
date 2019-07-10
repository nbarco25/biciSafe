package com.example.bicisafe;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

public class LogginActivity extends AppCompatActivity {

    //Aqui se establecen los atributos propios de la clase
    private static final String TAG = "AndroidClarified";
    private SignInButton googleSignInButton;
    private GoogleSignInClient googleSignInClient;
    private GoogleApiClient googleApiClient;



    //El metodo onCreate está presente casi en todas las actividades
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Se tiene que establecer que cada uno de los atributos debifinos arriba
        //corresponden a los elementos de la interfaz grafica que ellos representan.
        //Se buscan con el id correspindiente de la interfaz grafica
        googleSignInButton = findViewById(R.id.sign_in_button);
        googleSignInButton.setSize(SignInButton.SIZE_WIDE);

        //Estos metodos son necesarios para lograr el inicio de sesion. Se crea un GoogleSignInOpctions
        //para establecer que campos de informacion se requerirá al usuario al momento de iniciar sesion
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        //Se asigna la variable Cliente para manejar la sesion.
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        //Al boton de inicio de sesion, le activo un "escuchador clic"
        //Esto para que realicé el proceso que nosotros queramos cada vez que haga clic en ese boton.
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                //Para que el en el onActivityResult se encuentre el resultado de la actividad
                //se le asigna un codigo que se comprobará en el OnActivityResult
                startActivityForResult(signInIntent, 101);
            }
        });

    }

    //Otro metodo sobreescrito es onStart, que se diferencia de onCreate.
    //En este metodo simplemente se hacen verificaciones o comprabaciones al iniciar la app
    //diferente de onCreate en el que se declarar las variables o herramientas que se van a
    //usar a lo largo del tiempo de vida de la Activity.
    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount alreadyloggedAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (alreadyloggedAccount != null) {
            Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
            onLoggedIn(alreadyloggedAccount);
        } else {
            Log.d(TAG, "Not logged in");
        }
    }


    //Este metodo es como un manejador de eventos del startActivityResult (que se activa con el boton
    // declarado en el onCreate)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "y Ajá dentro de onActivityResult", Toast.LENGTH_SHORT).show();

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode==101){
                Toast.makeText(this, "Ahora dentro del IF de onActivityResult", Toast.LENGTH_SHORT).show();
                try {
                    Toast.makeText(this, "Ahora dentro del try del if", Toast.LENGTH_SHORT).show();

                    // The Task returned from this call is always completed, no need to attach
                    // a listener.
                    // Sentencia necesaria para crear una tarea que se encargue del inicio de Sesion
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    //Una vez logrado el inicio de sesion y obtenida la cuenta, llamo al metodo
                    //creado por mi, para gestionar el inicio de sesion de esa cuenta.
                    onLoggedIn(account);
                } catch (ApiException e) {
                    // The ApiException status code indicates the detailed failure reason.
                    Toast.makeText(this, "NO SE PUDO INICIAR SESION", Toast.LENGTH_SHORT).show();

                    Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                }

            }
        }





    }

    private void onLoggedIn(GoogleSignInAccount account) {

        //Cuando invoco este metodo, significa que ya abrió sesion y puedo mostrarle al
        //usuario la pantalla principal. Esto se logra con los "Intent" que sirven
        //para abrir las Activitys dandoles un contexto de inicio, y la Activity que
        //se quiere abrir.
        Toast.makeText(this, "se supone que ya inicié", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, PrincipalActivity.class);
        //Además, le pongo un dato extra para uno de los atributos de la Activity que quiero
        //abrir, que es la cuenta que se acaba de loggear.
        intent.putExtra(PrincipalActivity.GOOGLE_ACCOUNT, account);
        //Inicio la Activity nueva a abrir.
        startActivity(intent);
        //Y finalizo ésta activity (la de inicio de sesión)
        finish();

    }

}
