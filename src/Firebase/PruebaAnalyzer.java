/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Firebase;

import java.awt.TrayIcon;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JOptionPane;

/**
 *
 * @author fivc
 */
public class PruebaAnalyzer implements Runnable{

    private final String ruta;
    private final float temp,volt;
    private final ExecutorService exec;
    private final TrayIcon trayIcon;
    private final ArrayList<Motor> motores;
    
    public PruebaAnalyzer(String r, ArrayList<Motor> motores, float temp, float volt, TrayIcon t){
        this.motores=motores;
        ruta=r;
        this.temp=temp;
        this.volt=volt;
        exec=Executors.newFixedThreadPool(7);
        trayIcon=t;
    }
    
    @Override
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
                trayIcon.displayMessage("Mantenimiento SAPBot", "Posible problema en Motor "+(i+1), TrayIcon.MessageType.ERROR);
            if (analizar.get(i).alertaT)
                trayIcon.displayMessage("Mantenimiento SAPBot", "Temperatura Máxima excedida en Motor "+(i+1), TrayIcon.MessageType.ERROR);
            if (analizar.get(i).alertaV)
                trayIcon.displayMessage("Mantenimiento SAPBot", "Voltaje máximo excedido en Motor "+(i+1), TrayIcon.MessageType.ERROR);
        }
    } 
}
