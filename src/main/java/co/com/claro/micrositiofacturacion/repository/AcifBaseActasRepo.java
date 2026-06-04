package co.com.claro.micrositiofacturacion.repository;

import co.com.claro.micrositiofacturacion.entity.AcifBaseActasEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcifBaseActasRepo extends JpaRepository<AcifBaseActasEntity, Long> {
    List<AcifBaseActasEntity> findByIdCargueFk(Long idCargueFk);
    Page<AcifBaseActasEntity> findByIdCargueFk(Long idCargueFk, Pageable pageable);
    boolean existsByIdCargueFk(Long idCargueFk);
}
