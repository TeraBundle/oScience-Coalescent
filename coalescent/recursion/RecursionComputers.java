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

package coalescent.recursion;

import coalescent.recursion.listener.FocusedFwdCountChecker;
import coalescent.recursion.listener.FwdCountChecker;
import coalescent.statistic.AC;

/**
 * Contains static factory methods for recursion based computations.
 *
 * @version 1.4.0
 * @author Susanta Tewari
 * @history Created on 12/03/2010
 * @since 1.3.0
 */
public class RecursionComputers {

    private RecursionComputers() {}

    /**
     * @return a new instance of exact probability counter
     */
    public static ProbComputer_Exact getExactProbComputer() {
        return new ProbComputer_Exact();
    }

    /**
     * @return a new instance of config counter
     */
    public static AC_Counter getConfigCounter() {
        return new AC_Counter();
    }

    /**
     * @return a new instance of genealogy counter
     */
    public static GenealogyCounter getGenealogyCounter() {
        return new GenealogyCounter();
    }

    /**
     * @return a new instance of config builder
     */
    public static AC_Builder getConfigBuilder() {
        return new AC_Builder();
    }

    /**
     * @return a new instance of genealogy builder
     */
    public static GenealogyBuilder getGenealogyBuilder() {
        return new GenealogyBuilder();
    }

    /**
     * @return a new instance of forward count checker
     */
    public static FwdCountChecker getFwdCountChecker() {
        return new FwdCountChecker();
    }

    /**
     * @return a new instance of forward count checker
     */
    public static FocusedFwdCountChecker getFocusedFwdCountChecker(final AC<?, ?> focus) {
        return new FocusedFwdCountChecker(focus);
    }
}
