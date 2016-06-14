package simplecipheraes;

import simplecipheraes.SimpleCipherAES.CipherType;
import java.io.File;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author dPow
 */
public class SimpleCipherDriverGUI extends Application {
    Text progress;

    @Override
    public void start(Stage stage) throws Exception {
        SimpleCipherAES cipher = new SimpleCipherAES();
        
        //Password field
        TextField textField = new TextField();
        
        //Add buttons and their functions
        Button encrypt = new Button("Encrypt file");
        encrypt.setOnAction((ActionEvent event) -> {
            byte[] key = cipher.hashKey(textField.getText());
            if (key == null) { //Prevent encrypting with blank key
                //Do nothing
            } else {
                File file = openFileExplorer("Encrypt");
                if (file == null) { //If something went wrong with fileChooser
                    //Do nothing
                } else {
                    cipher.processFile(file, key, CipherType.ENCRYPT, this);
                }
            }
        });
        
        Button decrypt = new Button("Decrypt file");
        decrypt.setOnAction((ActionEvent event) -> {
            byte[] key = cipher.hashKey(textField.getText());
            if (key == null) {
                //Do nothing
            } else {
                File file = openFileExplorer("Decrypt");
                if (file == null) {
                    //Do nothing
                } else {
                    cipher.processFile(file, key, CipherType.DECRYPT, this);
                }
            }
        });
        
        progress = new Text("Type in a password to encrypt or decrypt");
        Font font = new Font("vernanda", 16);
        progress.setFont(font);
        progress.setFill(Color.BLACK);
        progress.setTextAlignment(TextAlignment.CENTER);
        
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(50, 10, 0, 10));
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(textField, encrypt, decrypt, progress);
        
        Scene scene = new Scene(vbox, Paint.valueOf("Blue"));
        
        stage.setScene(scene);
        stage.setTitle("Encrypt and Decrypt your Files");
        stage.setWidth(400);
        stage.setHeight(400);
        stage.setResizable(false);
        //stage.getIcons().add(new Image("/characterimages/DragonIcon.png"));
        
        stage.show();
    }
    
    @Override
    public void stop() {
        System.exit(0);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Opens a JFileChooser with the supplied text on the action button.
     * 
     * @param buttonText 
     *          Text to show on the action button
     * @return 
     *          File selected that will be encrypted or decrypted
     */
    public File openFileExplorer(String buttonText) {
        JFileChooser fileChooser = new JFileChooser("."){
            
            @Override
            public void approveSelection() {
                File fileChoice = this.getSelectedFile();
                if (fileChoice.isDirectory()) {
                    this.setCurrentDirectory(fileChoice);
                }
                else {
                    super.approveSelection();
                }
            }
        };
        
        //First, change to a nice LookAndFeel
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | 
                IllegalAccessException | UnsupportedLookAndFeelException ex) {
            System.err.println(ex.getClass());
        }
        
        //Update the JFileChooser's look and feel
        fileChooser.updateUI();
        
        //Allow the JFileChooser to see files and directories instead of only files
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        //Encrypt or decryptFile the selected file
        int fileChoice = fileChooser.showDialog(null, buttonText);
        if (fileChoice == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.isFile()) {
                return selectedFile;
            }
        }
        
        return null;
    }
    
    /**
     * Updates the progress of the encryption/decryption, and displays
     * it on the screen.
     * 
     * @param completed 
     *          Number of bytes processed in the encryption/decryption
     *          methods
     * @param fileSize
     *          Size of the file, in bytes
     */
    public void updateProgress(long completed, long fileSize) {
        int orderOfMagnitude = 0;
        String suffix;
        if (fileSize > 1000000000) {
            orderOfMagnitude = 1000000000; //Gigabyte
            suffix = "GB";
        } else if (fileSize > 1000000) {
            orderOfMagnitude = 1000000; //Megabyte
            suffix = "MB";
        } else if (fileSize > 1000) {
            orderOfMagnitude = 1000; //Kilobyte
            suffix = "KB";
        } else {
            orderOfMagnitude = 1; //Byte
            suffix = "B";
        }
        
        progress.setText("Running...\n" + completed/orderOfMagnitude + "/" +
                fileSize/orderOfMagnitude + " " + suffix + " completed");
    }
    
    /**
     * Overloaded message to display text instead of a fraction.
     * 
     * @param message 
     *          Text to display
     */
    public void updateProgress(String message) {
        progress.setText(message);
    }
    
}