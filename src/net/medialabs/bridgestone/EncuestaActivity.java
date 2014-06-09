package net.medialabs.bridgestone;

import java.io.IOException;
import java.util.ArrayList;

import net.medialabs.utilities.Alertas;
import net.medialabs.utilities.Respuesta;

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
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class EncuestaActivity extends Activity {
	
	private static final String SERVICE_BASE_URL = "http://www.solucionesparche.com/labs/bridgestone/index.php/servicios/";
	private static final String SERVICE_FORMAT = "format/json/";
	private static final String PREFERENCES_FILE = "net.medialabs.bridgestone.PREFERENCE_FILE_KEY";
	private JSONArray preguntasArray;
	private JSONArray opcionesArray;
	private JSONArray opcionesPreguntaArray;
	private JSONObject resultObject;
	private JSONObject preguntaSimple;
	private int indiceOtros;
	private int numeroPregunta;
	private int idEncuesta = -1;
	private int idPregunta = -1;
	private int opcion = -1;
	private String detalle = "";
	private ArrayList<Respuesta> listadoRespuestas = new ArrayList<Respuesta>();
	
	ImageButton btnSiguiente;
	ImageButton btnVolver;
	
	TextView enunciadoPregunta;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		EncuestaActiva encuesta = new EncuestaActiva(this);
		encuesta.execute();
	}
	
	private void mostrarEncuesta(final JSONObject encuesta, int numero, ArrayList<Respuesta> resp) {
		listadoRespuestas.clear();
		listadoRespuestas = resp;
		setContentView(R.layout.activity_encuesta);
		EditText campoRespuesta = null;
		LinearLayout contenedorPreguntas;
		Spinner listaRespuestas;
		numeroPregunta = numero;
		indiceOtros = -1;
		btnSiguiente = (ImageButton) findViewById(R.id.btnSiguiente);
		btnVolver = (ImageButton) findViewById(R.id.btnVolver);
		idEncuesta = -1;
		idPregunta = -1;
		opcion = -1;
		detalle = "";
		try {
			preguntasArray = encuesta.getJSONArray("preguntas");
			preguntaSimple = preguntasArray.getJSONObject(numeroPregunta);
			enunciadoPregunta = (TextView) findViewById(R.id.tituloPregunta);
			enunciadoPregunta.setText(preguntaSimple.getString("enunciado"));
			contenedorPreguntas = (LinearLayout) findViewById(R.id.contenedorPreguntas);
			if(preguntaSimple.getString("tipo").equalsIgnoreCase("TEXT")) {
				campoRespuesta = new EditText(this);
				campoRespuesta.setId(R.id.campoRespuesta);
				campoRespuesta.setWidth(LayoutParams.MATCH_PARENT);
				campoRespuesta.setHeight(LayoutParams.WRAP_CONTENT);
				//campoRespuesta.setBackgroundColor(Color.WHITE);
				contenedorPreguntas.addView(campoRespuesta);
			} else {
				opcionesArray = encuesta.getJSONArray("opciones");
				opcionesPreguntaArray = opcionesArray.getJSONArray(numeroPregunta);
				
				if(preguntaSimple.getString("tipo").equalsIgnoreCase("SELECT")) {
					ArrayList<String> optionsList = new ArrayList<String>();
					optionsList.add("Seleccione una alternativa");
					for(int i = 0; i < opcionesPreguntaArray.length(); i++) {
						JSONObject optionValues = opcionesPreguntaArray.getJSONObject(i);
						if(!optionValues.getString("valor").equalsIgnoreCase("OTROS")) {
							optionsList.add(optionValues.getString("nombre"));
						} else {
							indiceOtros = i;
						}
					}
					listaRespuestas = new Spinner(this);
					listaRespuestas.setId(R.id.spinnerRespuesta);
					ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, optionsList);
					listaRespuestas.setAdapter(spinnerArrayAdapter);
					contenedorPreguntas.addView(listaRespuestas);
					if(indiceOtros != -1) {
						JSONObject optionValues = opcionesPreguntaArray.getJSONObject(indiceOtros);
						campoRespuesta = new EditText(this);
						campoRespuesta.setWidth(LayoutParams.MATCH_PARENT);
						campoRespuesta.setHeight(LayoutParams.WRAP_CONTENT);
						campoRespuesta.setHint(optionValues.getString("nombre"));
						//campoRespuesta.setBackgroundColor(Color.WHITE);
						contenedorPreguntas.addView(campoRespuesta);
					}
					
					
				} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("RADIO")) {
					final RadioButton[] rb = new RadioButton[opcionesPreguntaArray.length()];
					RadioGroup rg = new RadioGroup(this);
					rg.setId(R.id.radioGroupRespuesta);
					rg.setOrientation(RadioGroup.VERTICAL);
					for(int i = 0; i < opcionesPreguntaArray.length(); i++) {
						JSONObject optionValues = opcionesPreguntaArray.getJSONObject(i);
						if(!optionValues.getString("valor").equalsIgnoreCase("OTROS")) {
							rb[i] = new RadioButton(this);
							rb[i].setText(optionValues.getString("nombre"));
							rb[i].setTextColor(Color.BLACK);
							rg.addView(rb[i]);
						} else {
							indiceOtros = i;
						}
						
					}
					LinearLayout scroll = (LinearLayout) findViewById(R.id.linearInsideContenedor);
					scroll.addView(rg);
					if(indiceOtros != -1) {
						JSONObject optionValues = opcionesPreguntaArray.getJSONObject(indiceOtros);
						campoRespuesta = new EditText(this);
						campoRespuesta.setId(R.id.campoRespuesta);
						campoRespuesta.setWidth(LayoutParams.MATCH_PARENT);
						campoRespuesta.setHeight(LayoutParams.WRAP_CONTENT);
						campoRespuesta.setHint(optionValues.getString("nombre"));
						//campoRespuesta.setBackgroundColor(Color.WHITE);
						scroll.addView(campoRespuesta);
					}
				} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("CHECKBOX")) {
					LinearLayout scroll = (LinearLayout) findViewById(R.id.linearInsideContenedor);
					for(int i = 0; i < opcionesPreguntaArray.length(); i++) {
						JSONObject optionValues = opcionesPreguntaArray.getJSONObject(i);
						if(!optionValues.getString("valor").equalsIgnoreCase("OTROS")) {
							CheckBox cb = new CheckBox(this);
							cb.setText(optionValues.getString("nombre"));
							scroll.addView(cb);
						} else {
							indiceOtros = i;
						}
						
					}
					if(indiceOtros != -1) {
						JSONObject optionValues = opcionesPreguntaArray.getJSONObject(indiceOtros);
						campoRespuesta = new EditText(this);
						campoRespuesta.setWidth(LayoutParams.MATCH_PARENT);
						campoRespuesta.setHeight(LayoutParams.WRAP_CONTENT);
						campoRespuesta.setHint(optionValues.getString("nombre"));
						//campoRespuesta.setBackgroundColor(Color.WHITE);
						scroll.addView(campoRespuesta);
					}	
				} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("ICONS")) {
					
				}
				
			} 
			
				
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		btnSiguiente.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean enabled = false;
				numeroPregunta++;
				if(numeroPregunta < preguntasArray.length()) {
					/*
					try {
						if(preguntaSimple.getString("tipo").equalsIgnoreCase("TEXT")) { 
							EditText campoR = (EditText) findViewById(R.id.campoRespuesta);
							detalle = campoR.getText().toString();
							if(!detalle.equals("")) {
								opcion = 0;
								enabled = true;
							}
						} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("SELECT")) {
							Spinner spinnerR = (Spinner) findViewById(R.id.spinnerRespuesta);
							int selected = spinnerR.getSelectedItemPosition();
							if(selected != Spinner.INVALID_POSITION) {
								JSONObject optionValues = opcionesPreguntaArray.getJSONObject(selected);
								enabled = true;
								opcion = Integer.parseInt(optionValues.getString("id"));
								detalle = optionValues.getString("nombre");
							}	
						} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("RADIO")) {
							
						} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("CHECKBOX")) {
							
						} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("ICONS")) {
							
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if(enabled) {
						numeroPregunta++;
						mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);
					} */
					
					mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);
				} else {
					Intent intent = new Intent(EncuestaActivity.this, RegistroActivity.class);
					startActivity(intent);
				}
			}
		});
		
		btnVolver.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EncuestaActiva encuesta = new EncuestaActiva(EncuestaActivity.this);
				encuesta.execute();
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.encuesta, menu);
		return true;
	}
	
	private class EncuestaActiva extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog dialog;
		private Alertas alerta = new Alertas();
		private EncuestaActivity activityRef;
		private JSONObject encuestaObject;
		
		public EncuestaActiva(EncuestaActivity activityRef) {
			this.activityRef = activityRef;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(activityRef, "", "Cargando encuesta...", true);	
		}
		
		@Override
		protected String doInBackground(Void... params) {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(SERVICE_BASE_URL + "encuestas/activa/" + SERVICE_FORMAT);
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
						encuestaObject = resultObject.getJSONObject("response");
						mostrarEncuesta(encuestaObject, 0, listadoRespuestas);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				dialog.dismiss();
			}
			
		}
		
	}

}
