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

package commons.is;

/**
 * Proposal distribution for Importance Sampling (IS).
 *
 * @param <X> domain of sampling
 * @author Susanta Tewari
 * @version 1.4.2
 * @history First created on 03/07/2012.
 * @since 1.4.2
 * @see Sampler
 */
public interface Proposal<X> {

    /**
     * Place holder for initializing heavy stuff. This is only called just before this proposal
     * is actively used by {@link commons.is.Sampler#run()}.
     */
    void init();

    /**
     * signals that resources may be released at this point.
     */
    void clear();

    /**
     * @return a newly drawn independent sample from this proposal
     */
    X sample();

    /**
     * If, none is specified, it uses the current system time as the random seed.
     *
     * @param randomSeed seed to be used for randomization
     */
    void setRandomSeed(long randomSeed);
}
