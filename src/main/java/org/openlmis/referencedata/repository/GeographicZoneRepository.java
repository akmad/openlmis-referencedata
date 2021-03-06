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

package org.openlmis.referencedata.repository;

import com.vividsolutions.jts.geom.Point;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.repository.custom.GeographicZoneRepositoryCustom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@JaversSpringDataAuditable
public interface GeographicZoneRepository extends PagingAndSortingRepository<GeographicZone, UUID>,
                                                  GeographicZoneRepositoryCustom {

  @Override
  <S extends GeographicZone> S save(S entity);

  @Override
  <S extends GeographicZone> Iterable<S> save(Iterable<S> entities);

  List<GeographicZone> findByParentAndLevel(GeographicZone parent, GeographicLevel level);

  @Query(name = "GeographicZone.findIdsByParent")
  Set<UUID> findIdsByParent(@Param("parentId") UUID parentId);

  List<GeographicZone> findByLevel(GeographicLevel level);

  <S extends GeographicZone> S findByCode(String code);

  @Query(value = "SELECT gz.*"
      + " FROM referencedata.geographic_zones gz"
      + " WHERE ST_Covers(gz.boundary, :location)",
      nativeQuery = true
  )
  List<GeographicZone> findByLocation(@Param("location") Point location);
}
