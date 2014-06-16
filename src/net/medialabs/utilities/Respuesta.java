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
	
	public int getEncuesta() {
		return encuesta;
	}
	
	public int getPregunta() {
		return pregunta;
	}

	
	public String getDetalle() {
		return detalle;
	}
	

}
