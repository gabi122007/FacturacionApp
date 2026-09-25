package ni.edu.uam.facturacionapp.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

        private static final String URL =
                "jdbc:postgresql://localhost:5432/facturacionapp";

        private static final String USUARIO = "postgres";

        private static final String PASSWORD = "12345";

        public static Connection conectar() {
            Connection conexion = null;

            try {
                conexion = DriverManager.getConnection(
                        URL,
                        USUARIO,
                        PASSWORD
                );

                System.out.println("Conexión exitosa a PostgreSQL");

            } catch (SQLException e) {
                System.out.println("Error al conectar con PostgreSQL:");
                System.out.println(e.getMessage());
            }

            return conexion;
        }
    }
