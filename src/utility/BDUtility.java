package utility;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class BDUtility {

    // ✅ Load and set image from resources (works in JAR)
    public static void setImage(JFrame frame, String resourcePath, int newWidth, int newHeight) {
        try {
            // Load image from classpath
            URL imgURL = BDUtility.class.getResource(resourcePath);
            if (imgURL == null) {
                System.out.println("Image not found: " + resourcePath);
                return;
            }

            BufferedImage originalImage = ImageIO.read(imgURL);
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            resizedImage.createGraphics().drawImage(
                originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH),
                0, 0, null
            );

            ImageIcon backgroundImage = new ImageIcon(resizedImage);
            JLabel backgroundLabel = new JLabel(backgroundImage);
            backgroundLabel.setBounds(0, 0, newWidth, newHeight);
            frame.getContentPane().add(backgroundLabel, BorderLayout.CENTER);
            frame.validate();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ✅ Helper for dynamic student image names
    public static String getResourcePath(String fileName) {
        return "/images/" + fileName; // e.g. "/images/Kerby Sexon.jpg"
    }

    private static HashMap<String, JFrame> formsMap = new HashMap<>();

    public static void openForm(String formName, JFrame formInstance) {
        JFrame existingForm = formsMap.get(formName);

        if (existingForm == null || !existingForm.isVisible()) {
            formsMap.put(formName, formInstance);
            formInstance.setVisible(true);
        } else {
            existingForm.toFront();
        }
    }

    public static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    public static BufferedImage scaleImage(BufferedImage originalImage, BufferedImage selectedImage) {
        int width = selectedImage.getWidth();
        int height = selectedImage.getHeight();
        BufferedImage scaledImage = new BufferedImage(width, height, originalImage.getType());
        scaledImage.createGraphics().drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        return scaledImage;
    }
}
