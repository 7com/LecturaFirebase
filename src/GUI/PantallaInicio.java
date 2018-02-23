/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import Motor.Motor;
import Motor.PruebaAnalyzer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author fivc
 */
public class PantallaInicio extends javax.swing.JFrame {
    private String token;
    private TrayIcon trayIcon;
    private SystemTray tray;
    private String firebaseURL;
    private ExecutorService exec;
    private boolean firstRun;
    private float maxTemp, maxVolt;
    /**
     * Creates new form PantallaInicio
     */
    public PantallaInicio(String t,String url) {
        this.firstRun = true;
        this.maxTemp = 34;
        this.maxVolt = 11;
        initComponents();
        jButton3.setText("<html><font color='black'>Iniciar Módulo Descarga</font></html>");
        jButton1.setText("<html><font color='black'>Configurar Parámetros</font></html>");
        jButton5.setText("<html><font color='black'>Abrir LOG</font></html>");
        token = t;
        firebaseURL=url;
        exec = Executors.newCachedThreadPool();
        try {
            HideToSystemTray();
        } catch (IOException | AWTException ex) {
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
        this.getContentPane().setBackground(Color.WHITE);
        while(true)
        {
            if (escribir())
                break;
        }
        firebase();
    }
    
    public void setMax(float temp, float volt){
        maxTemp=temp;
        maxVolt=volt;
    }
    
    public float getTemp(){
        return maxTemp;
    }
    
    public float getVolt(){
        return maxVolt;
    }
    
    private void firebase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref;
        
        ref = database.getReference(".info/connected");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
              boolean connected = snapshot.getValue(Boolean.class);
              if (connected) {
                    jLabel3.setText("<html><font color='blue'>Conectado</font></html>");
                    firstRun=false;
              } else {
                  if (!firstRun){
                    trayIcon.displayMessage("Mantenimiento SAPBot", "Firebase se ha desconectado. Revisar conexión a Internet.", TrayIcon.MessageType.WARNING);
                    jLabel3.setText("<html><font color='red'>Reintentando Conectar</font></html>");     
                }     
              }
            }

            @Override
            public void onCancelled(DatabaseError error) {
              JOptionPane.showMessageDialog(null,error.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        });
        
        ref = database.getReference("*Config");
        ValueEventListener cargarCFG = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                float temp=getTemp();
                float volt=getVolt();
                DataSnapshot d = ds.child("maxTemp");
                if (d.getValue()!= null)
                     temp = Float.parseFloat(d.getValue(String.class));
                else{
                    DatabaseReference dr = d.getRef();
                    dr.setValue(Float.toString(temp));
                }
                    
                d = ds.child("maxVolt");
                if (d.getValue()!= null)
                     volt = Float.parseFloat(d.getValue(String.class));
                else{
                    DatabaseReference dr = d.getRef();
                    dr.setValue(Float.toString(volt));
                }
                setMax(temp,volt);
            }

            @Override
            public void onCancelled(DatabaseError de) {
                JOptionPane.showMessageDialog(null,de.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            } 
        };
        ref.addListenerForSingleValueEvent(cargarCFG);
        
        ref = database.getReference("*Sin Procesar");
        ValueEventListener Lectura = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) { 
                if (ds.getChildrenCount() != 0){
                    trayIcon.displayMessage("Mantenimiento SAPBot","Nuevas pruebas detectadas. Analizando...", TrayIcon.MessageType.INFO);
                    for(DataSnapshot snap : ds.getChildren()){
                    //Loop to go through all the child nodes
                        String ruta = snap.getKey()+"/"+snap.getValue(String.class);
                        DatabaseReference r = ds.getRef().getRoot();
                        r = r.child(ruta);
                        ArrayList<Motor>motores = new ArrayList<>();
                        r.addListenerForSingleValueEvent(new ValueEventListener(){
                            @Override
                            public void onDataChange(DataSnapshot ds) {
                                for (int i=1;i<=7;i++){   
                                    DataSnapshot d = ds.child("Motor "+i);
                                    if (d.getValue() != null)
                                        motores.add(d.getValue(Motor.class));
                                }
                                PruebaAnalyzer prueba = new PruebaAnalyzer(snap.getKey(),snap.getValue(String.class),motores,maxTemp,maxVolt,trayIcon);
                                prueba.run();
                            }

                            @Override
                            public void onCancelled(DatabaseError de) {
                                JOptionPane.showMessageDialog(null,de.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                    ds.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError de) {
                JOptionPane.showMessageDialog(null,de.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            } 
        };
        ref.addValueEventListener(Lectura);
    }

    private boolean escribir()
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
            if(!archivo.exists()){
                bw = new BufferedWriter(new FileWriter(archivo));
                bw.write("Log Iniciado");
                bw.write(System.lineSeparator()+"**********************************************");
                bw.close();
            }
            
            return true;
        } catch (IOException ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }
    
    private void HideToSystemTray() throws IOException, AWTException{
        Image image = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("Imagenes/icon.png"));
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
        }catch(Exception e){
        }
        if(SystemTray.isSupported()){
            tray=SystemTray.getSystemTray();

            ActionListener exitListener=new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting....");
                    System.exit(0);
                }
            };
            PopupMenu popup=new PopupMenu();
            MenuItem defaultItem=new MenuItem("Abrir");
            defaultItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(true);
                    toFront();
                    repaint();
                    setState(Frame.NORMAL);
                }
            });
            popup.add(defaultItem);
            defaultItem=new MenuItem("Salir");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);
            
            trayIcon=new TrayIcon(image, "Prototipo Mant. Predictivo", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e){
                  if (e.getClickCount() == 2) {
                    setVisible(true);
                    toFront();
                    repaint();
                    setState(Frame.NORMAL);
                  }
                }
            });
        }
        else{
            System.out.println("system tray not supported");
        }
        

        setIconImage(image);
        tray.add(trayIcon);
    }
    
    public void setButton3(boolean b){
        jButton3.setEnabled(b);
    }
    public void setButton1(boolean b){
        jButton1.setEnabled(b);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Mant. Predictivo para SAPBot");
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);

        jButton1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jButton1.setText("Configurar Parámetros");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jButton2.setText("Salir");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Logo.png"))); // NOI18N

        jButton3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jButton3.setText("Iniciar Módulo Descarga");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        jButton4.setText("Minimizar en Barra de Tareas");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        jButton5.setText("Abrir LOG");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        jLabel2.setText("Estado Firebase:");

        jLabel3.setText("Conectando...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        setButton3(false);
        exec.execute(new PantallaDescarga(token,firebaseURL,this));
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setButton1(false);
        Configuracion c = new Configuracion(this);
        c.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        setVisible(false);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        try {
            // TODO add your handling code here:
            Date date = new Date();
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int year  = localDate.getYear();
            int month = localDate.getMonthValue();
            int day   = localDate.getDayOfMonth();
            String ruta = "Logs"+File.separator+day+"-"+month+"-"+year+".log";
            File f = new File (ruta);
            java.awt.Desktop.getDesktop().open(f);
        } catch (IOException | IllegalArgumentException ex ) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * @param args the command line arguments
     */
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    // End of variables declaration//GEN-END:variables
}
