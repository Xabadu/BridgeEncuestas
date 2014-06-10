package net.medialabs.bridgestone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class RegistroActivity extends Activity {

	ImageButton btnCancelar;
	ImageButton btnGuardar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registro);
		
		btnCancelar = (ImageButton) findViewById(R.id.btnCancelarRegistro);
		btnGuardar = (ImageButton) findViewById(R.id.btnGuardarRegistro);
		
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

}
