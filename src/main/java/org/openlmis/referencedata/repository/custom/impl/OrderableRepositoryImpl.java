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

package org.openlmis.referencedata.repository.custom.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.repository.custom.OrderableRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class OrderableRepositoryImpl implements OrderableRepositoryCustom {

  private static final String PRODUCT_CODE = "productCode";
  private static final String NAME = "fullProductName";
  private static final String PROGRAMS = "programOrderables";
  private static final String PROGRAM = "program";
  private static final String CODE = "code";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all orderables with matched parameters.
   * Method is ignoring case for orderable code and name.
   * To find all wanted orderables by code and name we use criteria query and like operator.
   *
   * @param code Part of wanted code.
   * @param name Part of wanted name.
   * @param programCode Wanted program.
   * @return List of orderables matching the parameters.
   */
  public Page<Orderable> search(String code, String name, Code programCode, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<Orderable> orderableQuery = builder.createQuery(Orderable.class);
    prepareQuery(orderableQuery, code, name, programCode, false);

    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    countQuery = prepareQuery(countQuery, code, name, programCode, true);

    Long count = entityManager.createQuery(countQuery).getSingleResult();

    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);
    List<Orderable> orderableList = entityManager.createQuery(orderableQuery)
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();
    return Pagination.getPage(orderableList, pageable, count);
  }

  private <T> CriteriaQuery<T> prepareQuery(CriteriaQuery<T> query,
                                            String code,
                                            String name,
                                            Code programCode,
                                            boolean count) {

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    Root<Orderable> root = query.from(Orderable.class);

    if (count) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      query = (CriteriaQuery<T>) countQuery.select(builder.count(root));
    }

    Predicate predicate = builder.conjunction();

    if (code != null) {
      predicate = builder.and(predicate,
          builder.like(builder.upper(root.get(PRODUCT_CODE).get("code")),
              "%" + code.toUpperCase() + "%"));
    }

    if (name != null) {
      predicate = builder.and(predicate,
          builder.like(builder.upper(root.get(NAME)), "%" + name.toUpperCase() + "%"));
    }

    if (programCode != null) {
      Join<Orderable, ProgramOrderable> orderablePrograms = root.join(PROGRAMS, JoinType.LEFT);
      Join<Orderable, ProgramOrderable> programs = orderablePrograms.join(PROGRAM, JoinType.INNER);
      predicate = builder.and(predicate,
          builder.like(builder.upper(programs.get(CODE).get("code")),
              programCode.toString().toUpperCase()));
    }

    query.where(predicate);
    return query;
  }
}
