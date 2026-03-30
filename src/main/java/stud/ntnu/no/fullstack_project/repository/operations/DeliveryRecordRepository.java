package stud.ntnu.no.fullstack_project.repository.operations;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.operations.DeliveryRecord;

public interface DeliveryRecordRepository extends JpaRepository<DeliveryRecord, Long> {

  List<DeliveryRecord> findByOrganizationIdOrderByDeliveryDateDesc(Long organizationId);

  List<DeliveryRecord> findBySupplierIdOrderByDeliveryDateDesc(Long supplierId);
}
