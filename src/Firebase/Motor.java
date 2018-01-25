/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Firebase;

/**
 *
 * @author fivc
 */
public class Motor {
    private String Posicion,Temperatura,Voltaje;
    
    public Motor(String Posicion, String Temperatura, String Voltaje){
        this.Posicion=Posicion;
        this.Temperatura=Temperatura;
        this.Voltaje=Voltaje;
    }
    public Motor(){
        Posicion="0";
        Temperatura="0";
        Voltaje="0";
    }
    
    
    public String getPosicion(){
        return Posicion.replaceAll("\\s","");
    }
    
    public String getTemperatura(){
        return Temperatura.replaceAll("\\s","");
    }
    
    public String getVoltaje(){
        return Voltaje.replaceAll("\\s","");
    }
    
    public void setPosicion(String Posicion){
        this.Posicion=Posicion;
    }
    
    public void setTemperatura(String Temperatura){
        this.Temperatura=Temperatura;
    }
    
    public void setVoltaje(String Voltaje){
        this.Voltaje=Voltaje;
    }
}
