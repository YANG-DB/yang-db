/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.NullOrder.NULL_FIRST;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOrder.ASC;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.utils.ExprValueOrdering;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.google.common.collect.Iterators;

import java.util.*;

import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Sort Operator.The input data is sorted by the sort fields in the {@link SortOperator#sortList}.
 * The sort field is specified by the {@link Expression} with {@link SortOption}.
 * The count indicate how many sorted result should been return.
 */
@ToString
@EqualsAndHashCode
public class SortOperator extends PhysicalPlan {
  @Getter
  private final PhysicalPlan input;
  /**
   * How many sorted result should been return. If count = 0, all the resulted will be returned.
   */
  @Getter
  private final Integer count;
  @Getter
  private final List<Pair<SortOption, Expression>> sortList;
  @EqualsAndHashCode.Exclude
  private final Sorter sorter;
  @EqualsAndHashCode.Exclude
  private Iterator<ExprValue> iterator;

  /**
   * Sort Operator Constructor.
   * @param input input {@link PhysicalPlan}
   * @param count how many sorted result should been return
   * @param sortList list of sort sort field.
   *                 The sort field is specified by the {@link Expression} with {@link SortOption}
   */
  public SortOperator(
      PhysicalPlan input, Integer count, List<Pair<SortOption, Expression>> sortList) {
    this.input = input;
    this.count = count;
    this.sortList = sortList;
    Sorter.SorterBuilder sorterBuilder = Sorter.builder();
    for (Pair<SortOption, Expression> pair : sortList) {
      SortOption option = pair.getLeft();
      ExprValueOrdering ordering =
          ASC.equals(option.getSortOrder())
              ? ExprValueOrdering.natural()
              : ExprValueOrdering.natural().reverse();
      ordering =
          NULL_FIRST.equals(option.getNullOrder()) ? ordering.nullsFirst() : ordering.nullsLast();
      sorterBuilder.comparator(Pair.of(pair.getRight(), ordering));
    }
    this.sorter = sorterBuilder.build();
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitSort(this, context);
  }

  @Override
  public void open() {
    super.open();
    PriorityQueue<ExprValue> sorted = new PriorityQueue<>(1, sorter::compare);
    while (input.hasNext()) {
      sorted.add(input.next());
    }

    Iterator<ExprValue> sortedIterator = iterator(sorted);
    iterator = count == 0 ? sortedIterator : Iterators.limit(sortedIterator, count);
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return Collections.singletonList(input);
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public ExprValue next() {
    return iterator.next();
  }

    private Iterator<ExprValue> iterator(PriorityQueue<ExprValue> result) {
    return new Iterator<ExprValue>() {
      @Override
      public boolean hasNext() {
        return !result.isEmpty();
      }

      @Override
      public ExprValue next() {
        return result.poll();
      }
    };
  }

  static class Sorter implements Comparator<ExprValue> {
    private final List<Pair<Expression, Comparator<ExprValue>>> comparators;

    public int compare(ExprValue o1, ExprValue o2) {
      Iterator var3 = this.comparators.iterator();

      int result;
      do {
        if (!var3.hasNext()) {
          return 0;
        }

        Pair<Expression, Comparator<ExprValue>> comparator = (Pair)var3.next();
        Expression expression = (Expression)comparator.getKey();
        result = ((Comparator)comparator.getValue()).compare(expression.valueOf(o1.bindingTuples()), expression.valueOf(o2.bindingTuples()));
      } while(result == 0);

      return result;
    }

    @Generated
    Sorter(List<Pair<Expression, Comparator<ExprValue>>> comparators) {
      this.comparators = comparators;
    }

    @Generated
    public static SortOperator.Sorter.SorterBuilder builder() {
      return new SortOperator.Sorter.SorterBuilder();
    }

    @Generated
    public static class SorterBuilder {
      @Generated
      private ArrayList<Pair<Expression, Comparator<ExprValue>>> comparators;

      @Generated
      SorterBuilder() {
      }

      @Generated
      public SortOperator.Sorter.SorterBuilder comparator(Pair<Expression, Comparator<ExprValue>> comparator) {
        if (this.comparators == null) {
          this.comparators = new ArrayList();
        }

        this.comparators.add(comparator);
        return this;
      }

      @Generated
      public SortOperator.Sorter.SorterBuilder comparators(Collection<? extends Pair<Expression, Comparator<ExprValue>>> comparators) {
        if (comparators == null) {
          throw new NullPointerException("comparators cannot be null");
        } else {
          if (this.comparators == null) {
            this.comparators = new ArrayList();
          }

          this.comparators.addAll(comparators);
          return this;
        }
      }

      @Generated
      public SortOperator.Sorter.SorterBuilder clearComparators() {
        if (this.comparators != null) {
          this.comparators.clear();
        }

        return this;
      }

      @Generated
      public SortOperator.Sorter build() {
        List comparators;
        switch(this.comparators == null ? 0 : this.comparators.size()) {
          case 0:
            comparators = Collections.emptyList();
            break;
          case 1:
            comparators = Collections.singletonList((Pair)this.comparators.get(0));
            break;
          default:
            comparators = Collections.unmodifiableList(new ArrayList(this.comparators));
        }

        return new SortOperator.Sorter(comparators);
      }

      @Generated
      public String toString() {
        return "SortOperator.Sorter.SorterBuilder(comparators=" + this.comparators + ")";
      }
    }
  }

}
