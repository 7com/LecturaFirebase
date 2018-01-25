
import GUI.Pantalla;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class main {
    private static String BD_URL, CREDENCIALES;
    public static void main(String args[]) {
        try {
            carcarCFG();
            iniciarFirebase();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        Pantalla p = new Pantalla();
        p.setVisible(true);
    }
    
    public static void carcarCFG() throws FileNotFoundException{
        File archivo = new File("config.cfg");
        Scanner scan = new Scanner(archivo);
        BD_URL=scan.nextLine();
        CREDENCIALES=scan.nextLine();
        if (BD_URL == null || CREDENCIALES == null)
            throw new FileNotFoundException("Archivo config.cfg se encuentra incompleto./n"
                    + "Revisar archivo config.cfg");
    }
    
    public static void iniciarFirebase() throws FileNotFoundException{
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setServiceAccount(new FileInputStream(CREDENCIALES))
                    .setDatabaseUrl(BD_URL)
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (FileNotFoundException ex) {
            throw new FileNotFoundException("El archivo "+CREDENCIALES+" no existe.\n"
                        + "Revisar archivo config.cfg");
        } 
    }
}
