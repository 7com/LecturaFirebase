/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Motor;

import java.awt.TrayIcon;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JOptionPane;

/**
 *
 * @author fivc
 */
public class PruebaAnalyzer{

    private final TrayIcon trayIcon;
    public ArrayList<String> problemas;
    private final String nombre;
    private final String fecha;
    private final float temp,volt;
    private final ExecutorService exec;
    private final ArrayList<Motor> motores;
    
    public PruebaAnalyzer(String n, String f, ArrayList<Motor> motores, float temp, float volt, TrayIcon trayIcon){
        this.trayIcon = trayIcon;
        fecha=f;
        this.motores=motores;
        nombre=n;
        this.temp=temp;
        this.volt=volt;
        exec=Executors.newFixedThreadPool(7);
        problemas = new ArrayList<>();
    }
        
    public void run() {
        ArrayList<MotorAnalyzer> analizar= new ArrayList<>();
        CountDownLatch la = new CountDownLatch(motores.size());
        for (int i=0;i<motores.size();i++){
            analizar.add(new MotorAnalyzer(motores.get(i),temp,volt,la));
            exec.execute(analizar.get(i));
        }
        try {
            la.await();
        } catch (InterruptedException E) {
             JOptionPane.showMessageDialog(null, E.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        for (int i=0;i<analizar.size();i++)
        {
            if (analizar.get(i).alerta)
                problemas.add("Posible problema eléctrico en Motor "+(i+1)+".");
            if (analizar.get(i).alertaT)
                problemas.add("Temperatura Máxima excedida en Motor "+(i+1)+".");
            if (analizar.get(i).alertaV)
                problemas.add("Voltaje máximo excedido en Motor "+(i+1)+".");
        }
        while(true)
        {
            if (escribir(problemas))
                break;
        }
        if(!problemas.isEmpty())
            trayIcon.displayMessage("Mantenimiento SAPBot", nombre+" con problemas. Revisar LOG", TrayIcon.MessageType.ERROR);
        else
            trayIcon.displayMessage("Mantenimiento SAPBot", "Analisis de "+nombre+" finalizada", TrayIcon.MessageType.INFO);
    } 
    
    private boolean escribir(ArrayList<String> s)
    {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year  = localDate.getYear();
        int month = localDate.getMonthValue();
        int day   = localDate.getDayOfMonth();
        String ruta = "Logs"+File.separator+day+"-"+month+"-"+year+".log";
        try
        {
            File directory = new File("Logs");
            if (! directory.exists()){
                directory.mkdir();
            }
            File archivo = new File(ruta);
            BufferedWriter bw;
            if(archivo.exists()) {
                bw = new BufferedWriter(new FileWriter(archivo, true));
                if(!problemas.isEmpty()){
                    bw.write(System.lineSeparator()+nombre+"/"+fecha);
                    for (int i=0; i<s.size(); i++){
                        bw.write(System.lineSeparator()+s.get(i));
                    }
                }
                else
                    bw.write(System.lineSeparator()+nombre+"/"+fecha+" no presenta problemas.");
                bw.write(System.lineSeparator()+"**********************************************");
            } else {
                bw = new BufferedWriter(new FileWriter(archivo));
                if(!problemas.isEmpty()){
                    bw.write(nombre+"/"+fecha);
                    for (int i=0; i<s.size(); i++){
                        bw.write(System.lineSeparator()+s.get(i));
                    }
                }
                else
                    bw.write(nombre+"/"+fecha+" no presenta problemas.");
                bw.write(System.lineSeparator()+"**********************************************");
            }
            bw.close();
            return true;
        } catch (IOException ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }
}
