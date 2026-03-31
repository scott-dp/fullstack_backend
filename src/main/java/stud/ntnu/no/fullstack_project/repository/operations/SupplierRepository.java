package stud.ntnu.no.fullstack_project.repository.operations;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.operations.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

  List<Supplier> findByOrganizationIdOrderByNameAsc(Long organizationId);

  List<Supplier> findByOrganizationIdAndActiveTrue(Long organizationId);
}
