/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
public class SupervisoryNodeDto extends SupervisoryNodeBaseDto {

  @JsonProperty
  @Getter
  private SupervisoryNodeBaseDto parentNode;

  @JsonProperty
  @Getter
  private RequisitionGroupBaseDto requisitionGroup;

  @JsonProperty
  private Set<SupervisoryNodeBaseDto> childNodes;


  public SupervisoryNodeDto(UUID id) {
    super(id);
  }

  @JsonIgnore
  @Override
  public void setFacility(Facility facility) {
    if (facility != null) {
      FacilityDto facilityDto = new FacilityDto();
      facility.export(facilityDto);
      setFacility(facilityDto);
    } else {
      setFacility((FacilityDto) null);
    }
  }

  @JsonIgnore
  @Override
  public void setParentNode(SupervisoryNode parentNode) {
    if (parentNode != null) {
      SupervisoryNodeBaseDto supervisoryNodeBaseDto = new SupervisoryNodeDto();
      parentNode.export(supervisoryNodeBaseDto);
      setParentNode(supervisoryNodeBaseDto);
    } else {
      setParentNode((SupervisoryNodeBaseDto) null);
    }
  }

  public void setParentNode(SupervisoryNodeBaseDto parentNode) {
    this.parentNode = parentNode;
  }

  @JsonIgnore
  @Override
  public void setChildNodes(Set<SupervisoryNode> childNodes) {
    if (childNodes != null) {
      Set<SupervisoryNodeBaseDto> supervisoryNodeBaseDtos = new HashSet<>();

      for (SupervisoryNode childNode : childNodes) {
        SupervisoryNodeBaseDto supervisoryNodeBaseDto = new SupervisoryNodeBaseDto();
        childNode.export(supervisoryNodeBaseDto);
        supervisoryNodeBaseDtos.add(supervisoryNodeBaseDto);
      }

      setChildNodeDtos(supervisoryNodeBaseDtos);
    } else {
      setChildNodeDtos(null);
    }
  }

  public void setChildNodeDtos(Set<SupervisoryNodeBaseDto> childNodes) {
    this.childNodes = childNodes;
  }

  @JsonIgnore
  @Override
  public void setRequisitionGroup(RequisitionGroup requisitionGroup) {
    if (requisitionGroup != null) {
      RequisitionGroupBaseDto requisitionGroupBaseDto = new RequisitionGroupDto();
      requisitionGroup.export(requisitionGroupBaseDto);
      setRequisitionGroup(requisitionGroupBaseDto);
    } else {
      setRequisitionGroup((RequisitionGroupBaseDto) null);
    }
  }

  public void setRequisitionGroup(RequisitionGroupBaseDto requisitionGroup) {
    this.requisitionGroup = requisitionGroup;
  }

  @Override
  public Set<SupervisoryNode.Importer> getChildNodes() {
    if (this.childNodes == null) {
      return null;
    }

    Set<SupervisoryNode.Importer> childNodes = new HashSet<>();
    childNodes.addAll(this.childNodes);
    return childNodes;
  }


}
