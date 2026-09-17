package com.example.demo.mapper;

import com.example.demo.dto.FavoritoRequestDTO;
import com.example.demo.dto.FavoritoResponseDTO;
import com.example.demo.model.Favorito;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Componente encargado de mapear entre la entidad Favorito y sus correspondientes DTOs.
 */
@Component
public class FavoritoMapper {

    /**
     * Convierte los datos que envía el cliente (Request) a la entidad interna.
     */
    public Favorito toEntity(FavoritoRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }
        
        Favorito favorito = new Favorito();
        favorito.setProductoId(requestDTO.productoId());
        favorito.setNotaPersonal(requestDTO.notaPersonal());
        // El id se genera en el Repository y la fecha se setea en el Service o acá al momento de la creación
        favorito.setFechaCreacion(LocalDateTime.now());
        
        return favorito;
    }

    /**
     * Convierte la entidad interna a la respuesta segura para el cliente.
     */
    public FavoritoResponseDTO toDTO(Favorito favorito) {
        if (favorito == null) {
            return null;
        }
        
        return new FavoritoResponseDTO(
                favorito.getId(),
                favorito.getProductoId(),
                favorito.getNotaPersonal(),
                favorito.getFechaCreacion()
        );
    }
}
