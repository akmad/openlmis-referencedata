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

package org.openlmis.referencedata.validate;

import org.openlmis.referencedata.dto.SupervisoryNodeDto;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * A validator for {@link SupervisoryNodeDto} object.
 */
@Component
public class SupervisoryNodeValidator implements BaseValidator {

  private static final String CODE = "code";
  private static final String NAME = "name";

  @Autowired
  private SupervisoryNodeRepository repository;

  /**
   * Checks if the given class definition is supported.
   *
   * @param clazz the {@link Class} that this {@link Validator} is being asked if it can {@link
   *              #validate(Object, Errors) validate}
   * @return true if {@code clazz} is equal to {@link SupervisoryNodeDto} class definition,
   *         false otherwise
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return SupervisoryNodeDto.class.equals(clazz);
  }

  /**
   * Validates the {@code target} object, which must be an instance of
   * {@link SupervisoryNodeDto} class. It validates that the code is set and it is not already
   * used by any other supervisory node.
   *
   * @param target the object that is to be validated (never {@code null})
   * @param errors contextual state about the validation process (never {@code null})
   */
  @Override
  public void validate(Object target, Errors errors) {
    verifyArguments(target, errors, SupervisoryNodeMessageKeys.ERROR_NULL);
    rejectIfEmptyOrWhitespace(errors, CODE, SupervisoryNodeMessageKeys.ERROR_CODE_REQUIRED);
    rejectIfEmptyOrWhitespace(errors, NAME, SupervisoryNodeMessageKeys.ERROR_NAME_REQUIRED);

    SupervisoryNodeDto node = (SupervisoryNodeDto) target;

    if (repository.existsByCode(node.getCode())) {
      rejectValue(errors, CODE, SupervisoryNodeMessageKeys.ERROR_CODE_MUST_BE_UNIQUE);
    }
  }
}
