package stud.ntnu.no.fullstack_project.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stud.ntnu.no.fullstack_project.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

  List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

  long countByUserIdAndReadFalse(Long userId);

  @Modifying
  @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
  void markAllAsRead(@Param("userId") Long userId);

  void deleteByUserId(Long userId);

  @Modifying
  @Query("DELETE FROM Notification n WHERE n.referenceType = :referenceType AND n.referenceId IN :referenceIds")
  void deleteByReferenceTypeAndReferenceIdIn(@Param("referenceType") String referenceType,
      @Param("referenceIds") Collection<Long> referenceIds);
}
