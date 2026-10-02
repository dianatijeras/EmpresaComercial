module co.edu.uniquindio.empresacomercial.empresacomercial {
    requires javafx.controls;
    requires javafx.fxml;


    opens co.edu.uniquindio.empresacomercial.empresacomercial to javafx.fxml;
    exports co.edu.uniquindio.empresacomercial.empresacomercial;
}