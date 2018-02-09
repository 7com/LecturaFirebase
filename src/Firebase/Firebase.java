/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 *
 * @author fivc
 */
public class Firebase implements Runnable{

    private JTree jTree1;
    private int op;
    private TreePath selPath;
    private String ruta;
    private String token;
    
    @Override
    public void run() {
        if (op==0)
        {
            String s=selPath.toString();
            s=(String)s.subSequence(0, s.length()-1);
            String[] lista = s.split(",");
            s=lista[1].substring(1)+"/"+lista[2].substring(1);
            ruta=s;
            s="https://sapbot-001.firebaseio.com/"+s+"/";
            crearXLS();

        }
        else
        {
            descargarJSON();
        }
    }
    
    public Firebase(JTree j,int i, TreePath t, String to)
    {
        jTree1=j;
        op=i;
        selPath=t;
        token=to;
    }
    
    private void crearXLS(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(ruta);
        ArrayList<Motor> motores = new ArrayList<Motor>();
        ValueEventListener cargarMotores = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                JProgressBar pb = new JProgressBar(0,100);
                pb.setPreferredSize(new Dimension(250,40));
                pb.setString("Preparando...");
                pb.setStringPainted(true);
                pb.setValue(0); 

                JPanel center_panel = new JPanel();
                center_panel.add(pb);

                JDialog dialog = new JDialog((JFrame)null, "Trabajando ...");
                dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                dialog.getContentPane().add(center_panel, BorderLayout.CENTER);
                dialog.pack();
                dialog.setVisible(true);

                dialog.setLocationRelativeTo(null); // center on screen
                dialog.toFront(); // raise above other java windows
                
                for (int i=1;i<=7;i++){   
                    DataSnapshot d = ds.child("Motor "+i);
                    if (d.getValue() != null)
                        motores.add(d.getValue(Motor.class));
                }

                HSSFWorkbook excel = new HSSFWorkbook();
                for (int i=0; i<motores.size();i++)
                {
                    pb.setString("Procesando Motor "+(i+1));
                    String[] temp,volt,enco;
                    temp=motores.get(i).getTemperatura().split(",");
                    volt=motores.get(i).getVoltaje().split(",");
                    enco=motores.get(i).getPosicion().split(",");
                    Sheet hoja = excel.createSheet("Motor "+(i+1));
                    Row fila = hoja.createRow((short)0);
                    fila.createCell(0).setCellValue("Posicion");
                    fila.createCell(1).setCellValue("Temperatura");
                    fila.createCell(2).setCellValue("Voltaje");
                    for (int j=1;j<=enco.length;j++)
                    {
                        fila = hoja.createRow((short)j);
                        fila.createCell(0).setCellValue(parseInt(enco[j-1]));
                        fila.createCell(1).setCellValue(parseFloat(temp[j-1]));
                        fila.createCell(2).setCellValue(parseFloat(volt[j-1]));
                    }
                    pb.setValue(pb.getValue()+14);
                }
                JFileChooser chooser = new JFileChooser();
                chooser.addChoosableFileFilter(new FileFilter() {

                    public String getDescription() {
                        return "Archivo Formato XLS (*.xls)";
                    }

                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        } else {
                            return f.getName().toLowerCase().endsWith(".xls");
                        }
                    }
                });
                chooser.setAcceptAllFileFilterUsed(false);
                ruta=ruta.replaceAll("/", " - ");
                ruta=ruta.replaceAll(":", "_");
                chooser.setSelectedFile(new File(ruta));
                if (chooser.showSaveDialog(jTree1) == JFileChooser.APPROVE_OPTION)
                {
                    pb.setString("Creando XLS");
                    pb.setValue(99);
                    File archivo = chooser.getSelectedFile();
                    if (!archivo.getAbsolutePath().endsWith(".xls")){
                        archivo = new File(chooser.getSelectedFile() + ".xls");
                    }
                    if (archivo.exists()) {
                        int response = JOptionPane.showConfirmDialog(null, //
                                "¿Desea sobreescribir el archivo?", //
                                "El archivo ya existe", JOptionPane.YES_NO_OPTION, //
                                JOptionPane.QUESTION_MESSAGE);
                        if (response == JOptionPane.YES_OPTION) {
                            archivo.delete();
                            try (FileOutputStream fileOut = new FileOutputStream(archivo)) {
                                excel.write(fileOut);
                            } catch (FileNotFoundException ex) {
                                JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
                            }
                            if (archivo.exists()){
                                JOptionPane.showMessageDialog(jTree1,"El archivo "+archivo.getName()+" ha sido creado.","Aviso",JOptionPane.INFORMATION_MESSAGE);
                            }
                        } 
                    }
                    else
                    {
                        try (FileOutputStream fileOut = new FileOutputStream(archivo)) {
                            excel.write(fileOut);
                        } catch (FileNotFoundException ex) {
                            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
                        }
                        if (archivo.exists()){
                            JOptionPane.showMessageDialog(jTree1,"El archivo "+archivo.getName()+" ha sido creado.","Aviso",JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
                dialog.dispose();
                
            }

            @Override
            public void onCancelled(DatabaseError de) {
                JOptionPane.showMessageDialog(null,de.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        };
        ref.addListenerForSingleValueEvent(cargarMotores);
        
    }
    
    private void descargarJSON(){
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new FileFilter() {
            public String getDescription() {
                return "Archivo Formato JSON (*.json)";
            }

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    return f.getName().toLowerCase().endsWith(".json");
                }
            }
        });
        chooser.setAcceptAllFileFilterUsed(false);
        String s=selPath.toString();
        s=(String)s.subSequence(0, s.length()-1);
        String[] lista = s.split(",");
        s=lista[1].substring(1)+"/"+lista[2].substring(1);
        String temp=s;
        s="https://sapbot-001.firebaseio.com/"+s+".json?print=pretty&access_token="+token;
        temp=temp.replaceAll("/", " - ");
        temp=temp.replaceAll(":", "_");
        chooser.setSelectedFile(new File(temp));
        if (chooser.showSaveDialog(jTree1) == JFileChooser.APPROVE_OPTION)
        {

            File archivo = chooser.getSelectedFile();
            if (!archivo.getAbsolutePath().endsWith(".json")){
                archivo = new File(chooser.getSelectedFile() + ".json");
            }                   
            if (archivo.exists()) {
                int response = JOptionPane.showConfirmDialog(null, //
                    "¿Desea sobreescribir el archivo?", //
                    "El archivo ya existe", JOptionPane.YES_NO_OPTION, //
                    JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    try {
                        archivo.delete();
                        FileUtils.copyURLToFile(new URL(s), archivo);
                    } catch (MalformedURLException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                    if (archivo.exists()){
                        JOptionPane.showMessageDialog(jTree1,"El archivo "+archivo.getName()+" ha sido creado.","Aviso",JOptionPane.INFORMATION_MESSAGE);
                    }
                } 
            }
            else
            {
                try {
                    FileUtils.copyURLToFile(new URL(s), archivo);
                } catch (MalformedURLException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
                if (archivo.exists()){
                    JOptionPane.showMessageDialog(jTree1,"El archivo "+archivo.getName()+" ha sido creado.","Aviso",JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }
}
