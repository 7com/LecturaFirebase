package GUI;

import Motor.FirebaseExport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.commons.io.IOUtils;

public class PantallaDescarga extends javax.swing.JFrame implements Runnable{
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("SAPBot");
    private final DefaultTreeModel SAPBot = new DefaultTreeModel(root);
    private final ExecutorService exec;
    private final String token;
    private final String firebaseURL;
    private PantallaInicio inicio;
    /** Creates new form Pantalla */
    public PantallaDescarga(String t,String url, PantallaInicio i) {
        token=t;
        firebaseURL=url;
        initComponents();
        this.exec = Executors.newFixedThreadPool(5);
        inicio=i;
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                inicio.setButton3(true);
            }
        });
    }

    //Función dedicada a crear el menú para descargar las pruebas desde Firebase
    private void leerJSON(String url) throws MalformedURLException, IOException{
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
        
	ArrayList<DefaultMutableTreeNode> n1 = new ArrayList<DefaultMutableTreeNode>();
	String json = IOUtils.toString(new URL(url));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
        Iterator fieldNames = node.fieldNames();
        int tot = node.size();
        int cont=0;
        int por;
        while (fieldNames.hasNext()) {
            cont++;
            por=(cont*50)/tot;
            pb.setValue(por);
            String fieldName = fieldNames.next().toString();
            if (!fieldName.equalsIgnoreCase("*Sin Procesar") && !fieldName.equalsIgnoreCase("*Config"))
                n1.add(new DefaultMutableTreeNode(fieldName));
        }
        tot = n1.size();
        cont=0;
        for (int i=0; i<n1.size(); i++){
            cont++;
            por=50+((cont*50)/tot);
            pb.setValue(por);
            json = IOUtils.toString(new URL(firebaseURL
                    +n1.get(i).getUserObject().toString()+".json?shallow=true&access_token="+token));
            mapper = new ObjectMapper();
            node = mapper.readTree(json);
            fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next().toString();
                n1.get(i).add(new DefaultMutableTreeNode(fieldName));
            }
            SAPBot.insertNodeInto(n1.get(i), root, root.getChildCount());
        }      
        dialog.dispose();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
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

    //Evento que crea menú desplegable al presionar el botón derecho del mouse.
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
                    exec.execute(new FirebaseExport(jTree1,0,selPath,token,firebaseURL));
                  }
                });
                JMenuItem item2 = new JMenuItem("Descargar pruebas formato JSON");
                item2.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                     exec.execute(new FirebaseExport(jTree1,1,selPath,token,firebaseURL));
                    }
                });
                JPopupMenu menu = new JPopupMenu("Popup");
                menu.add(item);
                menu.add(item2);
                menu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }//GEN-LAST:event_jTree1MousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void run() {
        try {
            leerJSON(firebaseURL+".json?shallow=true&access_token="+token);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.exit(1);
        }
        jTree1.expandRow(0);
        jTree1.setRootVisible(false);
        jTree1.setShowsRootHandles(true);
        setVisible(true);
    }

    
}


