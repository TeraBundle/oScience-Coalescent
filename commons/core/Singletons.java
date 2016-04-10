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

import org.apache.commons.lang.builder.ToStringStyle;

import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Susanta Tewari
 */
public interface Singletons {

    /** default {@code toString} style */
    public static final ToStringStyle TO_STRING_STYLE = ToStringStyle.DEFAULT_STYLE;

    /** default math rounding mode */
    public static final MathContext MATHCONTEXT_128_HALF_UP = new MathContext(128,
                                                                  RoundingMode.HALF_UP);

    /** default date format: yyyy-MM-dd */
    public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static class DATE {

        private DATE() {}

        /** date-time format */
        public static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");

        public static String toString(Date date) {
            return dateTimeFormat.format(date);
        }

        public static String getDateTime() {
            return toString(new Date());
        }

        public static Date of(String dateString) throws ParseException {
            return dateTimeFormat.parse(dateString);
        }
    }
}
