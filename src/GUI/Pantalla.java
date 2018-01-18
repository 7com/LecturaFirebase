/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package GUI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class Pantalla extends javax.swing.JFrame {
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode("SAPBot");
    private DefaultTreeModel SAPBot = new DefaultTreeModel(root);
    /** Creates new form Pantalla */
    public Pantalla() {
        initComponents();
        try {
            leerJSON("https://sapbot-001.firebaseio.com/.json?shallow=true");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        jTree1.expandRow(0);
        jTree1.setRootVisible(false);
        jTree1.setShowsRootHandles(true);
    }

    private void crearXLS(String url, String n) throws MalformedURLException, IOException{
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
        chooser.setSelectedFile(new File(n.replaceAll("/", " - ")));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            File archivo = chooser.getSelectedFile();
            if (!archivo.getAbsolutePath().endsWith(".xls")){
                archivo = new File(chooser.getSelectedFile() + ".xls");
            }
            try (FileOutputStream fileOut = new FileOutputStream(archivo)) {
                excel.write(fileOut);
            }
            if (archivo.exists()){
                JOptionPane.showMessageDialog(this,"El archivo "+archivo.getName()+" ha sido creado.","Aviso",JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void leerJSON(String url) throws MalformedURLException, IOException{
	ArrayList<DefaultMutableTreeNode> n1 = new ArrayList<DefaultMutableTreeNode>();
	String json = IOUtils.toString(new URL(url));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
        Iterator fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next().toString();
            n1.add(new DefaultMutableTreeNode(fieldName));
        }
        for (int i=0; i<n1.size(); i++){
            json = IOUtils.toString(new URL("https://sapbot-001.firebaseio.com/"
                    +n1.get(i).getUserObject().toString()+".json?shallow=true"));
            mapper = new ObjectMapper();
            node = mapper.readTree(json);
            fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next().toString();
                n1.get(i).add(new DefaultMutableTreeNode(fieldName));
            }
            SAPBot.insertNodeInto(n1.get(i), root, root.getChildCount());
        }      
    }
       
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sistema de Descarga SAPBot");
        setBackground(new java.awt.Color(255, 255, 255));

        jTree1.setModel(SAPBot);
        jTree1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTree1MousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTree1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jTree1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTree1MousePressed
        if(SwingUtilities.isRightMouseButton(evt)){
            boolean select=false;
            int selRow = jTree1.getRowForLocation(evt.getX(), evt.getY());
            TreePath selPath = jTree1.getPathForLocation(evt.getX(), evt.getY());
            jTree1.setSelectionPath(selPath); 
            if (selRow>-1){
                jTree1.setSelectionRow(selRow); 
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode)jTree1.getLastSelectedPathComponent();
                if(nodo.isLeaf())
                    select=true;
            }
            if (select)
            {
                JMenuItem item = new JMenuItem("Crear Excel");
                item.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                    String s=selPath.toString();
                    s=(String)s.subSequence(0, s.length()-1);
                    String[] lista = s.split(",");
                    s=lista[1].substring(1)+"/"+lista[2].substring(1);
                    String temp=s;
                    s="https://sapbot-001.firebaseio.com/"+s+"/";
                      try {
                          crearXLS(s,temp);
                      } catch (IOException ex) {
                          System.out.println(ex.getMessage());
                      }
                  }
                });
                JMenuItem item2 = new JMenuItem("Descargar JSON");
                item2.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
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
                    chooser.setSelectedFile(new File(temp.replaceAll("/", " - ")));
                    if (chooser.showSaveDialog(jTree1) == JFileChooser.APPROVE_OPTION)
                    {
                        
                        File archivo = chooser.getSelectedFile();
                        if (!archivo.getAbsolutePath().endsWith(".json")){
                            archivo = new File(chooser.getSelectedFile() + ".json");
                        }
                        try {
                            FileUtils.copyURLToFile(new URL(s), archivo);
                        } catch (MalformedURLException ex) {
                            System.out.println(ex.getMessage());
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }
                        if (archivo.exists()){
                            JOptionPane.showMessageDialog(jTree1,"El archivo "+archivo.getName()+" se ha descargado.","Aviso",JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                  }
                });
                JPopupMenu menu = new JPopupMenu("Popup");
                menu.add(item);
                menu.add(item2);
                menu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
        File f = new File("temp.json");
        f.delete();
    }//GEN-LAST:event_jTree1MousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

    
}


