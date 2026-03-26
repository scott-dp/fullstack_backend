package stud.ntnu.no.fullstack_project.entity;

/**
 * Norwegian alcohol group classification for bevilling permits.
 *
 * <ul>
 *   <li>{@code GROUP_1} - Beverages up to 4.7% ABV (beer, cider)</li>
 *   <li>{@code GROUP_2} - Beverages from 4.7% to 22% ABV (wine)</li>
 *   <li>{@code GROUP_3} - Beverages above 22% ABV (spirits)</li>
 * </ul>
 */
public enum AlcoholGroup {
  GROUP_1,
  GROUP_2,
  GROUP_3
}
