package net.medialabs.bridgestone;

import net.medialabs.utilities.Alertas;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

public class RegistroActivity extends Activity {
	
	EditText registroNombre;
	EditText registroApellidos;
	EditText registroCorreo;
	EditText registroRut;
	EditText registroPatente;
	EditText registroTelefono;
	ImageButton btnCancelar;
	ImageButton btnGuardar;
	private int[] ids;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registro);
		Intent intent = getIntent();
		ids = intent.getIntArrayExtra("ids");
		Log.d("Id", String.valueOf(ids[0]));
		
		registroNombre = (EditText) findViewById(R.id.registroNombre);
		registroApellidos = (EditText) findViewById(R.id.registroApellidos);
		registroCorreo = (EditText) findViewById(R.id.registroCorreo);
		registroRut = (EditText) findViewById(R.id.registroRut);
		registroPatente = (EditText) findViewById(R.id.registroPatente);
		registroTelefono = (EditText) findViewById(R.id.registroTelefono);
		btnCancelar = (ImageButton) findViewById(R.id.btnCancelarRegistro);
		btnGuardar = (ImageButton) findViewById(R.id.btnGuardarRegistro);
		
		btnGuardar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!registroNombre.getText().toString().equals("") && 
						!registroApellidos.getText().toString().equals("") && 
						!registroCorreo.getText().toString().equals("")) {
					
				}
			}
		});
		
		btnCancelar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(RegistroActivity.this, EncuestaActivity.class);
				startActivity(intent);
				RegistroActivity.this.finish();
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.registro, menu);
		return true;
	}
	
	private class RegistrarUsuario extends AsyncTask<Integer, Void, String> {
		
		private ProgressDialog dialog;
		private Alertas alerta = new Alertas();
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(RegistroActivity.this, "", "Registrando usuario...", true);	
		}
		
		@Override
		protected String doInBackground(Integer... params) {
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}
		
	}

}
