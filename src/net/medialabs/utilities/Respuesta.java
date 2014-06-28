package net.medialabs.utilities;

public class Respuesta {
	
	private int encuesta;
	private int pregunta;
	private String detalle;
	
	public Respuesta(int encuesta, int pregunta, String detalle) {
		this.encuesta = encuesta;
		this.pregunta = pregunta;
		this.detalle = limpiarDetalle(detalle);
	}
	
	public int getEncuesta() {
		return encuesta;
	}
	
	public int getPregunta() {
		return pregunta;
	}

	
	public String getDetalle() {
		return detalle;
	}
	
	public String limpiarDetalle(String d) {
		
		String cadenaSucia = "‡ˆŠ‘’“•—˜šœu–çË€ƒéèêíìîñ…òô†„‚";
		String cadenaLimpia = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
		String salida = d;
		
		for(int i = 0; i < cadenaSucia.length(); i++) {
			salida = salida.replace(cadenaSucia.charAt(i), cadenaLimpia.charAt(i));
		}
		
		return salida;
		
	}
}
