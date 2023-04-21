package simplecipheraes;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class AppProgressText {
    Text progress = new Text();
    Font font = new Font("vernanda", 16);

    public AppProgressText() {
        this("Type in a password to encrypt or decrypt");
    }
    public AppProgressText(String starterText) {
        this.progress.setText(starterText);
        this.progress.setFont(font);
        this.progress.setFill(Color.BLACK);
        this.progress.setTextAlignment(TextAlignment.CENTER);
    }

    /**
     * Updates the progress of the encryption/decryption, and displays
     * it on the screen.
     *
     * @param completedSize
     *          Number of bytes processed in the encryption/decryption
     *          methods
     * @param fileSize
     *          Size of the file, in bytes
     */
    public void updateProgress(double completedSize, double fileSize) {
        double orderOfMagnitude;
        String suffix;
        if (fileSize > 1000000000) {
            orderOfMagnitude = 1000000000.0; //Gigabyte
            suffix = "GB";
        } else if (fileSize > 1000000) {
            orderOfMagnitude = 1000000.0; //Megabyte
            suffix = "MB";
        } else if (fileSize > 1000) {
            orderOfMagnitude = 1000.0; //Kilobyte
            suffix = "KB";
        } else {
            orderOfMagnitude = 1.0; //Byte
            suffix = "B";
        }

        //Round numbers to the nearest hundredth
        double completedSizeDisplayed = ((double)Math.round(completedSize/orderOfMagnitude*100))/100;
        double fileSizeDisplayed = ((double)Math.round(fileSize/orderOfMagnitude*100))/100;

        this.updateProgress("Running...\n" + completedSizeDisplayed + "/" +
                                     fileSizeDisplayed + " " + suffix + " completed");
    }

    /**
     * Overloaded message to display text instead of a fraction.
     *
     * @param message
     *          Text to display
     */
    public void updateProgress(String message) {
        this.progress.setText(message);
    }

    public Text getSceneElement() {
        return this.progress;
    }
}
