package ni.edu.uam.facturacionapp.dao;

import ni.edu.uam.facturacionapp.model.Categoria;
import ni.edu.uam.facturacionapp.database.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDao {

    public boolean insertar(Categoria c) throws SQLException {
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?, ?)";

        Connection conn = ConexionDB.conectar();

        if (conn == null) {
            throw new SQLException("No se pudo establecer la conexión a la base de datos.");
        }

        try (conn;
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, c.getNombre());
            stmt.setBoolean(2, c.isActiva());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        c.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean actualizar(Categoria c) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ?, activa = ? WHERE id = ?";

        Connection conn = ConexionDB.conectar();

        if (conn == null) {
            throw new SQLException("No se pudo establecer la conexión a la base de datos.");
        }

        try (conn;
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getNombre());
            stmt.setBoolean(2, c.isActiva());
            stmt.setInt(3, c.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";

        Connection conn = ConexionDB.conectar();

        if (conn == null) {
            throw new SQLException("No se pudo establecer la conexión a la base de datos.");
        }

        try (conn;
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;
        }
    }

    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, activa FROM categoria";

        Connection conn = ConexionDB.conectar();

        if (conn == null) {
            throw new SQLException("No se pudo establecer la conexión a la base de datos.");
        }

        try (conn;
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Categoria c = new Categoria(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getBoolean("activa")
                );
                lista.add(c);
            }
        }

        return lista;
    }

    public boolean existeNombre(String nombre, Integer idExcluir) throws SQLException {
        boolean tieneId = (idExcluir != null && idExcluir > 0);
        String sql = tieneId
                ? "SELECT COUNT(*) FROM categoria WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?)) AND id != ?"
                : "SELECT COUNT(*) FROM categoria WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?))";

        Connection conn = ConexionDB.conectar();

        if (conn == null) {
            throw new SQLException("No se pudo establecer la conexión a la base de datos.");
        }

        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.setString(1, nombre);

            if (tieneId) {
                ps.setInt(2, idExcluir);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

}