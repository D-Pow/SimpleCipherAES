package simplecipheraes;

import java.io.File;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * GUI Driver for the simple AES cipher.
 * Includes a main GUI for typing in a password and selecting
 * options to encrypt or decrypt files. A JFileChooser is used
 * to select the user's file. A message is set below the buttons
 * showing the progress of the user's command.
 *
 * @author dPow
 */
public class SimpleCipherGUI extends Application {
    AppProgressText progressText = new AppProgressText();
    String filePath;

    public SimpleCipherGUI() {
        //set initial file-open dialog to current directory
        this.filePath = ".";
    }

    @Override
    public void start(Stage stage) throws Exception {
        SimpleCipherAES cipher = new SimpleCipherAES(this.progressText);

        //Password field
        PasswordField textField = new PasswordField();

        //Add buttons and their functions
        Button encrypt = new Button("Encrypt file");
        encrypt.setOnAction((ActionEvent event) -> {
            byte[] key = cipher.hashKey(textField.getText());
            if (key == null) { //Prevent encrypting with blank key
                this.progressText.updateProgress("Please type in a password.");
            } else {
                File file = openFileExplorer("Encrypt");
                if (file == null) {
                    this.progressText.updateProgress("Something went wrong with selecting your file.");
                } else {
                    //Remove password from text field to prevent people from peeking
                    //over your shoulder
                    textField.setText("");
                    //Let user know the file is being loaded
                    this.progressText.updateProgress("Loading...");
                    //Process file
                    cipher.processFile(file, key, EncryptionDirection.ENCRYPT);
                }
            }
        });

        Button decrypt = new Button("Decrypt file");
        decrypt.setOnAction((ActionEvent event) -> {
            byte[] key = cipher.hashKey(textField.getText());
            if (key == null) {
                this.progressText.updateProgress("Please type in a password.");
            } else {
                File file = openFileExplorer("Decrypt");
                if (file == null) {
                    this.progressText.updateProgress("Something went wrong with selecting your file.");
                } else {
                    //Remove password from text field to prevent people from peeking
                    //over your shoulder
                    textField.setText("");
                    //Let user know the file is being loaded
                    this.progressText.updateProgress("Loading...");
                    //Process file
                    cipher.processFile(file, key, EncryptionDirection.DECRYPT);
                }
            }
        });

        addEnterListener(encrypt);
        addEnterListener(decrypt);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(50, 10, 0, 10));
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(textField, encrypt, decrypt, progressText.getSceneElement());

        Scene scene = new Scene(vbox, Paint.valueOf("Blue"));

        stage.setScene(scene);
        stage.setTitle("Encrypt and Decrypt your Files");
        stage.setWidth(400);
        stage.setHeight(400);
        stage.setResizable(false);

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
     * Adds a keyListener so that when Enter is pressed, it fires the button.
     *
     * @param button
     *          Button to which a keyListener is added.
     */
    private void addEnterListener(Button button) {
        button.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (e.getCode() == KeyCode.ENTER) {
                    button.fire();
                    e.consume(); //Prevents further events from happening
                }
            }
        });
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
        JFileChooser fileChooser = new JFileChooser(this.filePath){

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

        //Set the decryption to only show ".enc" files
        if (buttonText.equals("Decrypt")) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Encrypted files (.enc)", "enc");
            fileChooser.setFileFilter(filter);
        }

        //Encrypt or decryptFile the selected file
        int fileChoice = fileChooser.showDialog(null, buttonText);
        if (fileChoice == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.isFile()) {
                this.filePath = selectedFile.getAbsolutePath();
                return selectedFile;
            }
        }

        return null;
    }

}
