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

package coalescent.data;

import coalescent.model.KC64;
import com.google.common.collect.ImmutableMap;
import commons.core.Data;
import commons.core.MutableData;
import commons.util.ExceptionUtil;
import org.openide.util.NbBundle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static commons.util.ExceptionUtil.throwArgEx;

/**
 * @since
 * @version
 * @author Susanta Tewari
 * @history Created on 12/4/12.
 *
 * @param <M> description
 * @param <D> description
 */
public class KC64Data<M extends KC64, D extends KC64Data<M, D>> extends Data<M, D> {

    /** Field description */
    private final ImmutableMap<String, Integer> alleleFreqMap;

    /**
     * Initializes the model and the allele frequencies.
     *
     * @param model the data model
     * @param alleleFreqMap allele frequency
     * @throws NullPointerException if any of the parameter is {@code null}
     * @throws IllegalArgumentException if {@code alleleFreqMap} is empty
     */
    protected KC64Data(final M model, final Map<String, Integer> alleleFreqMap) {

        super(model);

        throwArgEx(alleleFreqMap == null, "alleleFreqMap");
        throwArgEx(alleleFreqMap.isEmpty(), getClass(), "Map_Is_Empty", "alleleFreqMap");

        this.alleleFreqMap = ImmutableMap.copyOf(alleleFreqMap);
    }

    public ImmutableMap<String, Integer> getAlleleFreqMap() {
        return alleleFreqMap;
    }

    /**
     * Creates an instance of infinite-alleles data with the given model and the cannonical string
     * representation of the data.
     *
     * @param model the data model
     * @param cannonicalForm th cannonical string representation of the data. The cannonical
     * string representation is of the form {@code f1^t1_f2^t2...} where {@code fi} and {@code ti}
     * are positive and represent the frequency domain and the alleles on that frequency domain,
     * respectively.
     * @return a new instance of {@code KC64Data} with {@code String} based alleles
     */
    public static DEFAULT ofCannonicalForm(final KC64 model, final String cannonicalForm) {

        if (cannonicalForm == null) {
            ExceptionUtil.throwIllegalArgExNull("cannonicalForm");
        }

        return new DEFAULT(model, parseAn(cannonicalForm));
    }

    /**
     * @param cannonicalForm infinite-allele data in the cannonical form
     * @return creates a map with alleles (as keys) and their frequencies (as values)
     */
    private static Map<String, Integer> parseAn(final String cannonicalForm) {

        if (!Pattern.matches("(\\d+\\^\\d+_)*(\\d+\\^\\d+)", cannonicalForm)) {

            final String msg = NbBundle.getMessage(KC64Data.class, "Error_In_KC64_Cannonical_Form",
                                   cannonicalForm);

            throw new IllegalArgumentException(msg);
        }

        final String[] fitis = cannonicalForm.split("_");    // strings of fi^ti


        // calculate fi`s and ti`s
        final int[] fi = new int[fitis.length];
        final int[] ti = new int[fitis.length];

        for (int i = 0; i < fitis.length; i++) {

            fi[i] = Integer.parseInt(fitis[i].split("\\^")[0]);
            ti[i] = Integer.parseInt(fitis[i].split("\\^")[1]);
        }

        final Map<String, Integer> map = new HashMap<>(10);
        int type_counter               = 1;

        for (int i = 0; i < fi.length; i++) {

            for (int j = 0; j < ti[i]; j++) {

                map.put("Type-" + type_counter, fi[i]);

                type_counter++;
            }
        }

        return map;
    }

    /**
     * The alleles (distinct) of this data set.
     *
     * @return the alleles of this data set
     */
    public final Set<String> getAlleles() {
        return alleleFreqMap.keySet();
    }

    /**
     * The number of observations of the specified allele in this data set.
     *
     * @param allele an allele in this data set
     * @return frequency of the specified allele
     */
    public final int getAlleleFrequency(final String allele) {

        checkAllelePresent(allele);

        return alleleFreqMap.get(allele);
    }

    /**
     * The total number of alleles (distinct) in this data set.
     *
     * @return the total number of distinct alleles
     */
    public final int getAlleleCount() {
        return alleleFreqMap.size();
    }

    /**
     * Throws {@code IllegalArgumentException} if the parameter {@code allele} is not present in
     * this data set.
     *
     * @param allele allele supposedly present in this data set
     */
    protected final void checkAllelePresent(final String allele) {

        throwArgEx(allele == null, "allele");
        throwArgEx(!getAlleles().contains(allele), getClass(), "Allele_Not_Present", allele);
    }

    @Override
    public final int getSampleSize() {

        int sum = 0;

        for (final String k : getAlleles()) {
            sum += getAlleleFrequency(k);
        }

        return sum;
    }

    /**
     * @version 1.4.2
     * @since 1.4.0
     * @history Created on 01/24/2013
     * @author Susanta Tewari
     */
    public static class DEFAULT extends KC64Data<KC64, DEFAULT> {

        public DEFAULT(final KC64 model, final Map<String, Integer> alleleFreqMap) {
            super(model, alleleFreqMap);
        }

        @Override
        public MutableData<KC64, DEFAULT> getMutable() {

            return new MutableData<KC64, DEFAULT>(getModel()) {

                @Override
                public DEFAULT getImmutable() {
                    return new DEFAULT(getModel(), getAlleleFreqMap());
                }
            };
        }

        ;
    }
}
