package core;

public class App {
    
    private void nonStaticMain(String args[]){
        Main main = new Main();
        MainWindow window = new MainWindow();
        MainPresenter presenter = new MainPresenter(window, main);
        presenter.start();
    }
    
    public static void main(String args[]){
        App app = new App();
        app.nonStaticMain(args);
    }
}
