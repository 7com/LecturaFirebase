/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Firebase;

import static java.lang.Math.abs;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JOptionPane;

/**
 *
 * @author fivc
 */
public class MotorAnalyzer implements Runnable{
    private final Motor motor;
    private final float temp, volt;
    private final ExecutorService exec;
    public boolean alertaT=false;
    public boolean alertaV=false;
    public boolean alerta=false;
    private final CountDownLatch latch;
    
    public MotorAnalyzer(Motor m, float temp, float volt, CountDownLatch latch){
        motor=m;
        this.temp=temp;
        this.volt=volt;
        exec = Executors.newFixedThreadPool(3);
        this.latch = latch;
    }
    
    @Override
    public void run() {
        CountDownLatch la = new CountDownLatch(3);
        TempAnalyzer t = new TempAnalyzer(motor.getTemperatura(),temp,la);
        VoltAnalyzer v = new VoltAnalyzer(motor.getVoltaje(),volt,la);
        PosAnalyzer p = new PosAnalyzer(motor.getPosicion(),motor.getVoltaje(),la);
        exec.execute(p);
        exec.execute(t);
        exec.execute(v);
        try {
            la.await();
        } catch (InterruptedException E) {
             JOptionPane.showMessageDialog(null, E.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        alertaT=t.alerta;
        alertaV=v.alerta;
        alerta=p.alerta;
        latch.countDown();
    }
    
}

class TempAnalyzer implements Runnable{
    private String temp;
    private final float max;
    public boolean alerta = false;
    private final CountDownLatch latch;
    
    public TempAnalyzer(String t, float max, CountDownLatch latch){
        temp=t;
        this.max = max;
        this.latch=latch;
    }
    @Override
    public void run() {
        int contador=0;
        temp=temp.replaceAll(" ", "");
        String[] tmp = temp.split(",");
        for (int i=0; i<tmp.length; i++){
            if (Float.parseFloat(tmp[i]) > max)
                contador++;
        }
        int total = tmp.length;
        float por = (contador*100)/total;
        if (por >=2)
            alerta = true;
        latch.countDown();
    }
}

class VoltAnalyzer implements Runnable{
    
    private String volt;
    private final float max;
    public boolean alerta = false;
    private final CountDownLatch latch;
    
    public VoltAnalyzer(String v, float max, CountDownLatch latch){
        volt=v;
        this.max = max;
        this.latch=latch;
    }
    
    @Override
    public void run() {
        int contador=0;
        volt=volt.replaceAll(" ", "");
        String[] tmp = volt.split(",");
        for (int i=0; i<tmp.length; i++){
            if (Float.parseFloat(tmp[i]) > max)
                contador++;
        }
        int total = tmp.length;
        float por = (contador*100)/total;
        if (por >=2)
            alerta = true;
        latch.countDown();
    }
    
}

class PosAnalyzer implements Runnable{
    
    private String pos;
    private String volt;
    public boolean alerta = false;
    private final CountDownLatch latch;
    
    public PosAnalyzer(String p, String v, CountDownLatch latch){
        pos=p;
        volt=v;
        this.latch = latch;
    }
    
    private boolean dif(float a, float b){
        float f;
        if (a>=b)
            f=abs(a-b);
        else
            f=abs(b-a);
        return f>1;
    }
    
    @Override
    public void run() {
        pos=pos.replaceAll(" ", "");
        volt=volt.replaceAll(" ", "");
        String[] v = volt.split(",");
        String[] p = pos.split(",");
        int contador=0;
        for(int i=1;i<p.length;i++){
        
            if(p[i-1].equalsIgnoreCase(p[i])){
                float a = Float.parseFloat(v[i-1]);
                float b = Float.parseFloat(v[i]);
                if (dif(a,b)){
                    contador++;
                }
            }   
            i++;
        }
        int total = p.length;
        float por = (contador*100)/total;
        if (por >=2)
            alerta = true;
        latch.countDown();
        
    }
    
}