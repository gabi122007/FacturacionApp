package ni.edu.uam.facturacionapp.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Categoria {
    private Integer id;
    private String nombre;
    private boolean Activa;

    @Override
    public String toString() {
        return nombre;
    }
}
