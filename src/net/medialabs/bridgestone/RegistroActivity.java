package net.medialabs.bridgestone;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
	
	private static final String SERVICE_BASE_URL = "http://www.solucionesparche.com/labs/bridgestone/index.php/servicios/";
	private static final String SERVICE_FORMAT = "format/json/";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registro);
		Intent intent = getIntent();
		ids = intent.getIntArrayExtra("ids");
		
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
					RegistrarUsuario usuario = new RegistrarUsuario();
					usuario.execute();
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
	
	private class RegistrarUsuario extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(RegistroActivity.this, "", "Registrando usuario...", true);	
		}
		
		@Override
		protected String doInBackground(Void... params) {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(SERVICE_BASE_URL + "clientes/guardar/" + SERVICE_FORMAT);
			post.setHeader("content-type", "application/json");
			JSONObject listaIds = new JSONObject();
			for(int i = 0; i < ids.length; i++) {
				try {
					listaIds.put(String.valueOf(i), ids[i]);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			JSONObject usuarioInfo = new JSONObject();
			
			try {
				usuarioInfo.put("nombre", registroNombre.getText().toString());
				usuarioInfo.put("apellidos", registroApellidos.getText().toString());
				usuarioInfo.put("correo", registroCorreo.getText().toString());
				usuarioInfo.put("rut", registroRut.getText().toString());
				usuarioInfo.put("patente", registroPatente.getText().toString());
				usuarioInfo.put("telefono", registroTelefono.getText().toString());
				usuarioInfo.put("respuestas", listaIds);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			try {
				StringEntity entity = new StringEntity(usuarioInfo.toString());
				post.setEntity(entity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			HttpResponse resp;
			try {
				resp = client.execute(post);
				return EntityUtils.toString(resp.getEntity());
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dialog.dismiss();
			Log.d("Resultado", result);
			try {
				JSONObject resultObject = new JSONObject(result);
				if(resultObject.getString("status").equalsIgnoreCase("OK")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(RegistroActivity.this);
			        builder.setMessage("Cliente registrado exitosamente.")
			               .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
			                   public void onClick(DialogInterface dialog, int id) {
			                	   Intent intent = new Intent(RegistroActivity.this, EncuestaActivity.class);
			       					startActivity(intent);
			       					RegistroActivity.this.finish();
			                   }
			               });
			        builder.create();
			        builder.show();
				} else {
					Toast.makeText(RegistroActivity.this, resultObject.getString("response"), Toast.LENGTH_LONG).show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}

}
