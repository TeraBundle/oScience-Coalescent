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

package commons.core;

import commons.util.ExceptionUtil;

/**
 * Allows for changing the model.
 *
 * @param <M> model type
 * @param <D> data type
 * @author Susanta Tewari
 * @history Created on Jan 30, 2012
 */
public abstract class MutableData<M extends Model, D extends Data<M, D>> implements Mutable<D> {

    /** Field description */
    private M model;

    /**
     * Constructs ...
     *
     *
     * @param model
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public MutableData(final M model) {

        if (model == null) ExceptionUtil.throwIllegalArgExNull("model");

        this.model = model;
    }

    /**
     * Changes the underlying model of this data. Exception is thrown if the {@code model} is not
     * valid.
     *
     * @param model new model
     * @throws IllegalArgumentException if {@code model} is not valid for this data
     */
    public void setModel(final M model) {
        this.model = model;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    protected M getModel() {
        return model;
    }
}
