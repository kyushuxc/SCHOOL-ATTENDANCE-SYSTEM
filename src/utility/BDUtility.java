package utility;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
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
    
    /**
     * Scale an image to fit within specified dimensions while maintaining aspect ratio
     * @param originalImage The image to scale
     * @param maxWidth Maximum width
     * @param maxHeight Maximum height
     * @return Scaled BufferedImage
     */
   public static BufferedImage scaleImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
    if (originalImage == null) {
        return null;
    }
    
    int originalWidth = originalImage.getWidth();
    int originalHeight = originalImage.getHeight();
    
    double scaleX = (double) maxWidth / originalWidth;
    double scaleY = (double) maxHeight / originalHeight;
    double scale = Math.min(scaleX, scaleY);
    
    int scaledWidth = (int) (originalWidth * scale);
    int scaledHeight = (int) (originalHeight * scale);
    
    BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, originalImage.getType());
    scaledImage.createGraphics().drawImage(
        originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH),
        0, 0, null
    );
    
    return scaledImage;
}
    
    /**
     * Scale an image to exact dimensions (may distort aspect ratio)
     * @param originalImage The image to scale
     * @param width Target width
     * @param height Target height
     * @return Scaled BufferedImage
     */
    public static BufferedImage scaleImageExact(BufferedImage originalImage, int width, int height) {
        if (originalImage == null) {
            return null;
        }
        
        BufferedImage scaledImage = new BufferedImage(width, height, originalImage.getType());
        scaledImage.createGraphics().drawImage(
            originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH),
            0, 0, null
        );
        
        return scaledImage;
    }
    
    /**
     * Load image from file with error handling
     * @param file The file to load
     * @return BufferedImage or null if error
     */
    public static BufferedImage loadImage(File file) {
        try {
            return ImageIO.read(file);
        } catch (Exception ex) {
            System.err.println("Error loading image: " + file.getAbsolutePath());
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * Load image from resource path with error handling
     * @param resourcePath Path to resource (e.g., "/images/photo.jpg")
     * @return BufferedImage or null if error
     */
    public static BufferedImage loadImageResource(String resourcePath) {
        try {
            URL imgURL = BDUtility.class.getResource(resourcePath);
            if (imgURL == null) {
                System.err.println("Image resource not found: " + resourcePath);
                return null;
            }
            return ImageIO.read(imgURL);
        } catch (Exception ex) {
            System.err.println("Error loading image resource: " + resourcePath);
            ex.printStackTrace();
            return null;
        }
    }
}