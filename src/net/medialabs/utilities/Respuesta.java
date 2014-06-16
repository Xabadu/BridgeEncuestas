package net.medialabs.utilities;

public class Respuesta {
	
	private int encuesta;
	private int pregunta;
	private String detalle;
	
	public Respuesta(int encuesta, int pregunta, String detalle) {
		this.encuesta = encuesta;
		this.pregunta = pregunta;
		this.detalle = detalle;
	}
	
	private int getEncuesta() {
		return encuesta;
	}
	
	private int getPregunta() {
		return pregunta;
	}

	
	private String getDetalle() {
		return detalle;
	}
	

}
