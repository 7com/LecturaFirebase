/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

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
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
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
        this.maxTemp = 32;
        this.maxVolt = 11;
        initComponents();
        jButton3.setText("<html><font color='black'>Iniciar Módulo Descarga</font></html>");
        jButton1.setText("<html><font color='black'>Configurar Parámetros</font></html>");
        token = t;
        firebaseURL=url;
        exec = Executors.newFixedThreadPool(1);
        try {
            HideToSystemTray();
        } catch (IOException | AWTException ex) {
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
        this.getContentPane().setBackground(Color.WHITE);
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
                trayIcon.displayMessage("Mantenimiento SAPBot", "Firebase conectado.", TrayIcon.MessageType.INFO);
                firstRun=false;
              } else {
                  if (!firstRun)
                    trayIcon.displayMessage("Mantenimiento SAPBot", "Firebase se ha desconectado. Revisar conexión a Internet.", TrayIcon.MessageType.WARNING);
                  else
                    trayIcon.displayMessage("Mantenimiento SAPBot", "Conectando a Firebase...", TrayIcon.MessageType.INFO);
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
                d = ds.child("maxVolt");
                if (d.getValue()!= null)
                     volt = Float.parseFloat(d.getValue(String.class));
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
                for(DataSnapshot snap : ds.getChildren()){
                //Loop to go through all the child nodes
                    System.out.println(snap.getKey()+" - "+snap.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError de) {
                JOptionPane.showMessageDialog(null,de.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            } 
        };
        ref.addValueEventListener(Lectura);
        
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
        
        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                if(e.getNewState()==ICONIFIED){
                    setVisible(false);
                }
                if(e.getNewState()==7){
                    setVisible(false);
                    }
                if(e.getNewState()==MAXIMIZED_BOTH){
                            setVisible(true);
                            toFront();
                        }
                        if(e.getNewState()==NORMAL){
                            setVisible(true);
                            toFront();
                        }
            }
        });
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Protipo Mant. Predictivo para SAPBot");
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
        jButton3.setText("Iniciar Modulo Descarga");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

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
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    /**
     * @param args the command line arguments
     */
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
