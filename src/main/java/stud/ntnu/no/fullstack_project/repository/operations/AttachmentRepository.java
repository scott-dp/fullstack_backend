package stud.ntnu.no.fullstack_project.repository.operations;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.no.fullstack_project.entity.operations.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

  List<Attachment> findByOrganizationId(Long organizationId);
}
