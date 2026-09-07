package ni.edu.uam.facturacionapp.model;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@Data
public class Producto {
    private Integer id;
    private String codigo;
    private String nombre;
    private String categoria;
    private BigDecimal precio;
    private int exsistencia;
    private String rutaimagen;
    private boolean activo;
}
