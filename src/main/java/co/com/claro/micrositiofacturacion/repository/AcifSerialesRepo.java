package co.com.claro.micrositiofacturacion.repository;

import co.com.claro.micrositiofacturacion.entity.AcifSerialesEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcifSerialesRepo extends JpaRepository<AcifSerialesEntity, Long> {
    List<AcifSerialesEntity> findByIdCargueFk(Long idCargueFk);
    Page<AcifSerialesEntity> findByIdCargueFk(Long idCargueFk, Pageable pageable);
    boolean existsByIdCargueFk(Long idCargueFk);
}
