/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Firebase;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    TreePath selPath;
    
    @Override
    public void run() {
        if (op==0)
        {
            String s=selPath.toString();
            s=(String)s.subSequence(0, s.length()-1);
            String[] lista = s.split(",");
            s=lista[1].substring(1)+"/"+lista[2].substring(1);
            String temp=s;
            s="https://sapbot-001.firebaseio.com/"+s+"/";
            try {
                crearXLS(s,temp);
            } catch (IOException ex) {
                Logger.getLogger(Firebase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            descargarJSON();
        }
    }
    
    public Firebase(JTree j,int i, TreePath t)
    {
        jTree1=j;
        op=i;
        selPath=t;
    }
    
    private void crearXLS(String url, String n) throws MalformedURLException, IOException{
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
        
        
        
        String temp="",volt="",frec="",enco="";
        ArrayList<String> motor = new ArrayList<String>();
        motor.add("Motor 1");
        motor.add("Motor 2");
        motor.add("Motor 3");
        motor.add("Motor 4");
        motor.add("Motor 5");
        motor.add("Motor 6");
        motor.add("Motor 7");
        HSSFWorkbook excel = new HSSFWorkbook();
        File f = new File("temp.json");
        for(int i=0;i<7;i++)
        {   
            pb.setString("Procesando Motor "+(i+1));
            String s = url+motor.get(i)+"/Frecuencia/.json";
            FileUtils.copyURLToFile(new URL(s), f);
            Scanner scan = new Scanner(f);
            frec=scan.nextLine();
            
            s = url+motor.get(i)+"/Posicion/.json";
            FileUtils.copyURLToFile(new URL(s), f);
            scan = new Scanner(f);
            enco=scan.nextLine();
            
            s = url+motor.get(i)+"/Temperatura/.json";
            FileUtils.copyURLToFile(new URL(s), f);
            scan = new Scanner(f);
            temp=scan.nextLine();
            
            s = url+motor.get(i)+"/Voltaje/.json";
            FileUtils.copyURLToFile(new URL(s), f);
            scan = new Scanner(f);
            volt=scan.nextLine();
            
            if (!volt.equalsIgnoreCase("null"))
            {
                temp=temp.substring(1, temp.length()-1);
                volt=volt.substring(1, volt.length()-1);
                frec=frec.substring(1, frec.length()-1);
                enco=enco.substring(1, enco.length()-1);
                temp=temp.replaceAll("\\s","");
                volt=volt.replaceAll("\\s","");
                frec=frec.replaceAll("\\s","");
                enco=enco.replaceAll("\\s","");
                String[] te,vo,fr,en;
                te=temp.split(",");
                vo=volt.split(",");
                fr=frec.split(",");
                en=enco.split(",");
                Sheet hoja = excel.createSheet(motor.get(i));
                Row fila = hoja.createRow((short)0);
                fila.createCell(0).setCellValue("Frecuencia");
                fila.createCell(1).setCellValue("Posicion");
                fila.createCell(2).setCellValue("Temperatura");
                fila.createCell(3).setCellValue("Voltaje");
                
                for (int j=1;j<=en.length;j++)
                {
                    fila = hoja.createRow((short)j);
                    fila.createCell(0).setCellValue(parseFloat(fr[j-1]));
                    fila.createCell(1).setCellValue(parseInt(en[j-1]));
                    fila.createCell(2).setCellValue(parseFloat(te[j-1]));
                    fila.createCell(3).setCellValue(parseFloat(vo[j-1]));
                }
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
        n=n.replaceAll("/", " - ");
        n=n.replaceAll(":", "_");
        chooser.setSelectedFile(new File(n));
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
                    try (FileOutputStream fileOut = new FileOutputStream(archivo)) {
                    excel.write(fileOut);
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
                }
                if (archivo.exists()){
                    JOptionPane.showMessageDialog(jTree1,"El archivo "+archivo.getName()+" ha sido creado.","Aviso",JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
        dialog.dispose();
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
        s="https://sapbot-001.firebaseio.com/"+s+".json?print=pretty";
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
