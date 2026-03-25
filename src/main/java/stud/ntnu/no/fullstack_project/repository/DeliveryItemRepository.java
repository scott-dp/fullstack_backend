package stud.ntnu.no.fullstack_project.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.DeliveryItem;

public interface DeliveryItemRepository extends JpaRepository<DeliveryItem, Long> {

  List<DeliveryItem> findByDeliveryRecordId(Long deliveryRecordId);

  List<DeliveryItem> findByDeliveryRecordOrganizationIdAndProductNameContainingIgnoreCaseOrderByDeliveryRecordDeliveryDateDesc(
      Long orgId, String productName);

  List<DeliveryItem> findByDeliveryRecordOrganizationIdAndBatchLotContainingIgnoreCaseOrderByDeliveryRecordDeliveryDateDesc(
      Long orgId, String batchLot);
}
