package ni.edu.uam.facturacionapp.model;

import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Empleado {
    private Integer id;
    private String nombres;
    private String apellidos;
    private Cargo cargo;
    private LocalDate fechaNac;
    private boolean activo;
}
