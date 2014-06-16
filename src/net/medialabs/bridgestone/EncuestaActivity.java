package net.medialabs.bridgestone;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import net.medialabs.utilities.Alertas;
import net.medialabs.utilities.Respuesta;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
		Log.d("Total", String.valueOf(resp.size()));
		//listadoRespuestas.clear();
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
		detalle = "";
		try {
			preguntasArray = encuesta.getJSONArray("preguntas");
			preguntaSimple = preguntasArray.getJSONObject(numeroPregunta);
			enunciadoPregunta = (TextView) findViewById(R.id.tituloPregunta);
			enunciadoPregunta.setText(String.valueOf(numeroPregunta+1) + ".-" + preguntaSimple.getString("enunciado"));
			contenedorPreguntas = (LinearLayout) findViewById(R.id.contenedorPreguntas);
			if(preguntaSimple.getString("tipo").equalsIgnoreCase("TEXT")) {
				campoRespuesta = new EditText(this);
				campoRespuesta.setId(R.id.campoRespuesta);
				campoRespuesta.setBackgroundResource(R.drawable.rounded_bg_edittext);
				campoRespuesta.setHint("Ingrese su respuesta");
				campoRespuesta.setTextColor(Color.BLACK);
				contenedorPreguntas.addView(campoRespuesta);
				View v = new View(this);
				v.setLayoutParams(new ViewGroup.LayoutParams(
				        ViewGroup.LayoutParams.WRAP_CONTENT,
				        5));
				contenedorPreguntas.addView(v);
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
					listaRespuestas.setBackgroundResource(R.drawable.rounded_bg_edittext);
					listaRespuestas.setSelection(0);
					contenedorPreguntas.addView(listaRespuestas);
					View v = new View(this);
					v.setLayoutParams(new ViewGroup.LayoutParams(
					        ViewGroup.LayoutParams.WRAP_CONTENT,
					        5));
					contenedorPreguntas.addView(v);
					if(indiceOtros != -1) {
						JSONObject optionValues = opcionesPreguntaArray.getJSONObject(indiceOtros);
						campoRespuesta = new EditText(this);
						campoRespuesta.setHint(optionValues.getString("nombre"));
						campoRespuesta.setBackgroundResource(R.drawable.rounded_bg_edittext);
						campoRespuesta.setTextColor(Color.BLACK);
						contenedorPreguntas.addView(campoRespuesta);
						View v2 = new View(this);
						v2.setLayoutParams(new ViewGroup.LayoutParams(
						        ViewGroup.LayoutParams.WRAP_CONTENT,
						        5));
						contenedorPreguntas.addView(v2);
					}
					
					
				} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("RADIO")) {
					final RadioButton[] rb = new RadioButton[opcionesPreguntaArray.length()];
					RadioGroup rg = new RadioGroup(this);
					rg.setId(R.id.radioGroupRespuesta);
					rg.setOrientation(RadioGroup.VERTICAL);
					rg.setLayoutParams(new ViewGroup.LayoutParams(
					        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
					for(int i = 0; i < opcionesPreguntaArray.length(); i++) {
						JSONObject optionValues = opcionesPreguntaArray.getJSONObject(i);
						if(!optionValues.getString("valor").equalsIgnoreCase("OTROS")) {
							rb[i] = new RadioButton(this);
							rb[i].setText(optionValues.getString("nombre"));
							rb[i].setTextColor(Color.BLACK);
							String radioId = "radio" + String.valueOf(i+1);
							int resId = getResources().getIdentifier(radioId, "id", getPackageName());
							rb[i].setId(resId);
							rb[i].setBackgroundResource(R.drawable.rounded_bg_edittext);
							rb[i].setLayoutParams(new ViewGroup.LayoutParams(
							        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
							rg.addView(rb[i]);
							View v = new View(this);
							v.setLayoutParams(new ViewGroup.LayoutParams(
							        ViewGroup.LayoutParams.WRAP_CONTENT,
							        5));
							rg.addView(v);
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
						campoRespuesta.setHint(optionValues.getString("nombre"));
						campoRespuesta.setBackgroundResource(R.drawable.rounded_bg_edittext);
						campoRespuesta.setTextColor(Color.BLACK);
						scroll.addView(campoRespuesta);
						View v = new View(this);
						v.setLayoutParams(new ViewGroup.LayoutParams(
						        ViewGroup.LayoutParams.WRAP_CONTENT,
						        5));
						scroll.addView(v);
					}
				} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("ICONS")) {
					
					LinearLayout contenedorCaras = new LinearLayout(this);
					contenedorCaras.setOrientation(LinearLayout.HORIZONTAL);
					contenedorCaras.setBackgroundResource(R.drawable.rounded_bg_edittext);
					contenedorCaras.setGravity(Gravity.CENTER);
					ImageButton caraVerde = new ImageButton(this);
					caraVerde.setImageResource(R.drawable.img_face_green);
					caraVerde.setBackgroundColor(Color.TRANSPARENT);
					ImageButton caraAmarilla = new ImageButton(this);
					caraAmarilla.setImageResource(R.drawable.img_face_yellow);
					caraAmarilla.setBackgroundColor(Color.TRANSPARENT);
					ImageButton caraNaranja = new ImageButton(this);
					caraNaranja.setImageResource(R.drawable.img_face_orange);
					caraNaranja.setBackgroundColor(Color.TRANSPARENT);
					ImageButton caraRoja = new ImageButton(this);
					caraRoja.setImageResource(R.drawable.img_face_red);
					caraRoja.setBackgroundColor(Color.TRANSPARENT);
					contenedorCaras.addView(caraVerde);
					contenedorCaras.addView(caraAmarilla);
					contenedorCaras.addView(caraNaranja);
					contenedorCaras.addView(caraRoja);
					contenedorPreguntas.addView(contenedorCaras);
					View v = new View(this);
					v.setLayoutParams(new ViewGroup.LayoutParams(
					        ViewGroup.LayoutParams.WRAP_CONTENT,
					        5));
					contenedorPreguntas.addView(v);
					
					caraVerde.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							numeroPregunta++;
							JSONObject encuestaInfo;
							try {
								encuestaInfo = encuesta.getJSONObject("encuesta");
								idEncuesta = Integer.parseInt(encuestaInfo.getString("id"));
								idPregunta = Integer.parseInt(preguntaSimple.getString("id"));
								detalle = "BUENO";
								Respuesta respuesta = new Respuesta(idEncuesta, idPregunta, detalle);
								listadoRespuestas.add(respuesta);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							if(numeroPregunta < preguntasArray.length()) {
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);
							} else {
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});
					
					caraAmarilla.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							numeroPregunta++;
							JSONObject encuestaInfo;
							try {
								encuestaInfo = encuesta.getJSONObject("encuesta");
								idEncuesta = Integer.parseInt(encuestaInfo.getString("id"));
								idPregunta = Integer.parseInt(preguntaSimple.getString("id"));
								detalle = "REGULAR";
								Respuesta respuesta = new Respuesta(idEncuesta, idPregunta, detalle);
								listadoRespuestas.add(respuesta);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							if(numeroPregunta <= preguntasArray.length()) {
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);
							} else {
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});
					
					caraNaranja.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							numeroPregunta++;
							JSONObject encuestaInfo;
							try {
								encuestaInfo = encuesta.getJSONObject("encuesta");
								idEncuesta = Integer.parseInt(encuestaInfo.getString("id"));
								idPregunta = Integer.parseInt(preguntaSimple.getString("id"));
								detalle = "MALO";
								Respuesta respuesta = new Respuesta(idEncuesta, idPregunta, detalle);
								listadoRespuestas.add(respuesta);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							if(numeroPregunta <= preguntasArray.length()) {
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);
							} else {
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});
					
					caraRoja.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							numeroPregunta++;
							JSONObject encuestaInfo;
							try {
								encuestaInfo = encuesta.getJSONObject("encuesta");
								idEncuesta = Integer.parseInt(encuestaInfo.getString("id"));
								idPregunta = Integer.parseInt(preguntaSimple.getString("id"));
								detalle = "MUY MALO";
								Respuesta respuesta = new Respuesta(idEncuesta, idPregunta, detalle);
								listadoRespuestas.add(respuesta);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							if(numeroPregunta <= preguntasArray.length()) {
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);
							} else {
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});
				}
				
			} 
			
				
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		btnSiguiente.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean enabled = false;
				numeroPregunta++;
				try {
					if(preguntaSimple.getString("tipo").equalsIgnoreCase("TEXT")) { 
						EditText campoR = (EditText) findViewById(R.id.campoRespuesta);
						detalle = campoR.getText().toString();
						if(!detalle.equals("")) {
							enabled = true;
						}
					} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("SELECT")) {
						Spinner spinnerR = (Spinner) findViewById(R.id.spinnerRespuesta);
						int selected = spinnerR.getSelectedItemPosition();
						if(selected != Spinner.INVALID_POSITION) {
							JSONObject optionValues = opcionesPreguntaArray.getJSONObject(selected);
							enabled = true;
							detalle = optionValues.getString("nombre");
						} else {
							EditText campoR = (EditText) findViewById(R.id.campoRespuesta);
							detalle = campoR.getText().toString();
							if(!detalle.equals("")) {
								enabled = true;
							}
						}
					} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("RADIO")) {
						RadioGroup radioG = (RadioGroup) findViewById(R.id.radioGroupRespuesta);
						int radioSelectedId = radioG.getCheckedRadioButtonId();
						if(radioSelectedId != -1) {
							RadioButton radioB = (RadioButton) findViewById(radioSelectedId);
							detalle = radioB.getText().toString();
							enabled = true;
						} else {
							EditText campoR = (EditText) findViewById(R.id.campoRespuesta);
							detalle = campoR.getText().toString();
							if(!detalle.equals("")) {
								enabled = true;
							}
						}						
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				try {
					JSONObject encuestaInfo = encuesta.getJSONObject("encuesta");
					idEncuesta = Integer.parseInt(encuestaInfo.getString("id"));
					idPregunta = Integer.parseInt(preguntaSimple.getString("id"));
					Respuesta resp = new Respuesta(idEncuesta, idPregunta, detalle);
					listadoRespuestas.add(resp);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if(numeroPregunta < preguntasArray.length()) {
					if(enabled) {
						
						mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);
					} else {
						numeroPregunta--;
					}
					
				} else {
					GuardarEncuesta guardar = new GuardarEncuesta();
					guardar.execute();
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
						listadoRespuestas.clear();
						mostrarEncuesta(encuestaObject, 0, listadoRespuestas);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				dialog.dismiss();
			}
			
		}
		
	}
	
	private class GuardarEncuesta extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog dialog;
		private ArrayList<Respuesta> respuestas = new ArrayList<Respuesta>();
		private JSONObject responseObject;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(EncuestaActivity.this, "", "Guardando respuestas...", true);	
		}
		
		@Override
		protected String doInBackground(Void... params) {
			respuestas = listadoRespuestas;
			Log.d("Size", String.valueOf(respuestas.size()));
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(SERVICE_BASE_URL + "encuestas/guardar/" + SERVICE_FORMAT);
			post.setHeader("content-type", "application/json");
			
			JSONObject listaPreguntas = new JSONObject();
			JSONObject listaDetalles = new JSONObject();
			Respuesta singleResp = respuestas.get(0);
			int idEncuesta = singleResp.getEncuesta();
			Context context = EncuestaActivity.this;
			SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
			int idTienda = preferences.getInt("TIENDA_ID", 0);
			for(int i = 0; i < respuestas.size(); i++) {
				Respuesta singleRespObj = respuestas.get(i);
				try {
					listaPreguntas.put(String.valueOf(i), singleRespObj.getPregunta());
					listaDetalles.put(String.valueOf(i), singleRespObj.getDetalle());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
			JSONObject respuestasInfoObject = new JSONObject();
			try {
				respuestasInfoObject.put("encuesta", idEncuesta);
				respuestasInfoObject.put("preguntas", listaPreguntas);
				respuestasInfoObject.put("detalles", listaDetalles);
				respuestasInfoObject.put("tienda", idTienda);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			try {
				StringEntity entity = new StringEntity(respuestasInfoObject.toString());
				post.setEntity(entity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			try {
				HttpResponse resp = client.execute(post);
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
			try {
				responseObject = new JSONObject(result);
				if(responseObject.getString("status").equalsIgnoreCase("ERROR")) {
					Toast.makeText(EncuestaActivity.this, responseObject.getString("response"), Toast.LENGTH_LONG).show();
				} else {
					JSONArray idsResultantes = responseObject.getJSONArray("response");
					int[] ids = new int[idsResultantes.length()];
					for(int i = 0; i < idsResultantes.length(); i++) {
						ids[i] = idsResultantes.getInt(i);
					}
					Intent intent = new Intent(EncuestaActivity.this, RegistroActivity.class);
					intent.putExtra("ids", ids);
					startActivity(intent);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.d("Resultado", result);
			dialog.dismiss();
		}
		
	}

}
