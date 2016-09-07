package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * RequisitionGroup represents a group of facilities which follow a particular schedule for a
 * program. It also defines the contract for creation/upload of RequisitionGroup.
 */
@Entity
@Table(name = "requisition_groups", schema = "referencedata")
@NoArgsConstructor
public class RequisitionGroup extends BaseEntity {

  @Column(unique = true, nullable = false, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(nullable = false, columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String description;

  @OneToOne
  @JoinColumn(name = "supervisoryNodeId", nullable = false)
  @Getter
  @Setter
  private SupervisoryNode supervisoryNode;

  @OneToMany
  @JoinColumn(name = "requisitionGroupProgramSchedulesId")
  @Getter
  @Setter
  private List<RequisitionGroupProgramSchedule> requisitionGroupProgramSchedules;

  @ManyToMany(
      cascade = {CascadeType.PERSIST, CascadeType.MERGE}
      )
  @JoinTable(name = "requisition_group_members",
      joinColumns = @JoinColumn(name = "requisitiongroupid", nullable = false),
      inverseJoinColumns = @JoinColumn(name = "facilityid", nullable = false)
      )
  @Getter
  @Setter
  private List<Facility> memberFacilities;

  private RequisitionGroup(SupervisoryNode supervisoryNode, List<RequisitionGroupProgramSchedule>
      programSchedules, List<Facility> memberFacilities) {
    this.supervisoryNode = supervisoryNode;
    this.requisitionGroupProgramSchedules = programSchedules;
    this.memberFacilities = memberFacilities;
  }

  /**
   * Create a new requisition group with a specified supervisory node, program schedules and
   * facilities.
   *
   * @param supervisoryNode specified supervisory node
   * @param programSchedules specified program schedules
   * @param memberFacilities specified facilities
   * @return the new requisition group
   */
  public static RequisitionGroup newRequisitionGroup(SupervisoryNode supervisoryNode,
                                                     List<RequisitionGroupProgramSchedule>
                                                         programSchedules,
                                                     List<Facility> memberFacilities) {
    RequisitionGroup newRequisitionGroup = new RequisitionGroup(supervisoryNode,
        programSchedules, memberFacilities);

    return newRequisitionGroup;
  }
}