/*
 * Copyright 2010-2014 Susanta Tewari. <statsusant@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package coalescent.data.meta;

import coalescent.recursion.ProbComputer_Exact;
import coalescent.recursion.RecursionListener;
import coalescent.statistic.K69_AC;
import commons.util.ExceptionUtil;

import java.math.BigDecimal;
import java.util.*;

/**
 * Meta-Information on {@code K69Data}.
 *
 * @since
 * @version
 * @author Susanta Tewari
 * @history Created on 9/23/13.
 */
public class MetaInf_K69Data implements MetaInf_AC {

    /** theta-prob mapping with history (current to oldest) */
    private final Map<Double, SortedMap<Date, BigDecimal>> prob;
    private final int HISTORY_LIMIT = 10;

    public MetaInf_K69Data() {
        this.prob = new HashMap<>();
    }

    public MetaInf_K69Data(Map<Double, SortedMap<Date, BigDecimal>> prob) {
        this.prob = new HashMap<>(prob);
    }

    public Map<Double, SortedMap<Date, BigDecimal>> getProb() {
        return new HashMap<>(prob);
    }

    /**
     * A convenience method for {@code getProb.keySet()}.
     *
     * @return mutation rates (theta)
     */
    public Set<Double> getRates() {
        return prob.keySet();
    }

    /**
     * adds theta-prob map time-stamped
     * @param theta theta
     * @param exactProb exact prob.
     */
    public void addProb(double theta, BigDecimal exactProb) {

        if (!prob.containsKey(theta)) {

            final Comparator<Object> comparator = Collections.reverseOrder();

            prob.put(theta, new TreeMap<Date, BigDecimal>(comparator));

        } else {
            if (prob.get(theta).size() >= HISTORY_LIMIT) return;
        }

        prob.get(theta).put(new Date(), exactProb);
    }

    public boolean contains(double theta) {
        return prob.containsKey(theta);
    }

    /**
     * @param theta theta
     * @return exact probability last recorded
     */
    public BigDecimal getProb(double theta) {

        ExceptionUtil.throwArgEx(!contains(theta), "");

        return prob.get(theta).values().iterator().next();
    }

    @Override
    public void processComputation(RecursionListener computer) {

        if (computer instanceof ProbComputer_Exact) {

            final ProbComputer_Exact<?> probComputer = (ProbComputer_Exact) computer;
            final K69_AC ac                          = (K69_AC) probComputer.getSampleConfig();
            final Double theta                       = ac.getModel().getMutationRate();
            final BigDecimal result                  = probComputer.getResult();

            addProb(theta, result);
        }
    }
}
