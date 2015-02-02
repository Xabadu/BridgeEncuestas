package net.medialabs.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

public class Validador {
	
	Context context;
	
	public Validador(Context c) {
		context = c;
	}
	
	public boolean email(String e) {
		String email = e;
		
		String PATTERN_EMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
	            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		
		Pattern pattern = Pattern.compile(PATTERN_EMAIL);
		 
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();		
	}
	
	public boolean rut(String r) {
		String rut = r;
		try {
			r = r.replace(".", "");
			r = r.replace("-", "");
			int rutAux = Integer.parseInt(rut.substring(0, rut.length() - 1));
			
			char dv = rut.charAt(rut.length() - 1);
			int m = 0, s = 1;
			
			for (; rutAux != 0; rutAux /= 10) {  
				s = (s + rutAux % 10 * (9 - m++ % 6)) % 11;  
			}
			
			if (dv == (char) (s != 0 ? s + 47 : 75)) {  
				return true;  
			}
			
		} catch(java.lang.NumberFormatException e) {
			return false;
		} catch(Exception e) {
			return false;
		}
		return false;
	}
	
	public boolean telefono(String t) {
		String telefono = t;
		if(telefono.length() == 8) {
			return true;
		}
		return false;
	}
	
	public boolean patente(String p) {
		String patente = p;
		if(patente.length() != 6) {
			return false;
		}
		if(patente.contains("-")) {
			return false;
		}
		if(patente.contains(".")) {
			return false;
		}
		return true;
	}

}
