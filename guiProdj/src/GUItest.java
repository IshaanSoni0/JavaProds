import javax.swing.JOptionPane;
public class GUItest {
    public static void main(String[] args) throws Exception {
        String name = JOptionPane.showInputDialog("Enter your name:");
    }
}
//  use: javac -d bin src/*.java -------- to compile to bin
//  use: java -cp bin App -------- to run the App.class in bin