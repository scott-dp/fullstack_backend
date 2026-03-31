package stud.ntnu.no.fullstack_project.entity.operations;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single item within a checklist template that must be checked off during completion.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "checklist_items")
public class ChecklistItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id", nullable = false)
  private ChecklistTemplate template;

  @Column(nullable = false)
  private String description;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Column(name = "requires_comment", nullable = false)
  private boolean requiresComment;
}
