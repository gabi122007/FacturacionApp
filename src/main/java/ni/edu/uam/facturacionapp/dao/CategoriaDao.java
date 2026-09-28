package ni.edu.uam.facturacionapp.dao;

import ni.edu.uam.facturacionapp.database.ConexionDB;
import ni.edu.uam.facturacionapp.model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDao {

    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categoria WHERE activa = true ORDER BY id";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (conn == null) {
                throw new SQLException("No se pudo conectar a la base de datos.");
            }

            while (rs.next()) {
                lista.add(mapResultSetToCategoria(rs));
            }
        }
        return lista;
    }

    public boolean insertar(Categoria c) throws SQLException {
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?, ?)";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                throw new SQLException("No se pudo conectar a la base de datos.");
            }

            stmt.setString(1, c.getNombre());
            stmt.setBoolean(2, c.isActiva());

            return stmt.executeUpdate() > 0;
        }
    }


    private Categoria mapResultSetToCategoria(ResultSet rs) throws SQLException {
        return new Categoria(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getBoolean("activa")
        );
    }
}