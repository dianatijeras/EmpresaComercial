module co.edu.uniquindio.empresacomercial.empresacomercial {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;


    opens co.edu.uniquindio.empresacomercial.empresacomercial to javafx.fxml;
    exports co.edu.uniquindio.empresacomercial.empresacomercial;
}