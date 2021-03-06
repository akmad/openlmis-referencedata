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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.validate.ValidationTestUtils.assertErrorMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.dto.SupervisoryNodeDto;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.util.messagekeys.SupervisoryNodeMessageKeys;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;

@RunWith(MockitoJUnitRunner.class)
public class SupervisoryNodeValidatorTest {

  private static final String CODE = "code";
  private static final String DC1 = "DC1";

  @Mock
  private SupervisoryNodeRepository repository;

  @InjectMocks
  private Validator validator = new SupervisoryNodeValidator();

  private SupervisoryNodeDto dto;
  private BeanPropertyBindingResult errors;

  @Before
  public void setUp() {
    dto = new SupervisoryNodeDto();
    errors = new BeanPropertyBindingResult(dto, "supervisoryNodeDto");
  }

  @Test
  public void shouldNotFindErrorsIfSupervisoryNodeIsValid() {
    new SupervisoryNodeDataBuilder()
        .build()
        .export(dto);

    validator.validate(dto, errors);

    assertEquals(0, errors.getErrorCount());
  }

  @Test
  public void shouldRejectIfCodeIsNull() {
    new SupervisoryNodeDataBuilder()
        .withoutCode()
        .build()
        .export(dto);

    validator.validate(dto, errors);

    assertErrorMessage(errors, CODE, SupervisoryNodeMessageKeys.ERROR_CODE_REQUIRED);
  }

  @Test
  public void shouldRejectIfCodeIsDuplicated() {
    when(repository.existsByCode(DC1)).thenReturn(true);

    new SupervisoryNodeDataBuilder()
        .withCode(DC1)
        .build()
        .export(dto);

    validator.validate(dto, errors);

    assertErrorMessage(errors, CODE, SupervisoryNodeMessageKeys.ERROR_CODE_MUST_BE_UNIQUE);
  }

  @Test
  public void shouldRejectIfNameIsNull() {
    new SupervisoryNodeDataBuilder()
        .withoutCode()
        .build()
        .export(dto);

    validator.validate(dto, errors);

    assertErrorMessage(errors, CODE, SupervisoryNodeMessageKeys.ERROR_CODE_REQUIRED);
  }
}