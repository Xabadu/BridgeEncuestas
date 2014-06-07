package net.medialabs.utilities;

public class Respuesta {
	
	private int encuesta;
	private int pregunta;
	private int opcion;
	private String detalle;
	
	public Respuesta(int encuesta, int pregunta, int opcion, String detalle) {
		this.encuesta = encuesta;
		this.pregunta = pregunta;
		this.opcion = opcion;
		this.detalle = detalle;
	}
	
	private int getEncuesta() {
		return encuesta;
	}
	
	private int getPregunta() {
		return pregunta;
	}
	
	private int getOpcion() {
		return opcion;
	}
	
	private String getDetalle() {
		return detalle;
	}
	

}
