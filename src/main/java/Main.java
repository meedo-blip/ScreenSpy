import jade.Window;
import notspy.SpyScene;

// ... inside the try block
public class Main {
    public static void main(String[] args) {
        Window.get("Screen Spy").run(new SpyScene());
    }
}