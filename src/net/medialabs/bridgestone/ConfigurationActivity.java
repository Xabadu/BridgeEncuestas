package net.medialabs.bridgestone;

import java.io.IOException;

import net.medialabs.utilities.Alertas;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class ConfigurationActivity extends Activity implements OnItemSelectedListener {
	
	private static final String SERVICE_BASE_URL = "http://www.solucionesparche.com/labs/bridgestone/index.php/servicios/";
	private static final String SERVICE_FORMAT = "format/json/";
	private static final String PREFERENCES_FILE = "net.medialabs.bridgestone.PREFERENCE_FILE_KEY";
	private static int idTienda;
	Button guardarDatosBtn;
	Spinner listadoTiendas;
	JSONArray resultArray;
	JSONObject resultObject;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Configuration configuration = new Configuration(this);
		configuration.execute();
		
	}
	
	public void appConfiguration(String[] stores, JSONArray result) {
		setContentView(R.layout.activity_configuration);
		
		listadoTiendas = (Spinner) findViewById(R.id.listadoTiendas);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stores);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		listadoTiendas.setAdapter(adapter);
		
		guardarDatosBtn = (Button) findViewById(R.id.guardarDatosBtn);
		guardarDatosBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int position = listadoTiendas.getSelectedItemPosition();
				if(position != AdapterView.INVALID_POSITION) {
					try {
						resultObject = resultArray.getJSONObject(position);
						idTienda = resultObject.getInt("id");
						Context context = ConfigurationActivity.this;
						SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = preferences.edit();
						editor.putInt("TIENDA_ID", idTienda);
						editor.putBoolean("FIRST_TIME", false);
						editor.commit();
						Intent intent = new Intent(ConfigurationActivity.this, EncuestaActivity.class);
						startActivity(intent);
						ConfigurationActivity.this.finish();
					} catch (JSONException e) {
						e.printStackTrace();
					} 
				}
			}
		});
		
	}
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View view, int pos,
			long id) {
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.configuration, menu);
		return true;
	}
	
	private class Configuration extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog dialog;
		private Alertas alerta = new Alertas();
		private ConfigurationActivity activityRef;
		private String[] tiendas;
		
		public Configuration(ConfigurationActivity activityRef) {
			this.activityRef = activityRef;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(activityRef, "", "Cargando opciones...", true);	
		}
		
		@Override
		protected String doInBackground(Void... params) {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(SERVICE_BASE_URL + "tiendas/all/" + SERVICE_FORMAT);
			get.setHeader("content-type", "application/json");
			
			try {
				HttpResponse response = client.execute(get);
				return EntityUtils.toString(response.getEntity());
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
			if(result == null) {
				alerta.showAlertDialog(activityRef, "Error", "No hay servicios disponibles", false);
				dialog.dismiss();
			} else {
				try {
					resultObject = new JSONObject(result);
					if(resultObject.getString("status").equalsIgnoreCase("OK")) {
						resultArray = resultObject.getJSONArray("response");
						tiendas = new String[resultArray.length()];
						for(int i = 0; i < resultArray.length(); i++) {
							resultObject = resultArray.getJSONObject(i);
							tiendas[i] = resultObject.getString("nombre");
						}
					} else {
						alerta.showAlertDialog(activityRef, "Error", "No hay tiendas disponibles", false);
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				activityRef.appConfiguration(tiendas, resultArray);
				dialog.dismiss();
			}
			
		}
		
	}

}
