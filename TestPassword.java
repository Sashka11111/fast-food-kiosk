import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TestPassword {
    public static void main(String[] args) {
        String[] testPasswords = {"password", "123456", "admin", "user", "test", "sashka", "Sashka", "12345", "qwerty"};
        String targetHash = "32c6b1625a1aae8ba1cbdb24c20b6c24ed42ab7389c54c6a5a53d59fff0f2b59";
        
        for (String password : testPasswords) {
            String hash = hashPassword(password);
            System.out.println("Password: " + password + " -> Hash: " + hash);
            if (hash.equals(targetHash)) {
                System.out.println("MATCH FOUND! Password for Sashka is: " + password);
            }
        }
    }
    
    public static String hashPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = messageDigest.digest(password.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
