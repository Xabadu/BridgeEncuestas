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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
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
	private SoundPool sonidoAprobacion;
	private boolean soundLoaded = false;
	private int soundId;

	Cronometro cronometro;

	ImageButton btnSiguiente;
	ImageButton btnVolver;
	ImageView topBar;

	TextView enunciadoPregunta;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sonidoAprobacion = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		sonidoAprobacion.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
		    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				soundLoaded = true;
		    }
		});
		soundId = sonidoAprobacion.load(this, R.raw.bscl_aprueba, 1);

		EncuestaActiva encuesta = new EncuestaActiva(this);
		encuesta.execute();
	}

	private void mostrarEncuesta(final JSONObject encuesta, int numero, ArrayList<Respuesta> resp) {
		cronometro = new Cronometro(600000, 1000);
		cronometro.start();
		listadoRespuestas = resp;
		setContentView(R.layout.activity_encuesta);
		EditText campoRespuesta = null;
		LinearLayout contenedorPreguntas;
		Spinner listaRespuestas;

		RelativeLayout rlT = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
		if(numero % 2 == 0 && numero > 0) {
			rlT.setBackgroundResource(R.drawable.bg_encuestas1);
		} else if(numero % 3 == 0) {
			rlT.setBackgroundResource(R.drawable.bg_encuestas2);
		} else {
			rlT.setBackgroundResource(R.drawable.bg_encuestas);
		}

		topBar = (ImageView) findViewById(R.id.imgTopbar);

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
					btnSiguiente.setVisibility(View.INVISIBLE);
					btnVolver.setVisibility(View.INVISIBLE);
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
							rb[i].setTextSize(24);
							String radioId = "radio" + String.valueOf(i+1);
							int resId = getResources().getIdentifier(radioId, "id", getPackageName());
							rb[i].setId(resId);
							rb[i].setBackgroundResource(R.drawable.rounded_bg_edittext);
							rb[i].setButtonDrawable(android.R.color.transparent);
							rb[i].setPadding(31, 5, 0, 5);
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
					final RadioGroup tempRg = rg;
					LinearLayout scroll = (LinearLayout) findViewById(R.id.linearInsideContenedor);
					scroll.addView(rg);
					if(indiceOtros != -1) {
						btnSiguiente.setVisibility(View.VISIBLE);
						btnVolver.setVisibility(View.VISIBLE);
						JSONObject optionValues = opcionesPreguntaArray.getJSONObject(indiceOtros);
						campoRespuesta = new EditText(this);
						campoRespuesta.setId(R.id.campoRespuesta);
						campoRespuesta.setHint(optionValues.getString("nombre"));
						campoRespuesta.setBackgroundResource(R.drawable.rounded_bg_edittext);
						campoRespuesta.setTextColor(Color.BLACK);
						campoRespuesta.setTextSize(24);
						campoRespuesta.setPadding(31, 5, 0, 5);
						scroll.addView(campoRespuesta);
						View v = new View(this);
						v.setLayoutParams(new ViewGroup.LayoutParams(
						        ViewGroup.LayoutParams.WRAP_CONTENT,
						        5));
						scroll.addView(v);

						campoRespuesta.setOnFocusChangeListener(new OnFocusChangeListener() {

							@Override
							public void onFocusChange(View arg0, boolean arg1) {
								for(int i = 0; i < tempRg.getChildCount(); i++) {
									View view = tempRg.getChildAt(i);
									if(view instanceof RadioButton) {
										RadioButton tempRb = (RadioButton) view;
										tempRb.setBackgroundResource(R.drawable.rounded_bg_edittext);
										tempRb.setButtonDrawable(android.R.color.transparent);
										tempRb.setPadding(31, 5, 0, 5);
									}
								}
							}

						});

					}



					rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(RadioGroup group, int checkedId) {
							for(int i = 0; i < tempRg.getChildCount(); i++) {
								View view = tempRg.getChildAt(i);
								if(view instanceof RadioButton) {
									RadioButton tempRb = (RadioButton) view;
									tempRb.setBackgroundResource(R.drawable.rounded_bg_edittext);
									tempRb.setButtonDrawable(android.R.color.transparent);
									tempRb.setPadding(31, 5, 0, 5);
								}
							}
							RadioButton radioB = (RadioButton) findViewById(checkedId);
							radioB.setBackgroundColor(Color.LTGRAY);

							if(checkedId != -1) {
								detalle = radioB.getText().toString();
							} else {
								EditText campoR = (EditText) findViewById(R.id.campoRespuesta);
								detalle = campoR.getText().toString();
							}
							numeroPregunta++;

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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);
							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}

						}
					});

				} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("ICONS")) {
					btnSiguiente.setVisibility(View.INVISIBLE);
					btnVolver.setVisibility(View.INVISIBLE);
					LinearLayout contenedorCaras = new LinearLayout(this);
					contenedorCaras.setOrientation(LinearLayout.HORIZONTAL);
					contenedorCaras.setBackgroundResource(R.drawable.rounded_bg_edittext);
					contenedorCaras.setGravity(Gravity.CENTER);
					final ImageButton caraVerde = new ImageButton(this);
					caraVerde.setImageResource(R.drawable.green_face_btn);
					caraVerde.setBackgroundColor(Color.TRANSPARENT);
					final ImageButton caraAmarilla = new ImageButton(this);
					caraAmarilla.setImageResource(R.drawable.yellow_face_btn);
					caraAmarilla.setBackgroundColor(Color.TRANSPARENT);
					final ImageButton caraNaranja = new ImageButton(this);
					caraNaranja.setImageResource(R.drawable.orange_face_btn);
					caraNaranja.setBackgroundColor(Color.TRANSPARENT);
					final ImageButton caraRoja = new ImageButton(this);
					caraRoja.setImageResource(R.drawable.red_face_btn);
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
							detalle = "BUENO";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

					caraAmarilla.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "REGULAR";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

					caraNaranja.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "MALO";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

					caraRoja.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "MUY MALO";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

				} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("GRADE")) {
					btnSiguiente.setVisibility(View.INVISIBLE);
					btnVolver.setVisibility(View.INVISIBLE);
					LinearLayout contenedorNotas = new LinearLayout(this);
					contenedorNotas.setOrientation(LinearLayout.VERTICAL);
					contenedorNotas.setBackgroundResource(R.drawable.rounded_bg_notas);
					contenedorNotas.setGravity(Gravity.CENTER);
					LinearLayout primeraFila = new LinearLayout(this);
					primeraFila.setOrientation(LinearLayout.HORIZONTAL);
					primeraFila.setGravity(Gravity.CENTER);
					LinearLayout segundaFila = new LinearLayout(this);
					segundaFila.setOrientation(LinearLayout.HORIZONTAL);
					segundaFila.setGravity(Gravity.CENTER);

					final ImageButton nota1 = new ImageButton(this);
					nota1.setImageResource(R.drawable.img_nota_1);
					nota1.setBackgroundColor(Color.TRANSPARENT);
					final ImageButton nota2 = new ImageButton(this);
					nota2.setImageResource(R.drawable.img_nota_2);
					nota2.setBackgroundColor(Color.TRANSPARENT);
					final ImageButton nota3 = new ImageButton(this);
					nota3.setImageResource(R.drawable.img_nota_3);
					nota3.setBackgroundColor(Color.TRANSPARENT);
					final ImageButton nota4 = new ImageButton(this);
					nota4.setImageResource(R.drawable.img_nota_4);
					nota4.setBackgroundColor(Color.TRANSPARENT);
					final ImageButton nota5 = new ImageButton(this);
					nota5.setImageResource(R.drawable.img_nota_5);
					nota5.setBackgroundColor(Color.TRANSPARENT);
					final ImageButton nota6 = new ImageButton(this);
					nota6.setImageResource(R.drawable.img_nota_6);
					nota6.setBackgroundColor(Color.TRANSPARENT);
					final ImageButton nota7 = new ImageButton(this);
					nota7.setImageResource(R.drawable.img_nota_7);
					nota7.setBackgroundColor(Color.TRANSPARENT);
					final ImageButton nota8 = new ImageButton(this);
					nota8.setImageResource(R.drawable.img_nota_8);
					nota8.setBackgroundColor(Color.TRANSPARENT);
					final ImageButton nota9 = new ImageButton(this);
					nota9.setImageResource(R.drawable.img_nota_9);
					nota9.setBackgroundColor(Color.TRANSPARENT);
					final ImageButton nota10 = new ImageButton(this);
					nota10.setImageResource(R.drawable.img_nota_10);
					nota10.setBackgroundColor(Color.TRANSPARENT);

					primeraFila.addView(nota1);
					primeraFila.addView(nota2);
					primeraFila.addView(nota3);
					primeraFila.addView(nota4);
					primeraFila.addView(nota5);
					segundaFila.addView(nota6);
					segundaFila.addView(nota7);
					segundaFila.addView(nota8);
					segundaFila.addView(nota9);
					segundaFila.addView(nota10);

					contenedorNotas.addView(primeraFila);
					contenedorNotas.addView(segundaFila);

					contenedorPreguntas.addView(contenedorNotas);
					View v = new View(this);
					v.setLayoutParams(new ViewGroup.LayoutParams(
					        ViewGroup.LayoutParams.WRAP_CONTENT,
					        5));
					contenedorPreguntas.addView(v);

					nota1.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "1";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

					nota2.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "2";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

					nota3.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "3";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

					nota4.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "4";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

					nota5.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "5";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

					nota6.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "6";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

					nota7.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "7";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

					nota8.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "8";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

					nota9.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "9";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

					nota10.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							detalle = "10";
							numeroPregunta++;
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
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
								cp.setVisibility(View.INVISIBLE);
								RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
								Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
								animation.reset();
								rl.clearAnimation();
								rl.startAnimation(animation);
								mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);

							} else {
								if(soundLoaded) {
									sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
								}
								GuardarEncuesta guardar = new GuardarEncuesta();
								guardar.execute();
							}
						}
					});

				}

			}
			LinearLayout circulosLayout = (LinearLayout) findViewById(R.id.contenedorCirculos);

			for(int i = 0; i < preguntasArray.length(); i++) {
				ImageView circleImg = new ImageView(this);
				if(i == numeroPregunta) {
					circleImg.setBackgroundResource(R.drawable.circle_white);
				} else if(i - 1 == numeroPregunta || i + 1 == numeroPregunta) {
					circleImg.setBackgroundResource(R.drawable.circle_light_grey);
				} else {
					circleImg.setBackgroundResource(R.drawable.circle_dark_grey);
				}

				circulosLayout.addView(circleImg);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		topBar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EncuestaActiva encuesta = new EncuestaActiva(EncuestaActivity.this);
				encuesta.execute();
			}
		});

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
					} else if(preguntaSimple.getString("tipo").equalsIgnoreCase("ICONS")) {
						if(!detalle.equals("")) {
							enabled = true;
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
						if(soundLoaded) {
							sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
						}
						LinearLayout cp = (LinearLayout) findViewById(R.id.contenedorPreguntas);
						cp.setVisibility(View.INVISIBLE);
						RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeBgEncuestas);
						Animation animation = AnimationUtils.loadAnimation(EncuestaActivity.this, R.anim.alphaout);
						animation.reset();
						rl.clearAnimation();
						rl.startAnimation(animation);
						mostrarEncuesta(encuesta, numeroPregunta, listadoRespuestas);
					} else {
						numeroPregunta--;
						AlertDialog.Builder builder = new AlertDialog.Builder(EncuestaActivity.this);
				        builder.setMessage("Debe seleccionar una respuesta.")
				               .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
				                   public void onClick(DialogInterface dialog, int id) {

				                   }
				               });
				        builder.create();
				        builder.show();
					}

				} else {
					if(soundLoaded) {
						sonidoAprobacion.play(soundId, 1, 1, 0, 0, 1);
					}
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

	public void touch() {
		cronometro.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.encuesta, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.change_store:
	            Intent intent = new Intent(EncuestaActivity.this, ConfigurationActivity.class);
	            startActivity(intent);
	            EncuestaActivity.this.finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	public class Cronometro extends CountDownTimer {

		public Cronometro(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			EncuestaActiva encuesta = new EncuestaActiva(EncuestaActivity.this);
			encuesta.execute();
		}

		@Override
		public void onTick(long millisUntilFinished) {
		}

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
			Log.d("Resultado", result);
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

			dialog.dismiss();
		}

	}

}
