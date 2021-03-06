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

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openlmis.referencedata.domain.Orderable.COMMODITY_TYPE;
import static org.openlmis.referencedata.domain.Orderable.TRADE_ITEM;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import org.assertj.core.api.Assertions;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings({"PMD.TooManyMethods"})
public class OrderableRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<Orderable> {

  private static final String CODE = "abcd";
  private static final String NAME = "Abcd";
  private static final String EACH = "each";
  private static final String DESCRIPTION = "description";
  private static final String ORDERABLE_NAME = "abc";
  private static final String SOME_CODE = "some-code";

  @Autowired
  private OrderableRepository repository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  @Autowired
  private EntityManager entityManager;

  @Override
  CrudRepository<Orderable, UUID> getRepository() {
    return repository;
  }

  @Override
  Orderable generateInstance() {
    int instanceNumber = getNextInstanceNumber();
    return generateInstance(Code.code(CODE + instanceNumber));
  }

  Orderable generateInstance(Code productCode) {
    return new OrderableDataBuilder()
        .withProductCode(productCode)
        .withIdentifier("cSys", "cSysId")
        .withDispensable(Dispensable.createNew(EACH))
        .withFullProductName(NAME)
        .buildAsNew();
  }

  @Test
  public void shouldNotAllowForDuplicatedActiveProgramOrderables() {
    // given
    OrderableDisplayCategory orderableDisplayCategory = createOrderableDisplayCategory(SOME_CODE);
    OrderableDisplayCategory orderableDisplayCategory2 =
        createOrderableDisplayCategory("some-other-code");
    Program program = createProgram(SOME_CODE);
    Orderable orderable = generateInstance();

    ProgramOrderable programOrderable =
        ProgramOrderable.createNew(program, orderableDisplayCategory, orderable, CurrencyUnit.USD);
    ProgramOrderable programOrderableDuplicated =
        ProgramOrderable.createNew(program, orderableDisplayCategory2, orderable, CurrencyUnit.USD);
    orderable.setProgramOrderables(Arrays.asList(programOrderable, programOrderableDuplicated));

    // when
    Throwable thrown = catchThrowable(() -> repository.saveAndFlush(orderable));

    // then
    Assertions.assertThat(thrown).hasMessageContaining("unq_orderableid_programid");
  }

  @Test
  public void shouldAllowForDuplicatedProgramOrderablesIfTheyAreInactive() {
    // given
    OrderableDisplayCategory orderableDisplayCategory = createOrderableDisplayCategory(SOME_CODE);
    OrderableDisplayCategory orderableDisplayCategory2 =
        createOrderableDisplayCategory("some-other-code");
    Program program = createProgram(SOME_CODE);
    Orderable orderable = generateInstance();

    ProgramOrderable programOrderable =
        ProgramOrderable.createNew(program, orderableDisplayCategory, orderable, CurrencyUnit.USD);
    ProgramOrderable programOrderableDuplicated =
        ProgramOrderable.createNew(program, orderableDisplayCategory2, orderable, 0,
            false, true, 0, Money.of(CurrencyUnit.USD, BigDecimal.ZERO), CurrencyUnit.USD);
    orderable.setProgramOrderables(Arrays.asList(programOrderable, programOrderableDuplicated));

    // when
    Orderable savedOrderable = repository.saveAndFlush(orderable);

    // then
    assertEquals(programOrderable, savedOrderable.getProgramOrderable(program));
  }

  @Test
  public void findAllByIdShouldFindAll() {
    // given orderables I want
    Orderable orderable = generateInstance();
    orderable = repository.save(orderable);
    Orderable orderable2 = generateInstance();
    orderable2 = repository.save(orderable2);

    // given an orderable I don't
    repository.save(generateInstance());

    // when
    Set<UUID> ids = newHashSet(orderable.getId(), orderable2.getId());
    Page<Orderable> found = repository.findAllByIds(ids, null);

    // then
    assertEquals(2, found.getTotalElements());
  }

  @Test
  public void shouldFindOrderablesWithSimilarCode() {
    Orderable orderable = generateInstance();
    repository.save(orderable);
    Orderable orderable2 = generateInstance();
    repository.save(orderable2);

    searchOrderablesAndCheckResults(orderable.getProductCode().toString(),
        null, null, orderable, 1);
  }

  @Test
  public void shouldNotFindOrderablesWithSimilarCodeWhenProgramCodeIsBlank() {
    Orderable orderable = generateInstance();
    repository.save(orderable);
    Orderable orderable2 = generateInstance();
    repository.save(orderable2);

    searchOrderablesAndCheckResults(orderable.getProductCode().toString(),
        null, new Program(""), orderable, 0);
  }

  @Test
  public void shouldFindOrderablesWithSimilarCodeIgnoringCase() {
    Orderable orderable = generateInstance();
    repository.save(orderable);

    searchOrderablesAndCheckResults(orderable.getProductCode().toString().toUpperCase(),
        null, null, orderable, 1);
    searchOrderablesAndCheckResults(orderable.getProductCode().toString().toLowerCase(),
        null, null, orderable, 1);
    searchOrderablesAndCheckResults("a", null, null, orderable, 1);
    searchOrderablesAndCheckResults("A", null, null, orderable, 1);
  }

  @Test
  public void shouldFindOrderablesWithSimilarName() {
    Orderable orderable = generateInstance();
    repository.save(orderable);

    searchOrderablesAndCheckResults(null, "Ab", null, orderable, 1);
  }

  @Test
  public void shouldFindOrderablesByEmptyName() {
    Orderable orderable = generateInstance();
    ReflectionTestUtils.setField(orderable, "fullProductName", "");
    repository.save(orderable);

    searchOrderablesAndCheckResults(null, "", null, orderable, 1);
  }

  @Test
  public void shouldNotFindOrderablesWithSimilarNameWhenProgramCodeIsBlank() {
    Orderable orderable = generateInstance();
    repository.save(orderable);

    searchOrderablesAndCheckResults(null, "Ab", new Program(""), orderable, 0);
  }

  @Test
  public void shouldFindOrderablesWithSimilarNameIgnoringCase() {
    Orderable orderable = generateInstance();
    repository.save(orderable);

    searchOrderablesAndCheckResults(null, ORDERABLE_NAME, null, orderable, 1);
    searchOrderablesAndCheckResults(null, "ABC", null, orderable, 1);
    searchOrderablesAndCheckResults(null, "aBc", null, orderable, 1);
    searchOrderablesAndCheckResults(null, "AbC", null, orderable, 1);
  }

  @Test
  public void shouldFindOrderablesWithSimilarCodeAndName() {
    Orderable orderable = generateInstance();
    repository.save(orderable);
    Orderable orderable2 = generateInstance();
    repository.save(orderable2);

    searchOrderablesAndCheckResults(CODE, ORDERABLE_NAME, null, orderable, 2);
  }

  @Test
  public void shouldNotFindOrderablesWithSimilarCodeAndNameWhenProgramCodeIsBlank() {
    Orderable orderable = generateInstance();
    repository.save(orderable);
    Orderable orderable2 = generateInstance();
    repository.save(orderable2);

    searchOrderablesAndCheckResults(CODE, ORDERABLE_NAME, new Program(""), orderable, 0);
  }

  @Test
  public void shouldFindOrderablesWithSimilarCodeAndNameIgnoringCase() {
    Orderable orderable = generateInstance();
    repository.save(orderable);
    Orderable orderable2 = generateInstance();
    repository.save(orderable2);

    searchOrderablesAndCheckResults(CODE, ORDERABLE_NAME, null, orderable, 2);
    searchOrderablesAndCheckResults("ABCD", "ABC", null, orderable, 2);
    searchOrderablesAndCheckResults("a", "AbC", null, orderable, 2);
    searchOrderablesAndCheckResults("A", "aBc", null, orderable, 2);
  }

  @Test
  public void shouldNotFindAnyOrderableForIncorrectCodeAndName() {
    // given a program and an orderable in that program
    Program validProgram = createProgram("valid-code");
    List<ProgramOrderable> programOrderables = new ArrayList<>();
    Orderable validOrderable = new Orderable(Code.code(CODE + getNextInstanceNumber()),
        Dispensable.createNew(EACH), NAME, DESCRIPTION, 10, 5, false, programOrderables, null,
        null);
    repository.save(validOrderable);
    programOrderables.add(createProgramOrderable(validProgram, validOrderable));
    repository.save(validOrderable);

    // when
    Page<Orderable> foundOrderables = repository.search("something", "something", null, null);

    // then
    assertEquals(0, foundOrderables.getTotalElements());
  }

  @Test
  public void shouldFindOrderablesByProgram() {
    // given a program and an orderable in that program
    String programCode = SOME_CODE;
    Orderable validOrderable = createOrderableWithSupportedProgram(programCode);

    // given another program and another orderable in that program
    createOrderableWithSupportedProgram("invalid-code");

    // when
    Page<Orderable> foundOrderables = repository.search(null, null, Code.code(programCode),
        null);

    // then
    assertEquals(1, foundOrderables.getTotalElements());
    assertEquals(validOrderable.getId(), foundOrderables.getContent().get(0).getId());
  }

  @Test
  public void shouldFindOrderablesByProgramCodeIgnoreCase() {
    // given a program
    Program validProgram = createProgram("a-code");

    // given an orderable in that program
    List<ProgramOrderable> programOrderables = new ArrayList<>();
    Orderable validOrderable = new Orderable(Code.code(CODE + getNextInstanceNumber()),
        Dispensable.createNew(EACH), NAME, DESCRIPTION, 10, 5, false, programOrderables, null,
        null);
    repository.save(validOrderable);
    programOrderables.add(createProgramOrderable(validProgram, validOrderable));
    repository.save(validOrderable);

    // given an orderable not in that program
    Orderable orderableWithCode = generateInstance();
    repository.save(orderableWithCode);

    // when & then
    searchOrderablesAndCheckResults(null,
        null,
        new Program("a-code"),
        validOrderable,
        1);
  }

  @Test
  public void shouldFindOrderablesByEmptyProgramCode() {
    Orderable orderable = createOrderableWithSupportedProgram("");
    createOrderableWithSupportedProgram("other");
    createOrderableWithSupportedProgram("not-empty");

    searchOrderablesAndCheckResults(null, null, new Program(""), orderable, 1);
  }

  @Test
  public void shouldFindOrderablesByAllParams() {
    // given a program
    Program validProgram = createProgram("some-test-code");

    // given an orderable in that program
    List<ProgramOrderable> programOrderables = new ArrayList<>();
    Orderable validOrderable = new Orderable(Code.code(CODE), Dispensable.createNew(EACH),
        NAME, DESCRIPTION, 10, 5, false, programOrderables, null, null);
    repository.save(validOrderable);
    programOrderables.add(createProgramOrderable(validProgram, validOrderable));
    repository.save(validOrderable);

    // given some other orderable
    Orderable orderableWithCode = generateInstance();
    repository.save(orderableWithCode);

    // when
    Page<Orderable> foundOrderables = repository.search(validOrderable.getProductCode().toString(),
        CODE,
        validProgram.getCode(),
        null);

    // then
    assertEquals(1, foundOrderables.getTotalElements());
    assertEquals(validOrderable.getId(), foundOrderables.getContent().get(0).getId());
  }

  @Test
  public void shouldFindByProductCode() throws Exception {
    Orderable orderable = generateInstance();
    Code productCode = orderable.getProductCode();

    assertNull(repository.findByProductCode(productCode));
    assertFalse(repository.existsByProductCode(productCode));

    repository.save(orderable);

    assertEquals(orderable.getId(), repository.findByProductCode(productCode).getId());
    assertTrue(repository.existsByProductCode(productCode));
  }

  @Test(expected = PersistenceException.class)
  public void shouldNotAllowDuplicates() {
    Code productCode = Code.code("test_product_code");

    Orderable orderable1 = generateInstance(productCode);
    Orderable orderable2 = generateInstance(productCode);

    repository.save(orderable1);
    repository.save(orderable2);

    entityManager.flush();
  }

  @Test
  public void findAllShouldReturnEmptyPageEmptyContentWithNothingInTheRepository() {
    // given and when
    Pageable pageable = null;
    Page<Orderable> actual = repository.findAll(pageable);

    // then
    assertNotNull(actual);
    assertEquals(0, actual.getContent().size());
  }

  @Test
  public void findAllByIdsShouldReturnEmptyPageEmptyContentWithNothingInTheRepository() {
    // given and when
    Page<Orderable> actual = repository.findAllByIds(null, null);

    // then
    assertNotNull(actual);
    assertEquals(0, actual.getContent().size());
  }

  @Test
  public void searchShouldReturnEmptyPageEmptyContentWithNothingInTheRepository() {
    // given and when
    Page<Orderable> actual = repository.search(null, null, null, null);

    // then
    assertNotNull(actual);
    assertEquals(0, actual.getContent().size());
  }

  @Test
  public void searchShouldPaginate() {
    // given
    for (int i = 0; i < 10; ++i) {
      Orderable orderable = generateInstance();
      repository.save(orderable);
    }

    // when
    Pageable pageable = new PageRequest(1, 2);
    Page<Orderable> actual = repository.search(null, null, null, pageable);

    // then
    assertNotNull(actual);
    assertEquals(1, actual.getNumber());
    assertEquals(2, actual.getSize());
    assertEquals(5, actual.getTotalPages());
    assertEquals(10, actual.getTotalElements());
    assertEquals(2, actual.getContent().size());
  }

  @Test
  public void shouldFindByIdentifier() {
    String identifierValue1 = UUID.randomUUID().toString();
    String identifierValue2 = UUID.randomUUID().toString();

    Orderable orderable1 = new OrderableDataBuilder()
        .withIdentifier(TRADE_ITEM, identifierValue1)
        .buildAsNew();

    Orderable orderable2 = new OrderableDataBuilder()
        .withIdentifier(COMMODITY_TYPE, identifierValue2)
        .buildAsNew();

    repository.save(orderable1);
    repository.save(orderable2);

    List<Orderable> orderables = repository.findAllByIdentifier(TRADE_ITEM, identifierValue1);
    assertThat(orderables, hasSize(1));
    assertThat(orderables.get(0).getId(), is(orderable1.getId()));
    assertThat(orderables.get(0).getTradeItemIdentifier(), is(identifierValue1));

    orderables = repository.findAllByIdentifier(COMMODITY_TYPE, identifierValue2);
    assertThat(orderables, hasSize(1));
    assertThat(orderables.get(0).getId(), is(orderable2.getId()));
    assertThat(orderables.get(0).getCommodityTypeIdentifier(), is(identifierValue2));
  }

  private void searchOrderablesAndCheckResults(String code, String name, Program program,
                                               Orderable orderable, int expectedSize) {
    Code programCode = null == program ? null : program.getCode();
    Page<Orderable> foundOrderables = repository.search(code, name, programCode, null);

    assertEquals(expectedSize, foundOrderables.getTotalElements());

    if (expectedSize > 0) {
      assertEquals(orderable.getFullProductName(),
          foundOrderables.getContent().get(0).getFullProductName());
    }
  }

  private ProgramOrderable createProgramOrderable(Program program, Orderable orderable) {
    OrderableDisplayCategory orderableDisplayCategory = createOrderableDisplayCategory(
        "some-code");

    ProgramOrderable programOrderable = ProgramOrderable.createNew(program,
        orderableDisplayCategory, orderable, CurrencyUnit.USD);

    return programOrderable;
  }

  private OrderableDisplayCategory createOrderableDisplayCategory(String someCode) {
    OrderableDisplayCategory orderableDisplayCategory =
        OrderableDisplayCategory.createNew(Code.code(someCode));
    orderableDisplayCategoryRepository.save(orderableDisplayCategory);
    return orderableDisplayCategory;
  }

  private Orderable createOrderableWithSupportedProgram(String programCode) {
    Program validProgram = createProgram(programCode);
    List<ProgramOrderable> programOrderables = new ArrayList<>();
    Orderable validOrderable = new Orderable(Code.code(CODE + getNextInstanceNumber()),
        Dispensable.createNew(EACH), NAME, DESCRIPTION, 10, 5, false, programOrderables, null,
        null);
    repository.save(validOrderable);
    programOrderables.add(createProgramOrderable(validProgram, validOrderable));
    validOrderable = repository.save(validOrderable);
    return validOrderable;
  }

  private Program createProgram(String code) {
    Program program = new Program(code);
    programRepository.save(program);
    return program;
  }
}
