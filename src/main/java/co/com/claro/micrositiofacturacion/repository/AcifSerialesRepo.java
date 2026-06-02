package co.com.claro.micrositiofacturacion.repository;

import co.com.claro.micrositiofacturacion.entity.AcifSerialesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcifSerialesRepo extends JpaRepository<AcifSerialesEntity, Long> {
    List<AcifSerialesEntity> findByIdCargueFk(Long idCargueFk);
    boolean existsByIdCargueFk(Long idCargueFk);
}
