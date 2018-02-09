
import GUI.PantallaDescarga;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class main {
    private static String BD_URL, CREDENCIALES;
    public static void main(String args[]) {
        try {
            carcarCFG();
            iniciarFirebase();
            PantallaDescarga p = new PantallaDescarga(obtenerToken());
            p.setVisible(true);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    public static String obtenerToken() throws IOException{
        // Load the service account key JSON file
        FileInputStream serviceAccount = new FileInputStream(CREDENCIALES);

        // Authenticate a Google credential with the service account
        GoogleCredential googleCred = GoogleCredential.fromStream(serviceAccount);

        // Add the required scopes to the Google credential
        GoogleCredential scoped = googleCred.createScoped(
            Arrays.asList(
              "https://www.googleapis.com/auth/firebase.database",
              "https://www.googleapis.com/auth/userinfo.email"
            )
        );

        // Use the Google credential to generate an access token
        scoped.refreshToken();
        String token = scoped.getAccessToken();
        return token;
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
