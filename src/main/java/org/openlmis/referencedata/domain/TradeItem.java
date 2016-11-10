package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * TradeItems represent branded/produced/physical products.  A TradeItem is used for Product's that
 * are made and then bought/sold/exchanged.  Unlike a {@link GlobalProduct} a TradeItem usually
 * has one and only one manufacturer and is shipped in exactly one primary package.
 *
 * <p>TradeItem's also may:
 * <ul>
 *   <li>have a GlobalTradeItemNumber</li>
 *   <li>a MSRP</li>
 * </ul>
 */
@Entity
@DiscriminatorValue("TRADE_ITEM")
@NoArgsConstructor
public final class TradeItem extends OrderableProduct {
  @JsonProperty
  private String manufacturer;

  @ManyToOne
  private GlobalProduct globalProduct;

  private TradeItem(Code productCode, Dispensable dispensable, String name, long packSize) {
    super(productCode, dispensable, name, packSize);
  }

  @Override
  public String getDescription() {
    return manufacturer;
  }

  @Override
  /**
   * A TradeItem can fulfill for the given product if the product is this trade item or if this
   * product's GlobalProduct is the given product.
   * @param product the product we'd like to fulfill for.
   * @returns true if we can fulfill for the given product, false otherwise.
   */
  public boolean canFulfill(OrderableProduct product) {
    return this.equals(product) || hasGlobalProduct(product);
  }

  /**
   * Factory method to create a new trade item.
   * @param productCode a unique product code
   * @param packSize the # of dispensing units contained
   * @return a new trade item or armageddon if failure
   */
  @JsonCreator
  public static TradeItem newTradeItem(@JsonProperty("productCode") String productCode,
                                       @JsonProperty("dispensingUnit") String dispensingUnit,
                                       @JsonProperty("name") String name,
                                       @JsonProperty("packSize") long packSize) {
    Code code = Code.code(productCode);
    Dispensable dispensable = Dispensable.createNew(dispensingUnit);
    TradeItem tradeItem = new TradeItem(code, dispensable, name, packSize);

    return tradeItem;
  }

  /**
   * Assign a global product.
   * @param globalProduct the given global product, or null to un-assign.
   */
  void assignGlobalProduct(GlobalProduct globalProduct) {
    if (null == globalProduct || hasSameDispensingUnit(globalProduct)) {
      this.globalProduct = globalProduct;
    } else {
      throw new IllegalArgumentException(
          "Global product cannot be assigned because dispensing units are different");
    }
  }

  /*
  returns true if we have a global product and the one given has the same product code,
   false otherwise.
   */
  private boolean hasGlobalProduct(OrderableProduct product) {
    return null != globalProduct && globalProduct.equals(product);
  }

  private boolean hasSameDispensingUnit(OrderableProduct product) {
    return this.getDispensable().equals(product.getDispensable());
  }
}
